package krypton.addons.api;
/** Registration is available only during initialize; IDs are scoped to your Fabric mod ID. */
public interface AddonContext {
    Section section(String id, String title);
    void module(Section section, AddonModule module);
}
