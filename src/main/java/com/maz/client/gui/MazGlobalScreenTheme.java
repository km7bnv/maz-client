package com.maz.client.gui;

import com.maz.client.MazClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * Gives normal Minecraft menus a real Maz layout without replacing their screen
 * instances. Minecraft still owns every widget/action; MazClient owns the geometry.
 */
public final class MazGlobalScreenTheme implements ClientModInitializer {
    private static final int BG=0xFF090E1A,BG_TOP=0xFF11192A,PANEL=0xFF141E31,PANEL_2=0xFF1C2942,
            BORDER=0xFF2A3958,TEXT=0xFFF8FAFC,MUTED=0xFF94A3B8,ACCENT=0xFF5865F2,DISABLED=0xFF334155;
    private static final int MENU_WIDTH=620,MENU_HEIGHT=380,FOOTER_HEIGHT=28;
    private static boolean titleRepairQueued;

    @Override public void onInitializeClient(){
        ScreenEvents.AFTER_INIT.register((client,screen,w,h)->{
            if(shouldTheme(screen)){
                applyMazLayout(screen,w,h);
                ScreenEvents.afterBackground(screen).register(MazGlobalScreenTheme::renderTheme);
                ScreenEvents.afterExtract(screen).register(MazGlobalScreenTheme::renderForeground);
            }
            if(screen instanceof TitleScreen && Screens.getWidgets(screen).isEmpty()&&!titleRepairQueued){
                titleRepairQueued=true;
                client.execute(()->{try{
                    Screen current=client.gui.screen();
                    if(current==screen&&current instanceof TitleScreen&&Screens.getWidgets(current).isEmpty())
                        client.gui.setScreen(new TitleScreen());
                }finally{titleRepairQueued=false;}});
            }
        });
    }

    private static boolean shouldTheme(Screen screen){
        String n=screen.getClass().getName();
        if(n.startsWith("com.maz.client.gui."))return false;
        if(n.contains(".screens.inventory."))return false;
        if(n.endsWith("ChatScreen")||n.endsWith("InBedChatScreen")||n.endsWith("DeathScreen"))return false;
        if(n.endsWith("ReceivingLevelScreen")||n.endsWith("LevelLoadingScreen")||n.endsWith("ProgressScreen"))return false;
        return n.startsWith("net.minecraft.client.gui.screens.");
    }

    private static void renderTheme(Screen screen,GuiGraphicsExtractor g,int mx,int my,float d){
        Minecraft c=Minecraft.getInstance();int w=c.getWindow().getGuiScaledWidth(),h=c.getWindow().getGuiScaledHeight();
        Shell s=shell(w,h);g.fill(0,0,w,h,BG);g.fill(0,0,w,Math.max(110,h/3),BG_TOP);
        g.fill(s.left-1,s.top-1,s.right+1,s.bottom+1,BORDER);g.fill(s.left,s.top,s.right,s.bottom,PANEL);
        g.fill(s.left+18,s.top+17,s.left+58,s.top+57,ACCENT);g.centeredText(c.font,"M",s.left+38,s.top+31,0xFFFFFFFF);
        String title=screen.getTitle().getString();if(title.isBlank())title="Minecraft";
        g.text(c.font,title,s.left+74,s.top+20,TEXT,false);
        g.text(c.font,subtitle(screen),s.left+74,s.top+39,MUTED,false);
        g.fill(s.left,s.top+72,s.right,s.top+73,BORDER);
        g.fill(s.left,s.bottom-FOOTER_HEIGHT,s.right,s.bottom-FOOTER_HEIGHT+1,BORDER);
        g.text(c.font,"MazClient "+MazClient.getVersion(),s.left+18,s.bottom-18,MUTED,false);
        String credit="Made by awnkr_par";g.text(c.font,credit,s.right-18-c.font.width(credit),s.bottom-18,MUTED,false);
    }

    private static String subtitle(Screen screen){
        String n=screen.getClass().getSimpleName();
        if(n.contains("Video")||n.contains("Graphics"))return "Graphics & performance";
        if(n.contains("Control")||n.contains("Key"))return "Controls & keybinds";
        if(n.contains("Accessibility"))return "Accessibility";
        if(n.contains("Language"))return "Language & text";
        if(n.contains("Multiplayer")||n.contains("Server"))return "Multiplayer";
        if(n.contains("World"))return "Worlds";
        if(n.contains("Pack"))return "Resource packs";
        if(n.contains("Option")||n.contains("Setting"))return "Minecraft settings";
        return "Minecraft menu";
    }

