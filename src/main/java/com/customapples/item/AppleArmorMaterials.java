package com.customapples.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public final class AppleArmorMaterials {
    /**
     * One material for boots + chestplate + leggings — Minecraft uses {@code apple_layer_1.png}
     * (chest + boots) and {@code apple_layer_2.png} (leggings) for the whole set, not per-piece files.
     */
    public static final ArmorMaterial APPLE = new ArmorMaterial() {
        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return switch (type) {
                case BOOTS -> 5 * 13;
                case LEGGINGS -> 6 * 15;
                case CHESTPLATE -> 8 * 16;
                case HELMET -> 0;
            };
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return switch (type) {
                case BOOTS -> 1;
                case CHESTPLATE -> 2;
                case LEGGINGS -> 3;
                case HELMET -> 0;
            };
        }

        @Override
        public int getEnchantmentValue() {
            return 12;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_LEATHER;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "customapples:apple";
        }

        @Override
        public float getToughness() {
            return 0.0f;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.0f;
        }
    };

    private AppleArmorMaterials() {
    }
}
