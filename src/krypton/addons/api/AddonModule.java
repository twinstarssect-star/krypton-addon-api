package krypton.addons.api;
import java.util.*;
/** Callbacks run on the Minecraft client thread. Addons are trusted code, not sandboxed. */
public abstract class AddonModule {
    private final String id, title, description;
    private final List<Setting<?>> settings=new ArrayList<>();
    private boolean enabled;
    protected AddonModule(String id, String title, String description) {
        if (!id.matches("[a-z0-9_.-]+")||title.isBlank()) throw new IllegalArgumentException("Invalid module ID or title");
        this.id=id; this.title=title; this.description=Objects.requireNonNull(description);
    }
    protected final <T> Setting<T> setting(Setting<T> value) {
        if (value.id().equals("enabled")) throw new IllegalArgumentException("Reserved setting ID: enabled");
        if (settings.stream().anyMatch(s->s.id().equals(value.id()))) throw new IllegalArgumentException("Duplicate setting: "+value.id());
        settings.add(value); return value;
    }
    public final String id() { return id; }
    public final String title() { return title; }
    public final String description() { return description; }
    public final List<Setting<?>> settings() { return Collections.unmodifiableList(settings); }
    public final boolean enabled() { return enabled; }
    public final void enabled(boolean next) {
        if (enabled==next) return;
        if (next) { onEnable(); enabled=true; }
        else { enabled=false; onDisable(); }
    }
    /** Called by the host only while enabled. */
    public final void tick() { if(enabled) onTick(); }
    protected void onEnable() {}
    protected void onDisable() {}
    protected void onTick() {}
}
