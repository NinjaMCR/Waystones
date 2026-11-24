package com.ninjamcr.waystones.models;

import org.bukkit.Bukkit;
import org.bukkit.Material;

public enum WaystoneIcon {
    COMPASS(Material.COMPASS),
    CHEST(Material.CHEST),
    WHITE_BED(Material.WHITE_BED),
    ENCHANTING_TABLE(Material.ENCHANTING_TABLE),
    BREWING_STAND(Material.BREWING_STAND),
    FISHING_ROD(Material.FISHING_ROD),
    IRON_PICKAXE(Material.IRON_PICKAXE),
    DIAMOND(Material.DIAMOND),
    EMERALD(Material.EMERALD),

    WHEAT(Material.WHEAT),
    CARROT(Material.CARROT),
    POTATO(Material.POTATO),
    BEETROOT(Material.BEETROOT),
    PUMPKIN(Material.PUMPKIN),
    MELON_SLICE(Material.MELON_SLICE),
    SUGAR_CANE(Material.SUGAR_CANE),
    BAMBOO(Material.BAMBOO),
    CACTUS(Material.CACTUS),

    GRASS_BLOCK(Material.GRASS_BLOCK),
    OAK_LOG(Material.OAK_LOG),
    OAK_SAPLING(Material.OAK_SAPLING),
    COCOA_BEANS(Material.COCOA_BEANS),
    SWEET_BERRIES(Material.SWEET_BERRIES),
    GLOW_BERRIES(Material.GLOW_BERRIES),
    KELP(Material.KELP),
    RED_MUSHROOM(Material.RED_MUSHROOM),
    NETHER_WART(Material.NETHER_WART),

    EGG(Material.EGG),
    FEATHER(Material.FEATHER),
    LEATHER(Material.LEATHER),
    MILK_BUCKET(Material.MILK_BUCKET),
    WHITE_WOOL(Material.WHITE_WOOL),
    RABBIT_FOOT(Material.RABBIT_FOOT),
    INK_SAC(Material.INK_SAC),
    HONEYCOMB(Material.HONEYCOMB),
    HONEY_BOTTLE(Material.HONEY_BOTTLE),

    BONE(Material.BONE),
    STRING(Material.STRING),
    GUNPOWDER(Material.GUNPOWDER),
    SLIME_BALL(Material.SLIME_BALL),
    ENDER_PEARL(Material.ENDER_PEARL),
    BLAZE_ROD(Material.BLAZE_ROD),
    FIRE_CHARGE(Material.FIRE_CHARGE),
    IRON_INGOT(Material.IRON_INGOT),
    GOLD_INGOT(Material.GOLD_INGOT),

    WHITE_CONCRETE(Material.WHITE_CONCRETE),
    RED_CONCRETE(Material.RED_CONCRETE),
    ORANGE_CONCRETE(Material.ORANGE_CONCRETE),
    YELLOW_CONCRETE(Material.YELLOW_CONCRETE),
    LIME_CONCRETE(Material.LIME_CONCRETE),
    CYAN_CONCRETE(Material.CYAN_CONCRETE),
    BLUE_CONCRETE(Material.BLUE_CONCRETE),
    PURPLE_CONCRETE(Material.PURPLE_CONCRETE),
    BLACK_CONCRETE(Material.BLACK_CONCRETE);

    private final Material material;

    WaystoneIcon(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public static WaystoneIcon fromStorage(String value) {
        if (value != null) {
            try {
                return valueOf(value);
            } catch (IllegalArgumentException ignored) {
                Bukkit.getLogger().warning("Unknown saved icon '" + value + "'. Using COMPASS.");
            }
        }
        return COMPASS;
    }
}
