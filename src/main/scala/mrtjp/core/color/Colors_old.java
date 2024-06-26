/*
 * Copyright (c) 2014. Created by MrTJP. All rights reserved.
 */
package mrtjp.core.color;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourRGBA;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Deprecated
public enum Colors_old {

    WHITE(0xFFFFFF),
    ORANGE(0xC06300),
    MAGENTA(0xB51AB5),
    LIGHT_BLUE(0x6F84F1),
    YELLOW(0xBFBF00),
    LIME(0x6BF100),
    PINK(0xF14675),
    GREY(0x535353),
    LIGHT_GREY(0x939393),
    CYAN(0x008787),
    PURPLE(0x5E00C0),
    BLUE(0x1313C0),
    BROWN(0x4F2700),
    GREEN(0x088700),
    RED(0xA20F06),
    BLACK(0x1F1F1F);

    public static final Colors_old[] VALID_COLORS = values();
    private static final String[] dyeDictionary = { "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue",
            "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue",
            "dyeMagenta", "dyeOrange", "dyeWhite" };

    private Colors_old(int rgb) {
        name = name().substring(0, 1) + name().substring(1).toLowerCase();
        this.rgb = rgb;
        rgba = rgb << 8 | 0xFF;
        argb = 0xFF000000 | rgb;
        c = new ColourRGBA(rgb << 8 | 0xFF);
    }

    public int dyeId() {
        return 15 - ordinal();
    }

    public int woolId() {
        return ordinal();
    }

    public String getOreDict() {
        return dyeDictionary[dyeId()];
    }

    public final String name;
    public final Colour c;
    public final int rgb;
    public final int rgba;
    public final int argb;

    public static Colors_old get(int i) {
        if (i > 15) return BLACK;
        if (i < 0) return WHITE;
        return VALID_COLORS[i];
    }

    public ItemStack getDye() {
        return new ItemStack(Items.dye, 1, dyeId());
    }

    public float rF() {
        return (rgb >> 16 & 255) / 255.0F;
    }

    public float gF() {
        return (rgb >> 8 & 255) / 255.0F;
    }

    public float bF() {
        return (rgb & 255) / 255.0F;
    }

    @SideOnly(Side.CLIENT)
    public void setGL11Color(float alpha) {
        float r = (rgb >> 16 & 255) / 255.0F;
        float g = (rgb >> 8 & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;
        GL11.glColor4f(r, g, b, alpha);
    }
}
