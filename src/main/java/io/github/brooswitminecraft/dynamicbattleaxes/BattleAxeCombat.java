package io.github.brooswitminecraft.dynamicbattleaxes;

import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * The battle-axe's defining behaviour (MINECRAFT-258): every attack hits
 * everything in a 180-degree forward arc within reach, not just the single
 * entity vanilla's hit-scan resolved.
 *
 * <p>Friendly-fire/PvP decision (see this ticket's decision comment): no new
 * protection bypass. Each secondary target is attacked by literally
 * re-invoking {@link Player#attack(Entity)} -- the exact same vanilla path
 * (and the exact same {@link AttackEntityEvent}) a direct hit on that target
 * would have gone through, so every existing check (the {@code pvp} game
 * rule, scoreboard {@code friendlyFire}, invulnerability, other mods'
 * listeners) applies identically. {@link #IN_ARC_SWEEP} exists only to stop
 * that re-invocation from recursively sweeping again for each secondary
 * target; it is not a protection check of its own.
 */
@EventBusSubscriber(modid = DynamicBattleAxesMod.MODID)
public final class BattleAxeCombat {
    /**
     * Prevents the secondary {@code Player#attack} calls below from
     * themselves triggering another arc sweep (that event is the same
     * {@link AttackEntityEvent} this class listens to). Not thread-shared
     * state: {@link AttackEntityEvent} always fires on the server tick
     * thread that is processing the attacking player's input.
     */
    private static boolean inArcSweep = false;

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
        List<LivingEntity> arcTargets = entitiesInForwardArc(attacker, BattleAxeItem.reach());
        if (arcTargets.isEmpty()) {
            return;
        }
        inArcSweep = true;
        try {
            for (LivingEntity secondary : arcTargets) {
                if (secondary == primaryTarget || secondary == attacker) {
                    continue;
                }
                attacker.attack(secondary);
            }
        } finally {
            inArcSweep = false;
        }
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
