package dev.tucanu.wordle;

import com.mojang.brigadier.Command;
import com.mojang.logging.LogUtils;
import dev.tucanu.wordle.item.ModItems;
import dev.tucanu.wordle.item.custom.wordle.WordleLogic;
import dev.tucanu.wordle.item.custom.wordle.WordleScreen;
import dev.tucanu.wordle.util.ModMenus;
import dev.tucanu.wordle.util.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(Wordle.MOD_ID)
public class Wordle {
    public static final String MOD_ID = "wordle";
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final Logger LOGGER = LogUtils.getLogger();



    public Wordle() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
        ModNetwork.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {

        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.WORDLE_MENU.get(), WordleScreen::new);
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.WORDLE);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Initializing client setup for {}", MOD_ID);
            LOGGER.debug("Logged in as: {}", Minecraft.getInstance().getUser().getName());
        }

    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("newwordle") // /newwordle
                        .requires(src -> src.hasPermission(2)) // only OPs
                        .executes(context -> {
                            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                WordleLogic.NewWord(player);
                                player.sendSystemMessage(Component.literal("Word changed to " + WordleLogic.dailyWord));
                                return Command.SINGLE_SUCCESS;
                            } else {
                                context.getSource().sendFailure(Component.literal("Only players can run this command!"));
                                return 0;
                            }
                        })
        );
    }
}