    private static void applyMazLayout(Screen screen,int width,int height){
        Shell s=shell(width,height);
        List<AbstractWidget> buttons=new ArrayList<>();
        for(AbstractWidget w:Screens.getWidgets(screen))
            if(w.visible&&w instanceof AbstractButton&&w.getWidth()>0&&w.getHeight()>0)buttons.add(w);
        if(buttons.isEmpty())return;

        String n=screen.getClass().getSimpleName();
        boolean listMenu=n.contains("SelectWorld")||n.contains("WorldSelection")||n.contains("JoinMultiplayer")||
                n.contains("Multiplayer")||n.contains("PackSelection")||n.contains("ResourcePack")||n.contains("Language");

        if(listMenu)layoutFooterActions(buttons,s);
        else layoutSettingsGrid(buttons,s);
    }

    private static void layoutSettingsGrid(List<AbstractWidget> buttons,Shell s){
        int left=s.left+24,right=s.right-24,top=s.top+92,bottom=s.bottom-FOOTER_HEIGHT-14;
        int gap=10,rowH=24,colW=(right-left-gap)/2;
        int rows=(buttons.size()+1)/2;
        int used=Math.min(bottom-top,rows*(rowH+gap)-gap);
        int y=top+Math.max(0,(bottom-top-used)/2);
        for(int i=0;i<buttons.size();i++){
            AbstractWidget w=buttons.get(i);int col=i%2,row=i/2;
            int x=left+col*(colW+gap),wy=y+row*(rowH+gap);
            if(wy+rowH>bottom)break;
            w.setX(x);w.setY(wy);w.setWidth(colW);
        }
    }

    private static void layoutFooterActions(List<AbstractWidget> buttons,Shell s){
        int left=s.left+24,right=s.right-24,bottom=s.bottom-FOOTER_HEIGHT-12,gap=8,rowH=24;
        int count=buttons.size(),cols=Math.min(4,Math.max(1,count));
        int colW=(right-left-gap*(cols-1))/cols;
        int rows=(count+cols-1)/cols,startY=bottom-rows*rowH-(rows-1)*gap;
        for(int i=0;i<count;i++){
            AbstractWidget w=buttons.get(i);int row=i/cols,col=i%cols;
            w.setX(left+col*(colW+gap));w.setY(startY+row*(rowH+gap));w.setWidth(colW);
        }
    }

    private static void renderForeground(Screen screen,GuiGraphicsExtractor g,int mx,int my,float d){
        for(AbstractWidget w:Screens.getWidgets(screen)){
            if(!w.visible)continue;
            if(w instanceof AbstractButton)renderButton(g,w,mx,my);
            else if(w.getWidth()<=300&&w.getHeight()<=50)renderFrame(g,w,mx,my);
        }
    }

    private static void renderButton(GuiGraphicsExtractor g,AbstractWidget w,int mx,int my){
        int l=w.getX(),t=w.getY(),r=l+w.getWidth(),b=t+w.getHeight();boolean hover=w.active&&w.isMouseOver(mx,my);
        int fill=w.active?(hover?ACCENT:PANEL_2):DISABLED,border=hover?ACCENT:BORDER;
        g.fill(l,t,r,b,fill);g.fill(l,t,r,t+1,border);g.fill(l,b-1,r,b,border);g.fill(l,t,l+1,b,border);g.fill(r-1,t,r,b,border);
        Minecraft c=Minecraft.getInstance();g.centeredText(c.font,w.getMessage().getString(),(l+r)/2,t+Math.max(1,(w.getHeight()-8)/2),w.active?TEXT:MUTED);
    }

    private static void renderFrame(GuiGraphicsExtractor g,AbstractWidget w,int mx,int my){
        int l=w.getX()-1,t=w.getY()-1,r=w.getX()+w.getWidth()+1,b=w.getY()+w.getHeight()+1,bc=w.isMouseOver(mx,my)?ACCENT:BORDER;
        g.fill(l,t,r,t+1,bc);g.fill(l,b-1,r,b,bc);g.fill(l,t,l+1,b,bc);g.fill(r-1,t,r,b,bc);
    }

    private static Shell shell(int width,int height){
        int w=Math.min(MENU_WIDTH,Math.max(340,width-24)),h=Math.min(MENU_HEIGHT,Math.max(240,height-24));
        int l=(width-w)/2,t=(height-h)/2;return new Shell(l,t,l+w,t+h);
    }
    private record Shell(int left,int top,int right,int bottom){}
}
