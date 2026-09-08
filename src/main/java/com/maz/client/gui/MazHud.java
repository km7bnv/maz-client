package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.CombatStats;
import com.maz.client.module.CpsModule;
import com.maz.client.module.Module;
import com.maz.client.module.PingModule;
import com.maz.client.module.PotCounterModule;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MazHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;
    private static final int MEMORY_OK = 0xFF43A047;
    private static final int MEMORY_WARN = 0xFFFFB300;
    private static final int MEMORY_HIGH = 0xFFE53935;
    private static final int PRESSED_TEXT = 0xFFFFFFFF;
    private static final int KEY_BG = 0xFFFFFFFF;
    private static final int SATURATION_FULL = 0xFFFFC107;
    private static final int SATURATION_HALF = 0xFFFFD54F;
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        Module saturation = MazClient.MODULE_MANAGER.getModule("Saturation");
        if (enabled(saturation) && client.player != null) drawSaturationOnHungerBar(graphics, client);
        Module fps = MazClient.MODULE_MANAGER.getModule("FPS");
        if (enabled(fps)) drawHudBox(graphics, client, "FPS", "FPS: " + client.getFps(), pos("FPS", 8, 8));
        Module memory = MazClient.MODULE_MANAGER.getModule("Memory");
        if (enabled(memory)) {Runtime runtime=Runtime.getRuntime();long used=runtime.totalMemory()-runtime.freeMemory(),max=runtime.maxMemory();int percent=max>0?(int)((used*100)/max):0;String pressure=percent>=90?"HIGH":percent>=75?"WARN":"OK";int accent=percent>=90?MEMORY_HIGH:percent>=75?MEMORY_WARN:MEMORY_OK;drawHudBox(graphics,client,"Memory","RAM: "+used/1024/1024+" / "+max/1024/1024+" MB ("+percent+"%) | "+pressure,pos("Memory",8,30),accent);}
        Module coordinates=MazClient.MODULE_MANAGER.getModule("Coordinates");
        if(enabled(coordinates)&&client.player!=null)drawHudBox(graphics,client,"Coordinates","XYZ: "+(int)Math.floor(client.player.getX())+" / "+(int)Math.floor(client.player.getY())+" / "+(int)Math.floor(client.player.getZ()),pos("Coordinates",8,52));
        Module ping=MazClient.MODULE_MANAGER.getModule("Ping");
        if(enabled(ping)&&ping instanceof PingModule pingModule&&client.player!=null&&client.getConnection()!=null){PlayerInfo info=client.getConnection().getPlayerInfo(client.player.getUUID());if(info!=null){pingModule.sample(info.getLatency());drawHudBox(graphics,client,"Ping",pingModule.getDisplayText(),pos("Ping",8,74));}}
        Module speed=MazClient.MODULE_MANAGER.getModule("Speed");
        if(enabled(speed)&&client.player!=null){double dx=client.player.getX()-client.player.xOld,dz=client.player.getZ()-client.player.zOld;drawHudBox(graphics,client,"Speed",String.format(Locale.ROOT,"Speed: %.2f b/s",Math.sqrt(dx*dx+dz*dz)*20.0),pos("Speed",8,96));}
        Module direction=MazClient.MODULE_MANAGER.getModule("Direction");
        if(enabled(direction)&&client.player!=null)drawHudBox(graphics,client,"Direction","Facing: "+facingName(client.player.getYRot()),pos("Direction",8,118));
        Module clock=MazClient.MODULE_MANAGER.getModule("Clock");if(enabled(clock))drawHudBox(graphics,client,"Clock","Time: "+LocalTime.now().format(CLOCK_FORMAT),pos("Clock",8,140));
        Module session=MazClient.MODULE_MANAGER.getModule("Session Timer");
        if(enabled(session)){long totalSeconds=Math.max(0L,System.currentTimeMillis()-MazClient.SESSION_START_MILLIS)/1000L,hours=totalSeconds/3600L,minutes=(totalSeconds%3600L)/60L,seconds=totalSeconds%60L;String text=hours>0?String.format(Locale.ROOT,"Session: %d:%02d:%02d",hours,minutes,seconds):String.format(Locale.ROOT,"Session: %02d:%02d",minutes,seconds);drawHudBox(graphics,client,"Session Timer",text,pos("Session Timer",8,162));}
        Module cps=MazClient.MODULE_MANAGER.getModule("CPS");if(enabled(cps))drawHudBox(graphics,client,"CPS","CPS: L "+CpsModule.getLeftCps()+" | R "+CpsModule.getRightCps(),pos("CPS",8,184));
        Module keystrokes=MazClient.MODULE_MANAGER.getModule("Keystrokes");if(enabled(keystrokes)){HudLayout.Position p=pos("Keystrokes",8,206);drawKeystrokes(graphics,client,p.x(),p.y());}
        Module potCounter=MazClient.MODULE_MANAGER.getModule("PotCounter");if(enabled(potCounter))drawHudBox(graphics,client,"PotCounter","Pots: "+PotCounterModule.countPotions(client),pos("PotCounter",8,272));
        Module watermark=MazClient.MODULE_MANAGER.getModule("Watermark");if(enabled(watermark))drawHudBox(graphics,client,"Watermark","MazClient",pos("Watermark",8,294));
        Module targetHealth=MazClient.MODULE_MANAGER.getModule("Target Health");if(enabled(targetHealth)&&client.hitResult instanceof EntityHitResult hit&&hit.getEntity() instanceof LivingEntity living)drawHudBox(graphics,client,"Target Health",String.format(Locale.ROOT,"%s: %.1f / %.1f HP",living.getName().getString(),Math.max(0.0F,living.getHealth()),living.getMaxHealth()),pos("Target Health",8,316));
        Module itemCounter=MazClient.MODULE_MANAGER.getModule("Item Counter");
        if(enabled(itemCounter)&&client.player!=null){ItemStack held=client.player.getMainHandItem();String text="Item: Empty";if(!held.isEmpty()){int total=0;for(int i=0;i<client.player.getInventory().getContainerSize();i++){ItemStack stack=client.player.getInventory().getItem(i);if(!stack.isEmpty()&&stack.is(held.getItem()))total+=stack.getCount();}text=held.getHoverName().getString()+": "+total;if(held.isDamageableItem()){int max=held.getMaxDamage(),remaining=Math.max(0,max-held.getDamageValue()),percent=max>0?Math.round((remaining*100.0F)/max):0;text+=" | Durability: "+remaining+"/"+max+" ("+percent+"%)";}}drawHudBox(graphics,client,"Item Counter",text,pos("Item Counter",8,338));}
        Module armorDurability=MazClient.MODULE_MANAGER.getModule("Armor Durability");
        if(enabled(armorDurability)&&client.player!=null){int remaining=0,maximum=0,weakestPercent=101;String weakestPiece="";EquipmentSlot[] slots={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};for(EquipmentSlot slot:slots){ItemStack stack=client.player.getItemBySlot(slot);if(!stack.isEmpty()&&stack.isDamageableItem()){int maxDamage=stack.getMaxDamage(),itemRemaining=Math.max(0,maxDamage-stack.getDamageValue()),itemPercent=maxDamage>0?Math.round((itemRemaining*100.0F)/maxDamage):0;maximum+=maxDamage;remaining+=itemRemaining;if(itemPercent<weakestPercent){weakestPercent=itemPercent;weakestPiece=armorSlotName(slot);}}}int percent=maximum>0?Math.round((remaining*100.0F)/maximum):0;String text=maximum>0?"Armor: "+percent+"% | Weakest: "+weakestPiece+" "+weakestPercent+"%":"Armor: none";drawHudBox(graphics,client,"Armor Durability",text,pos("Armor Durability",8,360));}
        Module compass=MazClient.MODULE_MANAGER.getModule("Compass");if(enabled(compass)&&client.player!=null){float yaw=((client.player.getYRot()%360.0F)+360.0F)%360.0F;drawHudBox(graphics,client,"Compass",String.format(Locale.ROOT,"%s %.0f°",compassName(yaw),yaw),pos("Compass",8,382));}
        Module armor=MazClient.MODULE_MANAGER.getModule("Armor HUD");if(enabled(armor)&&client.player!=null)drawHudBox(graphics,client,"Armor HUD",armorHudText(client),pos("Armor HUD",8,404));
        Module combo=MazClient.MODULE_MANAGER.getModule("Combo Counter");if(enabled(combo))drawHudBox(graphics,client,"Combo Counter","Combo: "+CombatStats.getCombo(),pos("Combo Counter",8,426));
        Module reach=MazClient.MODULE_MANAGER.getModule("Reach Display");if(enabled(reach))drawHudBox(graphics,client,"Reach Display",String.format(Locale.ROOT,"Reach: %.2f",CombatStats.getLastReach()),pos("Reach Display",8,448));
        Module potionHud=MazClient.MODULE_MANAGER.getModule("Potion HUD");if(enabled(potionHud)&&client.player!=null)drawHudBox(graphics,client,"Potion HUD","Effects: "+client.player.getActiveEffects().size(),pos("Potion HUD",8,470));
    }

    private static void drawSaturationOnHungerBar(GuiGraphicsExtractor graphics,Minecraft client){float saturation=Math.max(0.0F,Math.min(20.0F,client.player.getFoodData().getSaturationLevel()));if(saturation<=0.0F)return;int centerX=client.getWindow().getGuiScaledWidth()/2,hungerY=client.getWindow().getGuiScaledHeight()-39;for(int i=0;i<10;i++){float points=saturation-i*2.0F;if(points<=0.0F)break;int iconX=centerX+91-i*8-9,y=hungerY+7;if(points>=2.0F)graphics.fill(iconX+1,y,iconX+8,y+2,SATURATION_FULL);else graphics.fill(iconX+4,y,iconX+8,y+2,SATURATION_HALF);}}
    private static boolean enabled(Module module){return module!=null&&module.isEnabled();}
    private static HudLayout.Position pos(String module,int defaultX,int defaultY){return HudLayout.getPosition(module,defaultX,defaultY);}
    public static int previewWidth(Minecraft client,String moduleName){return moduleName.equalsIgnoreCase("Keystrokes")?64:client.font.width(previewText(moduleName))+12;}
    public static int previewHeight(String moduleName){return moduleName.equalsIgnoreCase("Keystrokes")?64:18;}
    public static void drawPreview(GuiGraphicsExtractor graphics,Minecraft client,String moduleName,int x,int y){if(moduleName.equalsIgnoreCase("Keystrokes")){int alpha=HudLayout.getOpacity("Keystrokes");drawKey(graphics,client,"W",x+22,y,false,20,20,alpha);drawKey(graphics,client,"A",x,y+22,false,20,20,alpha);drawKey(graphics,client,"S",x+22,y+22,true,20,20,alpha);drawKey(graphics,client,"D",x+44,y+22,false,20,20,alpha);drawKey(graphics,client,"LMB",x,y+44,false,30,20,alpha);drawKey(graphics,client,"RMB",x+32,y+44,false,30,20,alpha);}else if(moduleName.equalsIgnoreCase("Memory")){drawHudBox(graphics,client,moduleName,previewText(moduleName),new HudLayout.Position(x,y),MEMORY_OK);}else drawHudBox(graphics,client,moduleName,previewText(moduleName),new HudLayout.Position(x,y));}
    private static String previewText(String moduleName){return switch(moduleName){case "FPS"->"FPS: 120";case "Memory"->"RAM: 1024 / 4096 MB (25%) | OK";case "Coordinates"->"XYZ: 100 / 64 / -100";case "Ping"->"Ping: 42 ms";case "Speed"->"Speed: 4.20 b/s";case "Direction"->"Facing: North";case "Clock"->"Time: 12:34 PM";case "Session Timer"->"Session: 12:34";case "CPS"->"CPS: L 8 | R 5";case "PotCounter"->"Pots: 6";case "Watermark"->"MazClient";case "Target Health"->"Zombie: 18.0 / 20.0 HP";case "Item Counter"->"Diamond Pickaxe: 1 | Durability: 1087/1561 (70%)";case "Armor Durability"->"Armor: 82% | Weakest: Boots 34%";case "Compass"->"NW 315°";case "Armor HUD"->"Helmet Diamond Helmet 145/165 | Chest Diamond Chestplate 412/528 | Legs Diamond Leggings 198/225 | Boots Diamond Boots 31/195";case "Combo Counter"->"Combo: 4";case "Reach Display"->"Reach: 3.12";case "Potion HUD"->"Effects: 2";default->moduleName;};}
    private static String facingName(float yaw){float n=((yaw%360.0F)+360.0F)%360.0F;if(n>=315.0F||n<45.0F)return"South";if(n<135.0F)return"West";if(n<225.0F)return"North";return"East";}
    private static String compassName(float yaw){if(yaw>=337.5F||yaw<22.5F)return"S";if(yaw<67.5F)return"SW";if(yaw<112.5F)return"W";if(yaw<157.5F)return"NW";if(yaw<202.5F)return"N";if(yaw<247.5F)return"NE";if(yaw<292.5F)return"E";return"SE";}
    private static String armorSlotName(EquipmentSlot slot){return switch(slot){case HEAD->"Helmet";case CHEST->"Chest";case LEGS->"Legs";case FEET->"Boots";default->slot.getName();};}
    private static String armorHudText(Minecraft client){StringBuilder text=new StringBuilder();EquipmentSlot[] slots={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};for(EquipmentSlot slot:slots){if(text.length()>0)text.append(" | ");ItemStack stack=client.player.getItemBySlot(slot);text.append(armorSlotName(slot)).append(' ');if(stack.isEmpty()){text.append("Empty");continue;}text.append(stack.getHoverName().getString()).append(' ');if(stack.isDamageableItem()){int max=stack.getMaxDamage(),remaining=Math.max(0,max-stack.getDamageValue());text.append(remaining).append('/').append(max);}else text.append("n/a");}return text.toString();}
    private static void drawKeystrokes(GuiGraphicsExtractor graphics,Minecraft client,int x,int y){int key=20,gap=2,alpha=HudLayout.getOpacity("Keystrokes");drawKey(graphics,client,"W",x+key+gap,y,client.options.keyUp.isDown(),key,key,alpha);int rowY=y+key+gap;drawKey(graphics,client,"A",x,rowY,client.options.keyLeft.isDown(),key,key,alpha);drawKey(graphics,client,"S",x+key+gap,rowY,client.options.keyDown.isDown(),key,key,alpha);drawKey(graphics,client,"D",x+(key+gap)*2,rowY,client.options.keyRight.isDown(),key,key,alpha);int mouseY=rowY+key+gap,mouseWidth=key+10;drawKey(graphics,client,"LMB",x,mouseY,client.options.keyAttack.isDown(),mouseWidth,key,alpha);drawKey(graphics,client,"RMB",x+mouseWidth+gap,mouseY,client.options.keyUse.isDown(),mouseWidth,key,alpha);}
    private static void drawKey(GuiGraphicsExtractor graphics,Minecraft client,String label,int x,int y,boolean pressed,int width,int height,int alpha){graphics.fill(x,y,x+width,y+height,withAlpha(pressed?ACCENT:KEY_BG,alpha));int textX=x+(width-client.font.width(label))/2,textY=y+(height-8)/2;graphics.text(client.font,label,textX,textY,pressed?PRESSED_TEXT:adaptiveTextColor(alpha),false);}
    private static void drawHudBox(GuiGraphicsExtractor graphics,Minecraft client,String moduleName,String text,HudLayout.Position p){drawHudBox(graphics,client,moduleName,text,p,ACCENT);}
    private static void drawHudBox(GuiGraphicsExtractor graphics,Minecraft client,String moduleName,String text,HudLayout.Position p,int accent){int alpha=HudLayout.getOpacity(moduleName),width=client.font.width(text)+12;graphics.fill(p.x(),p.y(),p.x()+width,p.y()+18,withAlpha(BACKGROUND,alpha));graphics.fill(p.x(),p.y(),p.x()+3,p.y()+18,withAlpha(accent,alpha));graphics.text(client.font,text,p.x()+7,p.y()+6,adaptiveTextColor(alpha),false);}
    private static int adaptiveTextColor(int alpha){int clamped=Math.max(0,Math.min(255,alpha));int channel=255-clamped;return 0xFF000000|(channel<<16)|(channel<<8)|channel;}
    private static int withAlpha(int color,int alpha){return(Math.max(0,Math.min(255,alpha))<<24)|(color&0x00FFFFFF);}
}
