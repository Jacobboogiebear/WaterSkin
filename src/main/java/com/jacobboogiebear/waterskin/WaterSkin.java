package com.jacobboogiebear.waterskin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import toughasnails.config.ThirstConfig;
import toughasnails.config.ThirstConfig.DrinkEntry;

import java.util.List;


@Mod("waterskin")
public class WaterSkin {
    private static final Logger LOGGER = LogManager.getLogger();

    public WaterSkin() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void itemToolTip(ItemTooltipEvent event) {
        if (event.isCanceled()) {
            return;
        }
        ItemStack itemStack = event.getItemStack();
        if (!shouldShowThirst(itemStack)) {
            return;
        }
        List<ITextComponent> tooltip = event.getToolTip();
        StringBuilder builder = new StringBuilder("");
        DrinkEntry drinkEntry = ThirstConfig.getDrinkEntry(itemStack.getItem().getRegistryName());
        for (int i = 2; i <= drinkEntry.getHydration() * 10; i += 2) {
            builder.append(" ");
        }
        tooltip.add(ITextComponent.getTextComponentOrEmpty(builder.toString()));
    }

    @SubscribeEvent
    public void renderToolTip(RenderTooltipEvent.PostText event) {
        if (event.isCanceled()) {
            return;
        }
        ItemStack itemStack = event.getStack();
        Minecraft mc = Minecraft.getInstance();
        Screen gui = mc.currentScreen;
        if (gui == null) {
            return;
        }

        int tX = event.getX();
        int tY = event.getY();

        MatrixStack stack = event.getMatrixStack();
        if (ThirstConfig.getDrinkEntry(itemStack.getItem().getRegistryName()) != null) {
            OverlayRenderThirst(mc, gui, stack, tX, tY, event, (int) (ThirstConfig.getDrinkEntry(itemStack.getItem().getRegistryName()).getHydration() * 10));
        }
    }

    public void OverlayRenderThirst(Minecraft mc, Screen gui, MatrixStack stack, int tX, int tY, RenderTooltipEvent.PostText event, int count) {
        List<? extends ITextProperties> lines = event.getLines();
        int linesSize = lines.size();
        int lineHeight = event.getHeight() / linesSize;
        int lineIndex = 0;
        int offset = 0;
        StringBuilder built = new StringBuilder("");
        for (int i = 2; i <= count; i += 2) {
            built.append(" ");
        }
        for (int i = 0; i < linesSize; i++) {
            if (lines.get(i).getString().contentEquals(built.toString())) {
                lineIndex = i;
            }
        }

        offset = lineIndex * lineHeight;

        double whole = Math.floor(count / 2);
        int half = count % 2;
        if (half > 0) {
            OverlayRenderThirstHalf(mc, gui, stack, tX, tY, offset, 1);
        }
        for (int i = 0; i < whole; i++) {
            if (half > 0) {
                OverlayRenderThirstWhole(mc, gui, stack, tX, tY, offset, i + 2);
            } else {
                OverlayRenderThirstWhole(mc, gui, stack, tX, tY, offset, i + 1);
            }
        }

    }

    public void OverlayRenderThirstWhole(Minecraft mc, Screen gui, MatrixStack stack, int tX, int tY, int offset, int index) {
        mc.getTextureManager().bindTexture(new ResourceLocation("waterskin", "textures/filled_drop.png"));
        gui.blit(stack, tX - 2 + (index - 2) * 6 + 6, tY + offset, 0, 0, 9, 9, 9, 9);
    }

    public void OverlayRenderThirstHalf(Minecraft mc, Screen gui, MatrixStack stack, int tX, int tY, int offset, int index) {
        mc.getTextureManager().bindTexture(new ResourceLocation("waterskin", "textures/half_drop.png"));
        gui.blit(stack, tX - 2 + (index - 2) * 6 + 6, tY + offset, 0, 0, 9, 9, 9, 9);
    }

    public boolean shouldShowThirst(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        if (ThirstConfig.getDrinkEntry(itemStack.getItem().getRegistryName()) == null) {
            return false;
        }
        return true;
    }

}
