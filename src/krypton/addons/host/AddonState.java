package krypton.addons.host;
import krypton.addons.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
public final class AddonState {
    private final Path path;
    private final Properties values=new Properties();
    public AddonState(Path path) { this.path=path; }
    public void load(AddonRegistry registry,BiConsumer<String,Throwable> report) throws IOException {
        if(!Files.exists(path)) return;
        try(Reader input=Files.newBufferedReader(path)) { values.load(input); }
        for(var entry:registry.entries()) {
            var module=entry.module(); String prefix=entry.key()+"/";
            for(var setting:module.settings()) {
                String text=values.getProperty(prefix+setting.id());
                if(text!=null) try { setting.restore(text); } catch(RuntimeException e) { report.accept("Invalid saved setting "+prefix+setting.id(),e); }
            }
            if("true".equals(values.getProperty(prefix+"enabled")))
                try { module.enabled(true); } catch(Throwable e) { report.accept("Could not enable "+entry.key(),e); }
        }
    }
    public void save(AddonRegistry registry) throws IOException {
        for(var entry:registry.entries()) {
            var module=entry.module(); String prefix=entry.key()+"/";
            values.setProperty(prefix+"enabled",Boolean.toString(module.enabled()));
            for(var setting:module.settings()) values.setProperty(prefix+setting.id(),setting.get().toString());
        }
        Files.createDirectories(path.toAbsolutePath().getParent());
        Path temp=Files.createTempFile(path.toAbsolutePath().getParent(),"krypton-addons-",".tmp");
        try {
            try(Writer output=Files.newBufferedWriter(temp)) { values.store(output,"Krypton Addon API 1; independent from Krypton native settings"); }
            try { Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch(AtomicMoveNotSupportedException e) { Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
}
