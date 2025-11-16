package com.example.gammautils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HUDOverlay {
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }
        
        GammaUtilsMod mod = GammaUtilsMod.getInstance();
        
        if (mod != null && mod.getMessageTimer() > 0) {
            String message = mod.getDisplayMessage();
            if (message != null && !message.isEmpty()) {
                int x = event.getResolution().getScaledWidth() / 2 - mc.fontRenderer.getStringWidth(message) / 2;
                int y = event.getResolution().getScaledHeight() - 65; // Position above hotbar
                
                // Draw semi-transparent background
                int padding = 2;
                int backgroundWidth = mc.fontRenderer.getStringWidth(message) + padding * 2;
                int backgroundHeight = mc.fontRenderer.FONT_HEIGHT + padding * 2;
                
                Gui.drawRect(
                    x - padding, 
                    y - padding, 
                    x + backgroundWidth - padding, 
                    y + backgroundHeight - padding, 
                    0x80000000
                );
                
                // Draw text (with color code support)
                mc.fontRenderer.drawStringWithShadow(message, x, y, 0xFFFFFF);
            }
        }
    }
}