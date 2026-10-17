package io.github.brooswitminecraft.dynamicbattleaxes;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * The battle-axe's defining behaviour (MINECRAFT-258): every attack hits
 * everything in a 180-degree forward arc within reach, not just the single
 * entity vanilla's hit-scan resolved -- whether that attack landed on an
 * entity ({@link #onAttackEntity}) or swung at empty air
 * ({@link #onAirSwing}, server-side handler for {@link BattleAxeAirSwingPayload}).
 *
 * <p>Friendly-fire/PvP decision (see this ticket's decision comment): no new
 * protection bypass. Each target is attacked by literally re-invoking
 * {@link Player#attack(Entity)} -- the exact same vanilla path (and the
 * exact same {@link AttackEntityEvent}) a direct hit on that target would
 * have gone through, so every existing check (the {@code pvp} game rule,
 * scoreboard {@code friendlyFire}, invulnerability, other mods' listeners)
 * applies identically. {@link #inArcSweep} exists only to stop that
 * re-invocation from recursively sweeping again for each secondary target;
 * it is not a protection check of its own.
 */
@EventBusSubscriber(modid = DynamicBattleAxesMod.MODID)
public final class BattleAxeCombat {
    /**
     * Prevents a secondary {@code Player#attack} call below from itself
     * triggering another arc sweep (that call fires the very same
     * {@link AttackEntityEvent} this class listens to). Not thread-shared
     * state: both entry points always run on the server tick thread that is
     * processing the attacking player's input/packet.
     */
    private static boolean inArcSweep = false;

    /**
     * {@code LivingEntity.attackStrengthTicker} (protected, declared on
     * {@code LivingEntity} -- not {@code Player}): reflection is the only
     * way to read/restore it from outside that class hierarchy. See
     * {@link #sweep} for why this needs restoring at all.
     */
    private static final Field ATTACK_STRENGTH_TICKER;

    static {
        try {
            ATTACK_STRENGTH_TICKER = LivingEntity.class.getDeclaredField("attackStrengthTicker");
            ATTACK_STRENGTH_TICKER.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private BattleAxeCombat() {
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (inArcSweep) {
            return;
        }
        Player attacker = event.getEntity();
        if (!(attacker.getMainHandItem().getItem() instanceof BattleAxeItem)) {
            return;
        }
        Entity primaryTarget = event.getTarget();
        List<LivingEntity> arcTargets = entitiesInForwardArc(attacker, reach(attacker));
        if (arcTargets.isEmpty()) {
            return;
        }
        inArcSweep = true;
        try {
            sweep(() -> attackStrengthTicker(attacker), value -> setAttackStrengthTicker(attacker, value),
                    target -> target == primaryTarget || target == attacker, attacker::attack, arcTargets);
        } finally {
            inArcSweep = false;
        }
    }

    /**
     * Server-side handler for {@link BattleAxeAirSwingPayload}: a swing
     * vanilla's own left-click pipeline resolves as "no entity/block
     * targeted" never fires {@link AttackEntityEvent} at all (the server is
     * never told about a plain empty-air swing otherwise), so without this
     * second entry point the arc would only ever trigger when the player
     * happened to be looking directly at one of the entities it should hit.
     * Mutually exclusive with {@link #onAttackEntity} by construction:
     * vanilla's pipeline fires exactly one of "found an entity" (which
     * sends no payload; it fires {@code AttackEntityEvent} instead, client
     * and server) or "found nothing" (this payload) for a given swing,
     * never both -- so no shared re-entrancy guard between the two is
     * needed, only {@link #inArcSweep} against this method's own
     * {@code attack} calls recursing into {@link #onAttackEntity}.
     */
    static void onAirSwing(BattleAxeAirSwingPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer attacker)) {
            return;
        }
        if (!(attacker.getMainHandItem().getItem() instanceof BattleAxeItem)) {
            return;
        }
        List<LivingEntity> arcTargets = entitiesInForwardArc(attacker, reach(attacker));
        if (arcTargets.isEmpty()) {
            return;
        }
        inArcSweep = true;
        try {
            sweep(() -> attackStrengthTicker(attacker), value -> setAttackStrengthTicker(attacker, value),
                    target -> target == attacker, attacker::attack, arcTargets);
        } finally {
            inArcSweep = false;
        }
    }

    /**
     * Attacks every target not excluded by {@code shouldSkip}, forcing
     * {@code setTicker} back to the value {@code getTicker} returned before
     * the FIRST call before every individual {@code attack} -- and once
     * more after the loop. Without this, {@code Player#attack} resets the
     * real attack-strength ticker to 0 at the end of every invocation
     * (vanilla always does this, hit or miss), so the first secondary
     * consumes the whole swing's charge and every target after it --
     * including, for {@link #onAttackEntity}, the directly-clicked primary
     * target, whose own damage vanilla computes from whatever the ticker
     * holds right after this event handler returns -- would otherwise
     * compute weaker damage than the swing that was actually thrown,
     * depending on iteration order. Generic and side-effect-free apart from
     * the supplied callbacks, so it is unit-testable with fake targets and
     * no live entity/world ({@link BattleAxeCombatTest}).
     */
    static <T> void sweep(IntSupplier getTicker, IntConsumer setTicker, Predicate<T> shouldSkip,
            Consumer<T> attack, List<T> targets) {
        int original = getTicker.getAsInt();
        for (T target : targets) {
            if (shouldSkip.test(target)) {
                continue;
            }
            setTicker.accept(original);
            attack.accept(target);
        }
        setTicker.accept(original);
    }

    private static int attackStrengthTicker(LivingEntity entity) {
        try {
            return ATTACK_STRENGTH_TICKER.getInt(entity);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to read attackStrengthTicker", e);
        }
    }

    private static void setAttackStrengthTicker(LivingEntity entity, int value) {
        try {
            ATTACK_STRENGTH_TICKER.setInt(entity, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set attackStrengthTicker", e);
        }
    }

    /**
     * The attacker's actual, live {@code ENTITY_INTERACTION_RANGE}
     * attribute value (which already includes {@link BattleAxeItem}'s own
     * reach modifier once the axe is equipped) -- not a hardcoded constant,
     * so creative mode or any other mod/effect that changes reach keeps the
     * arc's sweep radius agreeing with what vanilla used to find/validate
     * the primary target in the first place.
     */
    private static double reach(Player attacker) {
        return attacker.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
    }

    /**
     * Every living entity (other than {@code attacker}) within
     * {@code reach} blocks of {@code attacker}'s eye position and within
     * the forward 180-degree horizontal arc of {@code attacker}'s look
     * direction -- exactly the "everything in a 180-degree forward arc
     * within reach" the handoff describes. A target exactly at the +/-90
     * degree edge (dot product 0) counts as in the arc.
     */
    static List<LivingEntity> entitiesInForwardArc(Player attacker, double reach) {
        Vec3 origin = attacker.getEyePosition();
        Vec3 flatLook = flattenAndNormalize(attacker.getLookAngle());
        AABB searchBox = new AABB(origin, origin).inflate(reach);
        return attacker.level().getEntitiesOfClass(LivingEntity.class, searchBox, candidate ->
                candidate.isAlive()
                        && isWithinForwardArc(origin, flatLook, reach, candidate.position().add(0.0, candidate.getBbHeight() / 2.0, 0.0)));
    }

    /** Horizontal look direction, normalized; falls back to {@code +Z} when looking straight up/down. */
    static Vec3 flattenAndNormalize(Vec3 look) {
        Vec3 flat = new Vec3(look.x(), 0.0, look.z());
        return flat.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : flat.normalize();
    }

    /**
     * Pure geometry predicate (no world/entity access) for "within
     * {@code reach} of {@code origin} and within the forward 180-degree
     * horizontal arc of {@code flatLook}" -- the exact rule a point exactly
     * at the +/-90 degree edge (dot product 0) still satisfies.
     * {@code flatLook} must already be flattened (y == 0) and normalized;
     * see {@link #flattenAndNormalize}.
     */
    static boolean isWithinForwardArc(Vec3 origin, Vec3 flatLook, double reach, Vec3 target) {
        Vec3 toTarget = target.subtract(origin);
        if (toTarget.lengthSqr() > reach * reach) {
            return false;
        }
        Vec3 flatToTarget = new Vec3(toTarget.x(), 0.0, toTarget.z());
        if (flatToTarget.lengthSqr() < 1.0E-6) {
            // Directly above/below the attacker's eye line: treat as in the arc rather than undefined.
            return true;
        }
        return flatLook.dot(flatToTarget.normalize()) >= 0.0;
    }
}
