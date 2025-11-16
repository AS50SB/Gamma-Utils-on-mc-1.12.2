package com.example.gammautils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.input.Keyboard;

@Mod(
    modid = GammaUtilsMod.MODID,
    name = GammaUtilsMod.NAME, 
    version = GammaUtilsMod.VERSION,
    clientSideOnly = true
)
public class GammaUtilsMod {
    public static final String MODID = "gammautils";
    public static final String NAME = "Gamma Utils";
    public static final String VERSION = "1.0.0";

    // Key bindings
    private KeyBinding gammaToggleKey;
    private KeyBinding gammaUpKey;
    private KeyBinding gammaDownKey;
    private KeyBinding nightVisionToggleKey;

    // State variables
    private boolean nightVisionEnabled = false;
    private int gammaMode = 0; // 0=default, 1=boosted
    private final float defaultGamma = 1.0F;
    private final float boostedGamma = 15.0F; // 1500%
    private final float gammaStep = 0.1F; // 10%

    // Message display
    private int messageTimer = 0;
    private String displayMessage = "";

    // Mod instance
    @Mod.Instance(MODID)
    public static GammaUtilsMod instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize key bindings
        setupKeyBindings();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Register event handlers
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new HUDOverlay());
    }

    private void setupKeyBindings() {
        // Create key bindings
        gammaToggleKey = new KeyBinding(
            "key.gamma_toggle.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            Keyboard.KEY_G,
            "key.categories.gammautils"
        );

        gammaUpKey = new KeyBinding(
            "key.gamma_up.desc", 
            KeyConflictContext.IN_GAME,
            KeyModifier.CONTROL,
            Keyboard.KEY_U,
            "key.categories.gammautils"
        );

        gammaDownKey = new KeyBinding(
            "key.gamma_down.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.CONTROL, 
            Keyboard.KEY_D,
            "key.categories.gammautils"
        );

        nightVisionToggleKey = new KeyBinding(
            "key.nightvision_toggle.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            Keyboard.KEY_H,
            "key.categories.gammautils"
        );

        // Register key bindings
        ClientRegistry.registerKeyBinding(gammaToggleKey);
        ClientRegistry.registerKeyBinding(gammaUpKey);
        ClientRegistry.registerKeyBinding(gammaDownKey);
        ClientRegistry.registerKeyBinding(nightVisionToggleKey);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        
        // Check key presses
        if (gammaToggleKey.isPressed()) {
            toggleGamma();
            showMessage("Gamma: " + (gammaMode == 1 ? "1500%" : "100%"));
        }
        
        if (gammaUpKey.isPressed()) {
            increaseGamma();
            showMessage("Gamma +10%: " + Math.round(mc.gameSettings.gammaSetting * 100) + "%");
        }
        
        if (gammaDownKey.isPressed()) {
            decreaseGamma();
            showMessage("Gamma -10%: " + Math.round(mc.gameSettings.gammaSetting * 100) + "%");
        }
        
        if (nightVisionToggleKey.isPressed()) {
            toggleNightVision();
            showMessage("Night Vision: " + (nightVisionEnabled ? "ON" : "OFF"));
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();
            
            // Update message display timer
            if (messageTimer > 0) {
                messageTimer--;
            }
            
            // Continuously give night vision effect (if enabled)
            if (nightVisionEnabled && mc.player != null) {
                // Give 300 tick (15 second) night vision effect, continuously refreshed
                PotionEffect nightVision = new PotionEffect(Potion.getPotionById(16), 300, 0, false, false);
                mc.player.addPotionEffect(nightVision);
            }
        }
    }

    private void toggleGamma() {
        Minecraft mc = Minecraft.getMinecraft();
        if (gammaMode == 0) {
            // Switch to 1500%
            mc.gameSettings.gammaSetting = boostedGamma;
            gammaMode = 1;
        } else {
            // Switch back to 100%
            mc.gameSettings.gammaSetting = defaultGamma;
            gammaMode = 0;
        }
        // Save settings
        mc.gameSettings.saveOptions();
    }

    private void increaseGamma() {
        Minecraft mc = Minecraft.getMinecraft();
        float newGamma = mc.gameSettings.gammaSetting + gammaStep;
        // Limit maximum value to prevent overflow
        if (newGamma > 20.0F) {
            newGamma = 20.0F;
        }
        mc.gameSettings.gammaSetting = newGamma;
        mc.gameSettings.saveOptions();
        
        // If previously in toggle mode, now switch to custom mode
        gammaMode = 2;
    }

    private void decreaseGamma() {
        Minecraft mc = Minecraft.getMinecraft();
        float newGamma = mc.gameSettings.gammaSetting - gammaStep;
        // Limit minimum value
        if (newGamma < 0.0F) {
            newGamma = 0.0F;
        }
        mc.gameSettings.gammaSetting = newGamma;
        mc.gameSettings.saveOptions();
        
        // If previously in toggle mode, now switch to custom mode
        gammaMode = 2;
    }

    private void toggleNightVision() {
        nightVisionEnabled = !nightVisionEnabled;
        Minecraft mc = Minecraft.getMinecraft();
        
        if (!nightVisionEnabled && mc.player != null) {
            // Remove effect when turning off night vision
            mc.player.removePotionEffect(Potion.getPotionById(16));
        }
    }

    private void showMessage(String message) {
        displayMessage = message;
        messageTimer = 100; // 5 seconds (20 ticks/second * 5 seconds = 100 ticks)
    }

    // Get display message
    public String getDisplayMessage() {
        return displayMessage;
    }

    // Get message timer
    public int getMessageTimer() {
        return messageTimer;
    }

    // Get night vision status
    public boolean isNightVisionEnabled() {
        return nightVisionEnabled;
    }

    // Get gamma mode
    public int getGammaMode() {
        return gammaMode;
    }

    // Get instance
    public static GammaUtilsMod getInstance() {
        return instance;
    }
}