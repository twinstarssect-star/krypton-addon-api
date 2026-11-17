package krypton.addons.host;
import krypton.addons.api.*;
import java.util.*;
public final class AddonRegistry {
    public record Entry(String key, Section section, AddonModule module) {}
    private final LinkedHashMap<String,Section> sections=new LinkedHashMap<>();
    private final LinkedHashMap<String,Entry> modules=new LinkedHashMap<>();
    public void register(String addonId, KryptonAddon addon) throws Exception {
        if (!addonId.matches("[a-z][a-z0-9_-]*")) throw new IllegalArgumentException("Invalid addon mod ID");
        if (addon.apiVersion()!=1) throw new IllegalArgumentException("Unsupported addon API: "+addon.apiVersion());
        LinkedHashMap<String,Section> pendingSections=new LinkedHashMap<>();
        LinkedHashMap<String,Entry> pendingModules=new LinkedHashMap<>();
        boolean[] active={true};
        AddonContext context=new AddonContext() {
            private void check() { if(!active[0]) throw new IllegalStateException("Registration has closed"); }
            private String key(String id) { if(!id.matches("[a-z0-9_.-]+")) throw new IllegalArgumentException("Invalid local ID"); return addonId+":"+id; }
            public Section section(String id,String title) {
                check(); String key=key(id); Section section=new Section(key,title);
                if(sections.containsKey(key)||pendingSections.putIfAbsent(key,section)!=null) throw new IllegalArgumentException("Duplicate section: "+key);
                return section;
            }
            public void module(Section section,AddonModule module) {
                check(); Objects.requireNonNull(module);
                if(pendingSections.get(section.id())!=section) throw new IllegalArgumentException("Use a section created by this addon");
                String key=key(module.id());
                if(modules.containsKey(key)||pendingModules.putIfAbsent(key,new Entry(key,section,module))!=null) throw new IllegalArgumentException("Duplicate module: "+key);
            }
        };
        try { addon.initialize(context); } finally { active[0]=false; }
        sections.putAll(pendingSections); modules.putAll(pendingModules);
    }
    public List<Section> sections() { return List.copyOf(sections.values()); }
    public List<Entry> entries() { return List.copyOf(modules.values()); }
    public List<Entry> entries(Section section) { return modules.values().stream().filter(e->e.section().equals(section)).toList(); }
}
