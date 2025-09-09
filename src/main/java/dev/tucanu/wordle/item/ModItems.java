package dev.tucanu.wordle.item;

import dev.tucanu.wordle.Wordle;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final Rarity MegaRarity = Rarity.create("MEGA", ChatFormatting.GOLD);;

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Wordle.MOD_ID);

    public static final RegistryObject<Item> WORDLE = ITEMS.register("wordle",
            ()-> new dev.tucanu.wordle.item.custom.Wordle(new Item.Properties()
                    .rarity(MegaRarity)
                    .stacksTo(1)
                    ));
    
    
    public static void register(IEventBus eventBus) {ITEMS.register(eventBus);
    }
}
