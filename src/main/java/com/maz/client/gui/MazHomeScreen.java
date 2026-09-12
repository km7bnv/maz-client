package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class MazHomeScreen extends Screen {
    private static final int BG=0xFF090E1A,BG_TOP=0xFF11192A,PANEL=0xFF141E31,PANEL_HOVER=0xFF1C2942,BORDER=0xFF2A3958,TEXT=0xFFF8FAFC,MUTED=0xFF94A3B8,ACCENT=0xFF5865F2,ACCENT_HOVER=0xFF6875FF,SUCCESS=0xFF22C55E,DANGER=0xFFEF4444;
    private static final int CARD_WIDTH=540,CARD_HEIGHT=334,BUTTON_HEIGHT=42,GAP=10;
    public MazHomeScreen(){super(Component.literal("MazClient Home"));}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float d){g.fill(0,0,width,height,BG);g.fill(0,0,width,Math.max(120,height/3),BG_TOP);int l=(width-CARD_WIDTH)/2,t=(height-CARD_HEIGHT)/2,r=l+CARD_WIDTH,b=t+CARD_HEIGHT;g.fill(l-1,t-1,r+1,b+1,BORDER);g.fill(l,t,r,b,PANEL);g.fill(l+24,t+24,l+72,t+72,ACCENT);g.centeredText(font,"M",l+48,t+42,0xFFFFFFFF);g.text(font,"MazClient",l+88,t+28,TEXT,false);g.text(font,"Version "+MazClient.getVersion(),l+88,t+48,MUTED,false);int enabled=enabledModules(),total=MazClient.MODULE_MANAGER.getModules().size(),statusLeft=r-164;boolean modulesHover=inside(mx,my,statusLeft,t+25,r-24,t+68);g.fill(statusLeft,t+25,r-24,t+68,modulesHover?PANEL_HOVER:BG_TOP);g.fill(statusLeft,t+25,statusLeft+3,t+68,SUCCESS);g.text(font,enabled+"/"+total+" modules active",statusLeft+10,t+33,TEXT,false);g.text(font,"Right Shift  •  Modules",statusLeft+10,t+55,modulesHover?TEXT:MUTED,false);g.text(font,"Your Minecraft, your setup.",l+24,t+88,TEXT,false);g.text(font,"Performance, HUD tools and client controls in one place.",l+24,t+105,MUTED,false);int bl=l+24,br=r-24,y=t+132;button(g,mx,my,bl,y,br,y+BUTTON_HEIGHT,"Singleplayer",true);y+=BUTTON_HEIGHT+GAP;button(g,mx,my,bl,y,br,y+BUTTON_HEIGHT,"Multiplayer",false);y+=BUTTON_HEIGHT+GAP;int half=(br-bl-GAP)/2,mid=bl+half;button(g,mx,my,bl,y,mid,y+BUTTON_HEIGHT,"Client Settings",false);button(g,mx,my,mid+GAP,y,br,y+BUTTON_HEIGHT,"HUD Editor",false);g.fill(l+24,b-43,r-24,b-42,BORDER);g.text(font,"Made by awnkr_par",l+24,b-27,MUTED,false);boolean ah=inside(mx,my,r-142,b-36,r-88,b-12);g.text(font,"About",r-115,b-27,ah?TEXT:MUTED,false);boolean qh=inside(mx,my,r-76,b-36,r-24,b-12);g.text(font,"Quit",r-52,b-27,qh?0xFFFF6B6B:DANGER,false);super.extractRenderState(g,mx,my,d);}
    private int enabledModules(){int n=0;for(Module m:MazClient.MODULE_MANAGER.getModules())if(m.isEnabled())n++;return n;}
    private void button(GuiGraphicsExtractor g,int mx,int my,int l,int t,int r,int b,String label,boolean primary){boolean h=inside(mx,my,l,t,r,b);int c=primary?(h?ACCENT_HOVER:ACCENT):(h?PANEL_HOVER:BG_TOP);g.fill(l,t,r,b,c);if(!primary){g.fill(l,t,r,t+1,BORDER);g.fill(l,b-1,r,b,BORDER);g.fill(l,t,l+1,b,BORDER);g.fill(r-1,t,r,b,BORDER);}g.centeredText(font,label,(l+r)/2,t+16,TEXT);}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dc){if(e.button()!=0)return super.mouseClicked(e,dc);int l=(width-CARD_WIDTH)/2,t=(height-CARD_HEIGHT)/2,r=l+CARD_WIDTH,b=t+CARD_HEIGHT,bl=l+24,br=r-24,y=t+132,statusLeft=r-164;double x=e.x(),my=e.y();Minecraft c=Minecraft.getInstance();if(inside(x,my,statusLeft,t+25,r-24,t+68)){c.gui.setScreen(new MazMenuScreen());return true;}if(inside(x,my,bl,y,br,y+BUTTON_HEIGHT)){c.gui.setScreen(new SelectWorldScreen(this));return true;}y+=BUTTON_HEIGHT+GAP;if(inside(x,my,bl,y,br,y+BUTTON_HEIGHT)){c.gui.setScreen(new JoinMultiplayerScreen(this));return true;}y+=BUTTON_HEIGHT+GAP;int half=(br-bl-GAP)/2,mid=bl+half;if(inside(x,my,bl,y,mid,y+BUTTON_HEIGHT)){c.gui.setScreen(new OptionsScreen(this,c.options,false));return true;}if(inside(x,my,mid+GAP,y,br,y+BUTTON_HEIGHT)){c.gui.setScreen(new HudEditorScreen());return true;}if(inside(x,my,r-142,b-36,r-88,b-12)){c.gui.setScreen(new MazAboutScreen(this));return true;}if(inside(x,my,r-76,b-36,r-24,b-12)){org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose(c.getWindow().handle(),true);return true;}return super.mouseClicked(e,dc);}
    private static boolean inside(double x,double y,int l,int t,int r,int b){return x>=l&&x<=r&&y>=t&&y<=b;}
    @Override public boolean isPauseScreen(){return false;}
}
