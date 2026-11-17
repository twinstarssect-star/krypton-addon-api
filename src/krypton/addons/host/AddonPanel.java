package krypton.addons.host;
import krypton.addons.api.*;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import java.util.*;
final class AddonPanel {
    private final AddonRegistry registry;
    private final class_437 screen;
    private boolean open;
    private int sectionOffset,moduleOffset,settingOffset;
    private Section section;
    private AddonRegistry.Entry selected;
    private boolean rendered;
    AddonPanel(AddonRegistry registry,class_437 screen) { this.registry=registry; this.screen=screen; if(!registry.sections().isEmpty()) this.section=registry.sections().getFirst(); }
    boolean open() { return open; }
    boolean key(int code) { if(!open) return false; if(code==256) open=false; return true; }
    private int width() { return screen.field_22789; }
    private int height() { return screen.field_22790; }
    private int sidebar() { return Math.max(80,Math.min(150,width()/4)); }
    private int middle() { return sidebar()+(width()-sidebar())/2; }
    private int visible() { return Math.max(1,(height()-76)/22); }
    private boolean inside(double x,double y,int left,int top,int right,int bottom) { return x>=left&&x<right&&y>=top&&y<bottom; }
    private List<AddonRegistry.Entry> entries() { return section==null?List.of():registry.entries(section); }
    private void text(class_332 draw,String value,int x,int y,int color,int maxWidth) {
        var font=class_310.method_1551().field_1772;
        String clipped=font.method_27523(value,Math.max(0,maxWidth));
        draw.method_51433(font,clipped,x,y,color,false);
    }
    void render(class_332 draw,int mouseX,int mouseY) {
        int w=width(),h=height();
        if(open) {
            draw.method_25294(0,0,w,h,0xff161a21);
            draw.method_25294(0,0,w,39,0xff222a35);
            text(draw,"Krypton / Addons",12,15,0xffedf3ff,w-120);
            draw.method_25294(sidebar(),44,sidebar()+1,h-12,0xff344052);
            draw.method_25294(middle(),44,middle()+1,h-12,0xff344052);
            var sections=registry.sections();
            if(section==null&&!sections.isEmpty()) section=sections.getFirst();
            text(draw,"SECTIONS",10,48,0xff9bb2cb,sidebar()-16);
            for(int i=sectionOffset;i<Math.min(sections.size(),sectionOffset+visible());i++) {
                int y=65+(i-sectionOffset)*22; var row=sections.get(i);
                if(row.equals(section)) draw.method_25294(5,y-3,sidebar()-5,y+17,0xff30465d);
                text(draw,row.title(),10,y+2,0xffedf3ff,sidebar()-20);
            }
            text(draw,section==null?"No addons installed":section.title(),sidebar()+10,48,0xff9bb2cb,middle()-sidebar()-20);
            var entries=entries();
            for(int i=moduleOffset;i<Math.min(entries.size(),moduleOffset+visible());i++) {
                var row=entries.get(i); int y=65+(i-moduleOffset)*22;
                if(row.equals(selected)) draw.method_25294(sidebar()+5,y-3,middle()-5,y+17,0xff263646);
                text(draw,row.module().title(),sidebar()+10,y+2,0xffedf3ff,middle()-sidebar()-53);
                text(draw,row.module().enabled()?"ON":"OFF",middle()-33,y+2,row.module().enabled()?0xff86e5ad:0xff99a8b9,30);
            }
            if(selected!=null) {
                text(draw,selected.module().title(),middle()+10,48,0xffedf3ff,w-middle()-20);
                var settings=selected.module().settings();
                for(int i=settingOffset;i<Math.min(settings.size(),settingOffset+visible());i++) {
                    var row=settings.get(i); int y=65+(i-settingOffset)*22;
                    text(draw,row.title()+": "+row.get(),middle()+10,y+2,0xffc6d5e7,w-middle()-20);
                }
                if(settings.isEmpty()) text(draw,"No settings",middle()+10,70,0xff99a8b9,w-middle()-20);
            } else text(draw,"Select a module",middle()+10,48,0xff99a8b9,w-middle()-20);
            text(draw,"Click toggle; right-click details. Settings: left + / right -; wheel scrolls.",10,h-12,0xff99a8b9,w-20);
            if(!rendered) { rendered=true; AddonHost.LOG.info("Rendered addon section panel"); }
        }
        draw.method_25294(w-96,8,w-8,30,0xff395b79);
        text(draw,open?"Client":"Addons ("+registry.sections().size()+")",w-88,15,0xffffffff,76);
    }
    boolean click(double x,double y,int button) {
        if(inside(x,y,width()-96,8,width()-8,30)) { if(button==0) open=!open; return true; }
        if(!open) return false;
        if(y<65||y>=height()-15) return true;
        int index=(int)((y-65)/22);
        if(index>=visible()) return true;
        if(x<sidebar()) {
            var sections=registry.sections(); index+=sectionOffset;
            if(index<sections.size()) { section=sections.get(index); moduleOffset=0; selected=null; settingOffset=0; }
        } else if(x<middle()) {
            var entries=entries(); index+=moduleOffset;
            if(index<entries.size()) {
                selected=entries.get(index); settingOffset=0;
                if(button==0) {
                    try { selected.module().enabled(!selected.module().enabled()); }
                    catch(Throwable e) { AddonHost.report("Module toggle failed: "+selected.key(),e); }
                    AddonHost.save();
                }
            }
        } else if(selected!=null) {
            var settings=selected.module().settings(); index+=settingOffset;
            if(index<settings.size()&&(button==0||button==1)) { settings.get(index).adjust(button==0?1:-1); AddonHost.save(); }
        }
        return true;
    }
    boolean scroll(double x,double y,double amount) {
        if(!open) return false;
        int step=amount>0?-1:amount<0?1:0;
        if(x<sidebar()) sectionOffset=Math.max(0,Math.min(Math.max(0,registry.sections().size()-visible()),sectionOffset+step));
        else if(x<middle()) moduleOffset=Math.max(0,Math.min(Math.max(0,entries().size()-visible()),moduleOffset+step));
        else if(selected!=null) settingOffset=Math.max(0,Math.min(Math.max(0,selected.module().settings().size()-visible()),settingOffset+step));
        return true;
    }
}

