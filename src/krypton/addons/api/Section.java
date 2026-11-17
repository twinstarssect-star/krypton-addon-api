package krypton.addons.api;
import java.util.Objects;
public record Section(String id, String title) {
    public Section { Objects.requireNonNull(id); Objects.requireNonNull(title); if (title.isBlank()) throw new IllegalArgumentException("Blank section title"); }
}
