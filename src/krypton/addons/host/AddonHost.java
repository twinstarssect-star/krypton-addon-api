package krypton.addons.host;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import krypton.addons.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public final class AddonHost implements ClientModInitializer {
    public static final Logger LOG=LoggerFactory.getLogger("krypton-addon-host");
    static final AddonRegistry REGISTRY=new AddonRegistry();
    static AddonState state;
    @Override public void onInitializeClient() {
        for(var container:FabricLoader.getInstance().getEntrypointContainers("krypton-addon",KryptonAddon.class)) {
            String id=container.getProvider().getMetadata().getId();
            try { REGISTRY.register(id,container.getEntrypoint()); LOG.info("Loaded Krypton addon: {}",id); }
            catch(Throwable e) { LOG.error("Could not load Krypton addon "+id,e); }
        }
        state=new AddonState(FabricLoader.getInstance().getConfigDir().resolve("krypton-addons.properties"));
        ClientLifecycleEvents.CLIENT_STARTED.register(client->{
            try { state.load(REGISTRY,AddonHost::report); } catch(Exception e) { report("Could not load addon settings",e); }
            LOG.info("Addon API ready: {} sections, {} modules",REGISTRY.sections().size(),REGISTRY.entries().size());
        });
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            for(var entry:REGISTRY.entries()) try { entry.module().tick(); }
            catch(Throwable e) {
                report("Disabled addon module after callback error: "+entry.key(),e);
                try { entry.module().enabled(false); } catch(Throwable disableError) { report("Disable callback failed",disableError); }
                save();
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->{save();
            for(var entry:REGISTRY.entries()) try { entry.module().enabled(false); } catch(Throwable e) { report("Disable callback failed",e); }
        });
        ScreenEvents.AFTER_INIT.register((client,screen,width,height)->{
            if(!screen.getClass().getName().equals("dev.krypton.1E")) return;
            AddonPanel panel=new AddonPanel(REGISTRY,screen);
            ScreenEvents.afterRender(screen).register((s,draw,x,y,delta)->panel.render(draw,x,y));
            ScreenMouseEvents.allowMouseClick(screen).register((s,click)->!panel.click(click.comp_4798(),click.comp_4799(),click.method_74245()));
            ScreenMouseEvents.allowMouseScroll(screen).register((s,x,y,h,v)->!panel.scroll(x,y,v));
            ScreenKeyboardEvents.allowKeyPress(screen).register((s,key)->!panel.key(key.comp_4795()));
            ScreenMouseEvents.allowMouseRelease(screen).register((s,click)->!panel.open());
            ScreenMouseEvents.allowMouseDrag(screen).register((s,click,dx,dy)->!panel.open());
            LOG.info("Attached addon sections to Krypton GUI");
        });
    }
    static void report(String message,Throwable error) { LOG.error(message,error); }
    static void save() { if(state!=null) try { state.save(REGISTRY); } catch(Exception e) { report("Could not save addon settings",e); } }
}

