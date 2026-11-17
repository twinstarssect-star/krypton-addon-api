package example;
import krypton.addons.api.*;
public final class ExampleAddon implements KryptonAddon {
    @Override public void initialize(AddonContext context) {
        Section section=context.section("example","Example Addon");
        context.module(section,new Counter());
    }
    public static final class Counter extends AddonModule {
        private final Setting<Double> interval=setting(Setting.number("interval","Interval (ticks)",100,20,1200,20));
        private final Setting<Boolean> log=setting(Setting.bool("log","Log heartbeat",true));
        private final Setting<String> label=setting(Setting.choice("label","Label","Hello","Hello","Testing","Ready"));
        private long ticks;
        public Counter() { super("counter","Tick Counter","Example lifecycle and settings; logs a heartbeat while enabled."); }
        @Override protected void onEnable() { ticks=0; System.out.println("[ExampleAddon] enabled"); }
        @Override protected void onDisable() { System.out.println("[ExampleAddon] disabled"); }
        @Override protected void onTick() { if(++ticks % interval.get().longValue()==0 && log.get()) System.out.println("[ExampleAddon] "+label.get()+"; ticks="+ticks); }
    }
}
