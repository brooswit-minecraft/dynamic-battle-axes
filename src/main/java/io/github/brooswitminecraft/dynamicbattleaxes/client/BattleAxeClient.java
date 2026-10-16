package io.github.brooswitminecraft.dynamicbattleaxes.client;

import io.github.brooswitminecraft.dynamicbattleaxes.BattleAxeMaterial;
import io.github.brooswitminecraft.dynamicbattleaxes.DynamicBattleAxesMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Client-only registration keeps the runtime .vox renderer out of dedicated-server classloading. */
@EventBusSubscriber(modid = DynamicBattleAxesMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BattleAxeClient {
    private BattleAxeClient() {
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (BattleAxeMaterial material : BattleAxeMaterial.values()) {
            BattleAxeItemRenderer renderer = new BattleAxeItemRenderer(material);
            event.registerItem(new IClientItemExtensions() {
                @Override
                public BattleAxeItemRenderer getCustomRenderer() {
                    return renderer;
                }
            }, DynamicBattleAxesMod.BATTLE_AXES.get(material).get());
        }
    }
}
