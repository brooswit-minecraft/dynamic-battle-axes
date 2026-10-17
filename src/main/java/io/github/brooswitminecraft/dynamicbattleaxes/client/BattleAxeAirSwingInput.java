package io.github.brooswitminecraft.dynamicbattleaxes.client;

import io.github.brooswitminecraft.dynamicbattleaxes.BattleAxeAirSwingPayload;
import io.github.brooswitminecraft.dynamicbattleaxes.BattleAxeItem;
import io.github.brooswitminecraft.dynamicbattleaxes.DynamicBattleAxesMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client-only half of {@link BattleAxeAirSwingPayload} (MINECRAFT-258,
 * review finding #2): {@code PlayerInteractEvent.LeftClickEmpty} only ever
 * fires on the client (vanilla never reports a plain empty-air swing to the
 * server on its own), so this is the one place that can observe "the local
 * player just swung at nothing" at all. Client-only registration keeps this
 * off the dedicated-server classpath, same as {@code BattleAxeClient}.
 */
@EventBusSubscriber(modid = DynamicBattleAxesMod.MODID, value = Dist.CLIENT)
public final class BattleAxeAirSwingInput {
    private BattleAxeAirSwingInput() {
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getEntity().getMainHandItem().getItem() instanceof BattleAxeItem) {
            PacketDistributor.sendToServer(new BattleAxeAirSwingPayload());
        }
    }
}
