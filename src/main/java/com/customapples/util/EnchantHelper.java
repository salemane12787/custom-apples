package com.customapples.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public final class EnchantHelper {
    private EnchantHelper() {}

  /** Insane enchant levels matching the xNestorio video (Knockback 7, Loyalty 8, etc.). */
  private static final Map<String, Integer> INSANE_LEVELS = Map.ofEntries(
      Map.entry("sharpness", 10),
      Map.entry("smite", 10),
      Map.entry("bane_of_arthropods", 10),
      Map.entry("knockback", 7),
      Map.entry("fire_aspect", 5),
      Map.entry("looting", 5),
      Map.entry("sweeping", 5),
      Map.entry("efficiency", 10),
      Map.entry("silk_touch", 1),
      Map.entry("unbreaking", 10),
      Map.entry("fortune", 5),
      Map.entry("power", 10),
      Map.entry("punch", 5),
      Map.entry("flame", 1),
      Map.entry("infinity", 1),
      Map.entry("luck_of_the_sea", 5),
      Map.entry("lure", 5),
      Map.entry("loyalty", 8),
      Map.entry("impaling", 8),
      Map.entry("channeling", 1),
      Map.entry("riptide", 5),
      Map.entry("multishot", 1),
      Map.entry("quick_charge", 5),
      Map.entry("piercing", 5),
      Map.entry("mending", 1),
      Map.entry("protection", 10),
      Map.entry("fire_protection", 10),
      Map.entry("blast_protection", 10),
      Map.entry("projectile_protection", 10),
      Map.entry("feather_falling", 10),
      Map.entry("thorns", 5),
      Map.entry("depth_strider", 5),
      Map.entry("frost_walker", 5),
      Map.entry("soul_speed", 5),
      Map.entry("swift_sneak", 5)
  );

    public static void maxEnchantInventory(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                maxEnchant(stack);
            }
        }
    }

    private static void maxEnchant(ItemStack stack) {
        Map<Enchantment, Integer> enchants = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
            if (!enchantment.canEnchant(stack)) {
                continue;
            }
            String id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment).getPath();
            int level = INSANE_LEVELS.getOrDefault(id, Math.min(enchantment.getMaxLevel() + 3, 10));
            if ("silk_touch".equals(id) || "infinity".equals(id) || "mending".equals(id)
                    || "channeling".equals(id) || "multishot".equals(id)) {
                level = 1;
            }
            enchants.put(enchantment, level);
        }
        EnchantmentHelper.setEnchantments(enchants, stack);
    }
}
