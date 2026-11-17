package krypton.addons.api;
/** Implement this interface and declare a Fabric "krypton-addon" entrypoint. */
public interface KryptonAddon {
    default int apiVersion() { return 1; }
    void initialize(AddonContext context) throws Exception;
}
