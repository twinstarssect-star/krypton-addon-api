# Krypton Addon API 1

This companion Fabric mod adds addon JAR loading and custom sections to the recovered Krypton 5.5 client on Minecraft 1.21.11 / Java 21. It does not replace the Krypton JAR or change its authentication code.

## Install

1. Build the runtime host JAR from this source, or use a verified artifact from this exact repository. Put `krypton-addon-host-1.0.0.jar` beside Krypton and Fabric API in the instance's `mods` folder.
2. Put addon JARs in that same `mods` folder. Addons are ordinary Fabric client mods with a `krypton-addon` entrypoint.
3. Restart Minecraft. Open Krypton's normal GUI, then click **Addons** in the top right.
4. Addons can register their own named sections. Select a section on the left. Left-click a module to toggle it; right-click to inspect its settings without toggling it.
5. Left-click a setting to advance/increase it. Right-click reverses/decreases it. Scroll each column independently. Click **Client** to return to the native panels, or press Escape to close the addon panel.

Install `krypton-example-addon-1.0.0.jar` to see the **Example Addon** section and **Tick Counter** module. The counter only prints a heartbeat to the log while enabled.

The API-only `krypton-addon-api-1.0.0.jar` is a compile-time library for authors. Do not put it in `mods`; the host already supplies these classes.

## Write an addon

Implement `krypton.addons.api.KryptonAddon` and use the provided context during `initialize`:

```java
public void initialize(AddonContext context) {
    Section section = context.section("tools", "My Tools");
    context.module(section, new MyModule());
}
```

Extend `AddonModule` and override `onEnable`, `onDisable`, and/or `onTick`. Module IDs are local to the addon; the host prefixes them with the Fabric mod ID. Callbacks run on Minecraft's client thread. Keep callbacks short and do not block rendering with network or disk work. A tick callback failure disables that module and reports the error in the log.

Add boolean, bounded numeric, or choice settings using `setting(Setting.bool(...))`, `setting(Setting.number(...))`, and `setting(Setting.choice(...))`. Read values using `get()`. The setting ID `enabled` is reserved. Register sections and modules only inside `initialize`; the context closes afterward. Failed registration is rolled back. Addon API version 1 is required.

Declare the entrypoint and dependency in `fabric.mod.json`:

```json
{
  "schemaVersion": 1,
  "id": "my_addon",
  "version": "1.0.0",
  "environment": "client",
  "entrypoints": { "krypton-addon": ["example.MyAddon"] },
  "depends": { "krypton_addon_host": ">=1.0.0", "minecraft": "1.21.11", "java": ">=21" }
}
```

Compile against the API JAR without copying API classes into the addon. The included example builds with `build-example.ps1 -JavaHome <JDK21> -ApiJar <api-jar>` and needs no Minecraft dependency because its example callbacks use Java only. For addons that use Minecraft APIs, use a Fabric/Loom project targeting 1.21.11, declare this API as a compile-only dependency, and run Loom's remapJar task.

## State and compatibility

The host saves module enabled states and setting values to `config/krypton-addons.properties` on GUI changes and normal shutdown. Writes use a temporary file and atomic replacement where supported. The host leaves Krypton's own settings alone. Removed addons' saved keys are retained so reinstalling the addon can restore its state.

This API has its own section and module registry. Addon sections appear under the **Addons** tab inside Krypton's existing GUI; they are not appended to the protected native category enum. Native built-in modules are not exposed through this API. Existing Meteor addon JARs are not compatible.

The recovered class name `dev.krypton.1E` identifies the supported GUI. Other Krypton versions may need a different adapter. The host is a normal Fabric mod, not a native DLL patch. Addon JARs run as normal Java mods with the same permissions as Minecraft; install code you trust.

## Source and build

The new API, loader, persistence, UI, example, and API tests are included. This repository does not contain Krypton's proprietary native payload or claim to reconstruct its lost project.

`build-host.py --minecraft <client-intermediary.jar> --fabric-api <fabric-api.jar> --libraries <Minecraft-library-root> --javac <JDK21/bin/javac.exe>` rebuilds the host from intermediary-named source. Supply the matching 1.21.11 runtime dependencies. Use `build-example.ps1` for addon authoring with the SDK alone.


## Build the SDK without Minecraft dependencies

Run `./build-sdk.ps1 -JavaHome <JDK21>` to compile the API-only JAR. Then run `./build-example.ps1 -JavaHome <JDK21> -ApiJar ./krypton-addon-api-1.0.0.jar` to build the example addon. Both scripts use only the installed JDK; no package manager or remote downloader runs.

## Dependency and artifact trust

The build scripts do not install npm, PyPI, Maven, or Gradle packages and do not download executable code. The host build takes the matching Fabric API, Minecraft intermediary JAR, and runtime libraries as explicit local inputs. Java annotation processing is disabled. Get Fabric artifacts from the official Fabric project and Minecraft libraries from your launcher, rather than installing similarly named packages suggested by generated text.

No npm/PyPI/Maven package coordinates have been registered for this SDK. The canonical source is https://github.com/twinstarssect-star/krypton-addon-api. Check the owner and repository name before downloading; a similar package or repository name is not proof of affiliation. Repository privacy and secret scanning do not eliminate dependency impersonation or malicious addons.

## Verification scope

The original companion host passed two offline Windows Sandbox launches with the supplied Krypton build. Automated checks exercised addon discovery, custom-section rendering, left/right mouse behavior, settings changes, and enabled-state restoration after restart. Core API tests cover registration rollback, duplicate/version rejection, callback behavior, bounds, and persistence. This repository omits private runtime logs and the original client/native files. It does not repair or reimplement Krypton's original authentication.
