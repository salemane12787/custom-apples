package com.customapples.item;

import com.customapples.progression.ProgressionManager;
import com.customapples.progression.UnlockStep;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LetterAItem extends Item {
    private static final String LETTER_COUNT_KEY = "CustomApplesLetterCount";
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public LetterAItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            int count = player.getPersistentData().getInt(LETTER_COUNT_KEY);
            char letter = ALPHABET.charAt(count % ALPHABET.length());
            player.sendSystemMessage(Component.literal(String.valueOf(letter)));
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS,
                    0.8f, 0.8f + player.getRandom().nextFloat() * 0.4f);
            player.getPersistentData().putInt(LETTER_COUNT_KEY, count + 1);

            if (count == 0) {
                ProgressionManager.onLetterAUsed(player);
                ProgressionManager.unlockStep(player, UnlockStep.APPLE_AXE);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
