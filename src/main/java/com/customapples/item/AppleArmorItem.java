package com.customapples.item;

import com.customapples.progression.ProgressionManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class AppleArmorItem extends ArmorItem {
    private static final UUID ARMOR_UUID = UUID.fromString("c0a0a0a0-1111-2222-3333-444455556666");
    private final int bonusArmor;

    public AppleArmorItem(ArmorMaterial material, Type type, Properties properties, int bonusArmor) {
        super(material, type, properties);
        this.bonusArmor = bonusArmor;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> map =
                HashMultimap.create(super.getDefaultAttributeModifiers(slot));
        if (slot == getEquipmentSlot()) {
            map.removeAll(Attributes.ARMOR);
            map.put(Attributes.ARMOR, new AttributeModifier(
                    ARMOR_UUID, "Apple armor", bonusArmor, AttributeModifier.Operation.ADDITION));
        }
        return map;
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {
            ProgressionManager.unlockForItem(player, stack.getItem());
        }
    }
}
