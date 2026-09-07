package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.config.ConfigBundle;
import com.maz.client.module.Module;
import com.maz.client.module.ModuleCategory;
import com.maz.client.module.ModuleGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public class MazMenuScreen extends Screen {
    private static final int BG=0xFFF1F5F9,PANEL=0xFFFFFFFF,PANEL2=0xFFE2E8F0,BORDER=0xFFCBD5E1,TEXT=0xFF0F172A,MUTED=0xFF475569,ACCENT=0xFF5865F2,SUCCESS=0xFF16A34A,DANGER=0xFFDC2626;
    private static final int MENU_WIDTH=540,MENU_HEIGHT=340,SIDEBAR_WIDTH=130,ROW_HEIGHT=50,GROUP_HEADER_HEIGHT=18,SCROLL_STEP=28;
    private ModuleCategory selectedCategory=ModuleCategory.PERFORMANCE;
    private int scrollOffset;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;
    private EditBox searchBox;
    private String configStatus="";

    public MazMenuScreen(){ super(Component.literal("MazClient Modules")); }

    @Override protected void init(){
        int left=(width-MENU_WIDTH)/2,top=(height-MENU_HEIGHT)/2;
        int contentLeft=left+SIDEBAR_WIDTH+20;
        searchBox=new EditBox(font,contentLeft,top+78,250,22,Component.literal("Global module search"));
        searchBox.setHint(Component.literal("Search every module..."));
        searchBox.setMaxLength(64);
        searchBox.setResponder(v->{scrollOffset=0;clampScroll();});
        addRenderableWidget(searchBox);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float d){
        g.fill(0,0,width,height,BG);
        int l=(width-MENU_WIDTH)/2,t=(height-MENU_HEIGHT)/2,r=l+MENU_WIDTH,b=t+MENU_HEIGHT;
        g.fill(l-1,t-1,r+1,b+1,BORDER); g.fill(l,t,r,b,PANEL);
        g.fill(l+16,t+16,l+52,t+52,ACCENT); g.text(font,"M",l+30,t+30,0xFFFFFFFF,true);
        g.text(font,"MazClient Modules",l+64,t+20,TEXT,false); g.text(font,"Click a module for details. Use the switch only to toggle.",l+64,t+37,MUTED,false);

        int hudLeft=r-112;
        int importLeft=hudLeft-76;
        int exportLeft=importLeft-76;
        smallButton(g,mx,my,exportLeft,t+20,exportLeft+70,t+48,"EXPORT");
        smallButton(g,mx,my,importLeft,t+20,importLeft+70,t+48,"IMPORT");
        g.fill(hudLeft,t+20,r-16,t+48,ACCENT); g.centeredText(font,"HUD Editor",hudLeft+48,t+30,0xFFFFFFFF);
        g.fill(l,t+66,r,t+67,BORDER);

        int sidebarRight=l+SIDEBAR_WIDTH; g.fill(sidebarRight,t+67,sidebarRight+1,b,BORDER);
        int cy=t+82;
        for(ModuleCategory c:ModuleCategory.values()){
            boolean selected=!isSearching()&&c==selectedCategory;
            boolean hover=inside(mx,my,l+10,cy,sidebarRight-10,cy+26);
            if(selected||hover) g.fill(l+10,cy,sidebarRight-10,cy+26,PANEL2);
            if(selected) g.fill(l+10,cy,l+13,cy+26,ACCENT);
            g.text(font,c.getDisplayName(),l+20,cy+9,selected?ACCENT:TEXT,false);
            cy+=34;
        }

        int contentLeft=sidebarRight+20;
        g.text(font,isSearching()?"Global Search Results":selectedCategory.getDisplayName(),contentLeft,t+108,TEXT,false);
        if(isSearching()) g.text(font,"All categories",r-104,t+108,MUTED,false);
        int viewportTop=t+126,viewportBottom=b-28,moduleY=t+132-scrollOffset;
        boolean found=false;
        g.enableScissor(contentLeft-4,viewportTop,r-16,viewportBottom);
        for(ModuleGroup group:ModuleGroup.values()){
            if(!hasModulesInGroup(group)) continue;
            found=true; g.text(font,group.getDisplayName(),contentLeft,moduleY+2,ACCENT,false); moduleY+=GROUP_HEADER_HEIGHT;
            for(Module m:MazClient.MODULE_MANAGER.getModules()){
                if(!moduleVisible(m)||groupFor(m)!=group) continue;
                boolean hover=inside(mx,my,contentLeft,moduleY,r-20,moduleY+44)&&my>=viewportTop&&my<=viewportBottom;
                g.fill(contentLeft,moduleY,r-20,moduleY+44,hover?PANEL2:PANEL);
                g.text(font,m.getName(),contentLeft+10,moduleY+8,TEXT,false);
                g.text(font,trim(m.getDescription(),40),contentLeft+10,moduleY+25,MUTED,false);
                int tl=r-72,tr=r-30,tt=moduleY+14,tb=moduleY+30;
                if(m.isAction()){
                    g.fill(tl-22,tt,tr,tb,ACCENT); g.centeredText(font,"ACTION",(tl-22+tr)/2,tt+6,0xFFFFFFFF);
                } else {
                    g.fill(tl,tt,tr,tb,m.isEnabled()?SUCCESS:BORDER);
                    int knob=m.isEnabled()?tr-14:tl+2; g.fill(knob,tt+2,knob+12,tb-2,PANEL);
                }
                moduleY+=ROW_HEIGHT;
            }
            moduleY+=4;
        }
        if(!found) g.text(font,isSearching()?"No matching modules.":"No modules yet.",contentLeft,moduleY,MUTED,false);
        g.disableScissor(); drawScrollbar(g,r,viewportTop,viewportBottom);
        g.fill(l,b-27,r,b-26,BORDER); g.text(font,"MazClient "+MazClient.getVersion(),l+16,b-17,MUTED,false);
        if(!configStatus.isEmpty()) g.text(font,trim(configStatus,48),l+142,b-17,MUTED,false);
        super.extractRenderState(g,mx,my,d);
    }

    private void smallButton(GuiGraphicsExtractor g,int mx,int my,int l,int t,int r,int b,String label){boolean hover=inside(mx,my,l,t,r,b);g.fill(l,t,r,b,hover?ACCENT:PANEL2);g.centeredText(font,label,(l+r)/2,t+10,hover?0xFFFFFFFF:TEXT);}

    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dc){
        if(e.button()!=0) return super.mouseClicked(e,dc);
        int l=(width-MENU_WIDTH)/2,t=(height-MENU_HEIGHT)/2,r=l+MENU_WIDTH,b=t+MENU_HEIGHT,sidebarRight=l+SIDEBAR_WIDTH;
        int viewportTop=t+126,viewportBottom=b-28;
        int max=maxScroll();
        if(max>0&&inside(e.x(),e.y(),r-18,viewportTop,r-4,viewportBottom)){
            int track=viewportBottom-viewportTop,thumb=scrollbarThumbHeight(track,max),thumbY=scrollbarThumbY(viewportTop,track,thumb,max);
            if(e.y()>=thumbY&&e.y()<=thumbY+thumb){draggingScrollbar=true;scrollbarDragOffset=(int)e.y()-thumbY;}
            else {double ratio=(e.y()-viewportTop-thumb/2.0)/Math.max(1,track-thumb);scrollOffset=(int)Math.round(Math.max(0,Math.min(1,ratio))*max);draggingScrollbar=true;scrollbarDragOffset=thumb/2;}
            clampScroll(); return true;
        }

        int hudLeft=r-112,importLeft=hudLeft-76,exportLeft=importLeft-76;
        if(inside(e.x(),e.y(),exportLeft,t+20,exportLeft+70,t+48)){exportConfig();return true;}
        if(inside(e.x(),e.y(),importLeft,t+20,importLeft+70,t+48)){importConfig();return true;}
        if(inside(e.x(),e.y(),hudLeft,t+20,r-16,t+48)){Minecraft.getInstance().gui.setScreen(new HudEditorScreen());return true;}

        int cy=t+82; for(ModuleCategory c:ModuleCategory.values()){
            if(inside(e.x(),e.y(),l+10,cy,sidebarRight-10,cy+26)){selectedCategory=c;scrollOffset=0;return true;} cy+=34;
        }
        int contentLeft=sidebarRight+20; if(e.y()<viewportTop||e.y()>viewportBottom) return super.mouseClicked(e,dc);
        int moduleY=t+132-scrollOffset;
        for(ModuleGroup group:ModuleGroup.values()){
            if(!hasModulesInGroup(group)) continue; moduleY+=GROUP_HEADER_HEIGHT;
            for(Module m:MazClient.MODULE_MANAGER.getModules()){
                if(!moduleVisible(m)||groupFor(m)!=group) continue;
                if(inside(e.x(),e.y(),contentLeft,moduleY,r-20,moduleY+44)){
                    if(!m.isAction()&&inside(e.x(),e.y(),r-72,moduleY+14,r-30,moduleY+30)){m.toggle();return true;}
                    Minecraft.getInstance().gui.setScreen(new ModuleDetailsScreen(this,m)); return true;
                }
                moduleY+=ROW_HEIGHT;
            }
            moduleY+=4;
        }
        return super.mouseClicked(e,dc);
    }

    private void exportConfig(){
        try{
            Path path=ConfigBundle.chooseAndExport();
            if(path!=null) configStatus="Exported "+path.getFileName();
        }catch(Exception ex){configStatus="Export failed: "+ex.getMessage();}
    }

    private void importConfig(){
        try{
            Path path=ConfigBundle.chooseAndImport();
            if(path!=null) configStatus="Imported "+path.getFileName()+" • restart for Minecraft settings";
        }catch(Exception ex){configStatus="Import failed: "+ex.getMessage();}
    }

    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){
        if(e.button()==0&&draggingScrollbar){int t=(height-MENU_HEIGHT)/2,b=t+MENU_HEIGHT,vt=t+126,vb=b-28,track=vb-vt,max=maxScroll();if(max>0){int th=scrollbarThumbHeight(track,max),travel=Math.max(1,track-th),ny=(int)e.y()-scrollbarDragOffset,clamped=Math.max(vt,Math.min(vb-th,ny));scrollOffset=(int)Math.round(((clamped-vt)/(double)travel)*max);clampScroll();}return true;}return super.mouseDragged(e,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent e){if(e.button()==0&&draggingScrollbar){draggingScrollbar=false;return true;}return super.mouseReleased(e);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){int l=(width-MENU_WIDTH)/2,t=(height-MENU_HEIGHT)/2,r=l+MENU_WIDTH,b=t+MENU_HEIGHT,contentLeft=l+SIDEBAR_WIDTH+20;if(x>=contentLeft&&x<=r-4&&y>=t+126&&y<=b-28&&sy!=0){scrollOffset-=(int)Math.round(sy*SCROLL_STEP);clampScroll();return true;}return super.mouseScrolled(x,y,sx,sy);}

    private void drawScrollbar(GuiGraphicsExtractor g,int r,int vt,int vb){int max=maxScroll();if(max<=0)return;int track=vb-vt,th=scrollbarThumbHeight(track,max),ty=scrollbarThumbY(vt,track,th,max);g.fill(r-14,vt,r-7,vb,PANEL2);g.fill(r-14,ty,r-7,ty+th,ACCENT);}
    private int scrollbarThumbHeight(int track,int max){return Math.max(24,(track*track)/(track+max));}
    private int scrollbarThumbY(int vt,int track,int th,int max){return vt+(int)Math.round((scrollOffset/(double)max)*Math.max(1,track-th));}
    private void clampScroll(){scrollOffset=Math.max(0,Math.min(maxScroll(),scrollOffset));}
    private int maxScroll(){int h=0;for(ModuleGroup g:ModuleGroup.values()){int c=0;for(Module m:MazClient.MODULE_MANAGER.getModules())if(moduleVisible(m)&&groupFor(m)==g)c++;if(c>0)h+=GROUP_HEADER_HEIGHT+c*ROW_HEIGHT+4;}return Math.max(0,h-(MENU_HEIGHT-160));}
    private boolean hasModulesInGroup(ModuleGroup g){for(Module m:MazClient.MODULE_MANAGER.getModules())if(moduleVisible(m)&&groupFor(m)==g)return true;return false;}
    private boolean moduleVisible(Module m){String q=searchQuery();if(q.isEmpty())return m.getCategory()==selectedCategory;return m.getName().toLowerCase(java.util.Locale.ROOT).contains(q)||m.getDescription().toLowerCase(java.util.Locale.ROOT).contains(q)||m.getCategory().getDisplayName().toLowerCase(java.util.Locale.ROOT).contains(q);}
    private boolean isSearching(){return !searchQuery().isEmpty();}
    private String searchQuery(){return searchBox==null?"":searchBox.getValue().trim().toLowerCase(java.util.Locale.ROOT);}
    private static String trim(String s,int max){if(s==null)return "";return s.length()<=max?s:s.substring(0,max-3)+"...";}
    private ModuleGroup groupFor(Module m){String n=m.getName();if(n.equalsIgnoreCase("Keystrokes"))return ModuleGroup.INPUT;return switch(m.getCategory()){case PERFORMANCE->ModuleGroup.PERFORMANCE;case HUD,COMBAT->ModuleGroup.STATS;case VISUAL->ModuleGroup.VISUALS;case UTILITY->ModuleGroup.GENERAL;};}
    private static boolean inside(double x,double y,int l,int t,int r,int b){return x>=l&&x<=r&&y>=t&&y<=b;}
    @Override public boolean isPauseScreen(){return false;}
}
