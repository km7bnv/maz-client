package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class MazPauseScreen extends Screen {
    private static final int OVERLAY=0xA8000000,PANEL=0xFF111827,PANEL_HOVER=0xFF1F2937,BORDER=0xFF334155,TEXT=0xFFF8FAFC,MUTED=0xFF94A3B8,ACCENT=0xFF5865F2,ACCENT_HOVER=0xFF6875FF,DANGER=0xFFB91C1C,DANGER_HOVER=0xFFDC2626,SUCCESS=0xFF22C55E;
    private static final int WIDTH=460,HEIGHT=326,BUTTON_HEIGHT=38,GAP=10;
    public MazPauseScreen(){super(Component.literal("MazClient Pause"));}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float d){g.fill(0,0,width,height,OVERLAY);int l=(width-WIDTH)/2,t=(height-HEIGHT)/2,r=l+WIDTH,b=t+HEIGHT;g.fill(l-1,t-1,r+1,b+1,BORDER);g.fill(l,t,r,b,PANEL);g.fill(l+22,t+20,l+62,t+60,ACCENT);g.centeredText(font,"M",l+42,t+34,0xFFFFFFFF);g.text(font,"MazClient",l+76,t+23,TEXT,false);g.text(font,"Paused • v"+MazClient.getVersion(),l+76,t+42,MUTED,false);Minecraft c=Minecraft.getInstance();String pn=c.player!=null?c.player.getName().getString():"Player",loc=c.hasSingleplayerServer()?"Singleplayer world":"Multiplayer server";int total=MazClient.MODULE_MANAGER.getModules().size(),enabled=0;for(Module m:MazClient.MODULE_MANAGER.getModules())if(m.isEnabled())enabled++;long s=Math.max(0L,(System.currentTimeMillis()-MazClient.SESSION_START_MILLIS)/1000L),min=s/60L,h=min/60L;String session=h>0?String.format("%dh %02dm",h,min%60L):String.format("%dm %02ds",min,s%60L);g.text(font,pn,l+22,t+78,TEXT,false);g.text(font,loc,l+22,t+95,MUTED,false);g.text(font,"Session "+session,r-132,t+78,MUTED,false);g.text(font,enabled+"/"+total+" modules active",r-132,t+95,enabled>0?SUCCESS:MUTED,false);int bl=l+22,br=r-22,y=t+121;button(g,mx,my,bl,y,br,y+BUTTON_HEIGHT,"Resume Game",true,false);y+=BUTTON_HEIGHT+GAP;int half=(br-bl-GAP)/2,mid=bl+half;button(g,mx,my,bl,y,mid,y+BUTTON_HEIGHT,"Client Settings",false,false);button(g,mx,my,mid+GAP,y,br,y+BUTTON_HEIGHT,"HUD Editor",false,false);y+=BUTTON_HEIGHT+GAP;button(g,mx,my,bl,y,br,y+BUTTON_HEIGHT,"Open Modules",false,false);y+=BUTTON_HEIGHT+GAP;button(g,mx,my,bl,y,br,y+BUTTON_HEIGHT,"Disconnect to Title Screen",false,true);g.text(font,"ESC resumes • Right Shift opens modules",l+22,b-20,MUTED,false);super.extractRenderState(g,mx,my,d);}
    private void button(GuiGraphicsExtractor g,int mx,int my,int l,int t,int r,int b,String label,boolean p,boolean danger){boolean h=inside(mx,my,l,t,r,b);int c=danger?(h?DANGER_HOVER:DANGER):(p?(h?ACCENT_HOVER:ACCENT):(h?PANEL_HOVER:0xFF182235));g.fill(l,t,r,b,c);if(!p&&!danger){g.fill(l,t,r,t+1,BORDER);g.fill(l,b-1,r,b,BORDER);g.fill(l,t,l+1,b,BORDER);g.fill(r-1,t,r,b,BORDER);}g.centeredText(font,label,(l+r)/2,t+14,TEXT);}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dc){if(e.button()!=0)return super.mouseClicked(e,dc);int l=(width-WIDTH)/2,t=(height-HEIGHT)/2,r=l+WIDTH,bl=l+22,br=r-22,y=t+121;double x=e.x(),my=e.y();Minecraft c=Minecraft.getInstance();if(inside(x,my,bl,y,br,y+BUTTON_HEIGHT)){c.gui.setScreen(null);return true;}y+=BUTTON_HEIGHT+GAP;int half=(br-bl-GAP)/2,mid=bl+half;if(inside(x,my,bl,y,mid,y+BUTTON_HEIGHT)){c.gui.setScreen(new OptionsScreen(this,c.options,true));return true;}if(inside(x,my,mid+GAP,y,br,y+BUTTON_HEIGHT)){c.gui.setScreen(new HudEditorScreen());return true;}y+=BUTTON_HEIGHT+GAP;if(inside(x,my,bl,y,br,y+BUTTON_HEIGHT)){c.gui.setScreen(new MazMenuScreen());return true;}y+=BUTTON_HEIGHT+GAP;if(inside(x,my,bl,y,br,y+BUTTON_HEIGHT)){if(c.hasSingleplayerServer())c.disconnectWithSavingScreen();else c.disconnectWithProgressScreen();return true;}return super.mouseClicked(e,dc);}
    private static boolean inside(double x,double y,int l,int t,int r,int b){return x>=l&&x<=r&&y>=t&&y<=b;}
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent e){if(e.key()==org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE){Minecraft.getInstance().gui.setScreen(null);return true;}return super.keyPressed(e);}
    @Override public boolean isPauseScreen(){return true;}
}
