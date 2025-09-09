package dev.tucanu.wordle.item.custom;

import dev.tucanu.wordle.item.custom.wordle.WordleLogic;
import dev.tucanu.wordle.item.custom.wordle.WordleMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class Wordle extends Item {

    public Wordle(Properties properties) {
        super(properties);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Ensure daily word exists in player's persistent data
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
                NetworkHooks.openScreen(sp, new SimpleMenuProvider(
                        (windowId, inv, p) -> new WordleMenu(windowId, inv),
                        Component.literal("Wordle")
                ));
        }

        player.getCooldowns().addCooldown(stack.getItem(), 50);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
