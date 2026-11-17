package krypton.addons.api;
import java.util.*;
/** Typed settings supported by the addon panel and saved by the host. */
public final class Setting<T> {
    public enum Kind { BOOLEAN, NUMBER, CHOICE }
    private final String id, title;
    private final Kind kind;
    private final List<T> choices;
    private final double min, max, step;
    private T value;
    private Setting(String id, String title, Kind kind, T value, List<T> choices, double min, double max, double step) {
        if (!id.matches("[a-z0-9_.-]+") || title.isBlank()) throw new IllegalArgumentException("Invalid setting ID or title");
        this.id=id; this.title=title; this.kind=kind; this.value=Objects.requireNonNull(value); this.choices=List.copyOf(choices);
        this.min=min; this.max=max; this.step=step;
    }
    public static Setting<Boolean> bool(String id, String title, boolean value) { return new Setting<>(id,title,Kind.BOOLEAN,value,List.of(),0,0,0); }
    public static Setting<Double> number(String id, String title, double value, double min, double max, double step) {
        if (!Double.isFinite(value)||!Double.isFinite(min)||!Double.isFinite(max)||!Double.isFinite(step)||min>max||step<=0||value<min||value>max) throw new IllegalArgumentException("Invalid number range");
        return new Setting<>(id,title,Kind.NUMBER,value,List.of(),min,max,step);
    }
    public static Setting<String> choice(String id, String title, String value, String... choices) {
        List<String> values=List.of(choices); if (values.isEmpty()||!values.contains(value)||new HashSet<>(values).size()!=values.size()) throw new IllegalArgumentException("Invalid choices");
        return new Setting<>(id,title,Kind.CHOICE,value,values,0,0,0);
    }
    public String id() { return id; }
    public String title() { return title; }
    public Kind kind() { return kind; }
    public T get() { return value; }
    @SuppressWarnings("unchecked") public void set(T next) {
        Objects.requireNonNull(next);
        switch (kind) {
            case BOOLEAN -> { if (!(next instanceof Boolean)) throw new IllegalArgumentException("Expected boolean"); }
            case NUMBER -> { if (!(next instanceof Double n)||!Double.isFinite(n)||n<min||n>max) throw new IllegalArgumentException("Number out of range"); }
            case CHOICE -> { if (!choices.contains(next)) throw new IllegalArgumentException("Unknown choice"); }
        }
        value=next;
    }
    @SuppressWarnings("unchecked") public void adjust(int direction) {
        switch (kind) {
            case BOOLEAN -> value=(T)Boolean.valueOf(!(Boolean)value);
            case NUMBER -> { double n=Math.max(min,Math.min(max,(Double)value+step*direction)); value=(T)Double.valueOf(Math.rint(n*1e9)/1e9); }
            case CHOICE -> value=choices.get(Math.floorMod(choices.indexOf(value)+direction,choices.size()));
        }
    }
    @SuppressWarnings("unchecked") public void restore(String text) {
        switch (kind) {
            case BOOLEAN -> { if (!text.equals("true")&&!text.equals("false")) throw new IllegalArgumentException("Invalid boolean"); set((T)Boolean.valueOf(text)); }
            case NUMBER -> set((T)Double.valueOf(text));
            case CHOICE -> set((T)text);
        }
    }
}
