package com.customapples.util;

import com.customapples.item.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class AppMerchant implements Merchant {
    private final MerchantOffers offers = buildOffers();
    private Player tradingPlayer;

    private static MerchantOffers buildOffers() {
        MerchantOffers offers = new MerchantOffers();
        offers.add(new MerchantOffer(new ItemStack(Items.APPLE, 1), new ItemStack(Items.COAL, 4), 9999, 0, 0.05f));
        offers.add(new MerchantOffer(new ItemStack(Items.APPLE, 2), new ItemStack(Items.IRON_INGOT, 2), 9999, 0, 0.05f));
        offers.add(new MerchantOffer(new ItemStack(Items.APPLE, 16), new ItemStack(Items.GOLD_INGOT, 2), 9999, 0, 0.05f));
        offers.add(new MerchantOffer(new ItemStack(Items.APPLE, 32), new ItemStack(Items.DIAMOND, 1), 9999, 0, 0.05f));
        offers.add(new MerchantOffer(new ItemStack(Items.APPLE, 5), new ItemStack(Items.EMERALD, 1), 9999, 0, 0.05f));
        offers.add(new MerchantOffer(new ItemStack(Items.APPLE, 64), new ItemStack(Items.GOLDEN_APPLE, 1), 9999, 0, 0.05f));
        offers.add(new MerchantOffer(new ItemStack(Items.APPLE, 8), new ItemStack(ModItems.APPLE_APPLE_APPLE.get(), 3), 9999, 0, 0.05f));
        return offers;
    }

    @Override
    public void setTradingPlayer(Player player) {
        this.tradingPlayer = player;
    }

    @Override
    public Player getTradingPlayer() {
        return tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        return offers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        if (tradingPlayer != null) {
            tradingPlayer.level().playSound(null, tradingPlayer.blockPosition(),
                    SoundEvents.VILLAGER_YES, tradingPlayer.getSoundSource(), 1.0f, 1.0f);
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return tradingPlayer != null && tradingPlayer.level().isClientSide();
    }
}
