package dev.tucanu.wordle.util;

import dev.tucanu.wordle.Wordle;
import dev.tucanu.wordle.item.custom.wordle.WordleMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Wordle.MOD_ID);

    public static final RegistryObject<MenuType<WordleMenu>> WORDLE_MENU =
            MENUS.register("wordle_menu",
                    () -> IForgeMenuType.create((windowId, inv, data) -> new WordleMenu(windowId, inv)));

    public static void register(IEventBus eventBus) {MENUS.register(eventBus);
    }
}