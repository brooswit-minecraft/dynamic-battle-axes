package io.github.brooswitminecraft.dynamicbattleaxes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server "I just swung at empty air with a battle-axe" notice
 * (MINECRAFT-258, review finding #2). Vanilla's own left-click pipeline
 * fires {@code AttackEntityEvent} only when the raycast actually resolves
 * to an entity; a swing that hits nothing is never reported to the server
 * on its own ({@code PlayerInteractEvent.LeftClickEmpty} is client-side
 * only), so without this payload the 180-degree arc would never evaluate
 * for a swing that happened not to be centered on an entity.
 *
 * <p>Empty payload, same as {@code WhipHoldPingPayload}: the sender and the
 * current server tick are all {@link BattleAxeCombat#onAirSwing} needs.
 * The server independently recomputes the attacker's position, look
 * direction and live reach -- it never trusts anything from the client
 * beyond "a swing happened," so this cannot be used to claim a hit the
 * server's own geometry check would not also find.
 */
public record BattleAxeAirSwingPayload() implements CustomPacketPayload {
    public static final Type<BattleAxeAirSwingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(DynamicBattleAxesMod.MODID, "battle_axe_air_swing"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BattleAxeAirSwingPayload> STREAM_CODEC =
            StreamCodec.unit(new BattleAxeAirSwingPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
