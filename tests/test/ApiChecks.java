package test;
import krypton.addons.api.*;
import krypton.addons.host.*;
import java.nio.file.*;
public final class ApiChecks {
    static int enabled,disabled,ticks;
    static class M extends AddonModule {
        final Setting<Double> amount=setting(Setting.number("amount","Amount",2,0,10,1));
        M() { super("test","Test","Test module"); }
        protected void onEnable() { enabled++; }
        protected void onDisable() { disabled++; }
        protected void onTick() { ticks++; }
    }
    static void check(boolean value) { if(!value) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        AddonRegistry registry=new AddonRegistry(); M first=new M();
        registry.register("test",context->{var section=context.section("tools","Tools"); context.module(section,first);});
        try { registry.register("bad",context->{context.section("partial","Partial"); throw new Exception("Expected");}); throw new AssertionError(); } catch(Exception expected) {}
        check(registry.sections().size()==1&&registry.entries().size()==1);
        try { registry.register("test",context->context.section("tools","Duplicate")); throw new AssertionError(); } catch(IllegalArgumentException expected) {}
        try { registry.register("future",new KryptonAddon() { public int apiVersion(){return 2;} public void initialize(AddonContext c){} }); throw new AssertionError(); } catch(IllegalArgumentException expected) {}
        first.tick(); check(ticks==0); first.enabled(true); first.enabled(true); first.tick(); check(enabled==1&&ticks==1);
        first.amount.set(7.0); AddonState state=new AddonState(Path.of(args[0])); state.save(registry);
        AddonRegistry restored=new AddonRegistry(); M second=new M();
        restored.register("test",context->{var section=context.section("tools","Tools"); context.module(section,second);});
        new AddonState(Path.of(args[0])).load(restored,(m,e)->{throw new AssertionError(m,e);});
        check(second.enabled()&&second.amount.get()==7.0);
        second.enabled(false); second.tick(); check(disabled==1&&ticks==1);
        var choice=Setting.choice("choice","Choice","a","a","b"); choice.adjust(-1); check(choice.get().equals("b"));
        try { first.amount.restore("NaN"); throw new AssertionError(); } catch(IllegalArgumentException expected) {}
        for(int i=0;i<20;i++) first.amount.adjust(1); check(first.amount.get()==10.0);
        System.out.println("PASS: discovery contracts, registration rollback, duplicate/API checks, lifecycle, settings bounds and persisted restart state");
    }
}
