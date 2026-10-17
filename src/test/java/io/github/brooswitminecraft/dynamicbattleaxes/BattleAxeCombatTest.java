package io.github.brooswitminecraft.dynamicbattleaxes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.world.phys.Vec3;

/**
 * Pure checks on {@link BattleAxeCombat}'s "180-degree forward arc within
 * reach" geometry; no game bootstrap required ({@link Vec3} has none of its
 * own). This is the behaviour the handoff calls the battle-axe's defining
 * trait, so it is covered directly rather than only through the attribute
 * wiring in {@link DynamicBattleAxesModTest}.
 */
class BattleAxeCombatTest {

    private static final Vec3 ORIGIN = new Vec3(0.0, 0.0, 0.0);
    private static final Vec3 FORWARD_Z = new Vec3(0.0, 0.0, 1.0);

    @Test
    void targetDirectlyAheadWithinReachIsIncluded() {
        assertTrue(BattleAxeCombat.isWithinForwardArc(ORIGIN, FORWARD_Z, 3.5, new Vec3(0.0, 0.0, 2.0)));
    }

    @Test
    void targetDirectlyBehindIsExcluded() {
        assertFalse(BattleAxeCombat.isWithinForwardArc(ORIGIN, FORWARD_Z, 3.5, new Vec3(0.0, 0.0, -2.0)));
    }

    @Test
    void targetBeyondReachIsExcludedEvenDirectlyAhead() {
        assertFalse(BattleAxeCombat.isWithinForwardArc(ORIGIN, FORWARD_Z, 3.5, new Vec3(0.0, 0.0, 4.0)));
    }

    @Test
    void targetExactlyAtTheNinetyDegreeEdgeIsIncluded() {
        // Straight to the attacker's right, perpendicular to forward: dot product is exactly 0.
        assertTrue(BattleAxeCombat.isWithinForwardArc(ORIGIN, FORWARD_Z, 3.5, new Vec3(2.0, 0.0, 0.0)));
    }

    @Test
    void targetJustPastTheNinetyDegreeEdgeIsExcluded() {
        assertFalse(BattleAxeCombat.isWithinForwardArc(ORIGIN, FORWARD_Z, 3.5, new Vec3(2.0, 0.0, -0.01)));
    }

    @Test
    void targetDirectlyAboveTheAttackerIsIncluded() {
        // No horizontal component to compare against the look direction: defined as in-arc, not excluded.
        assertTrue(BattleAxeCombat.isWithinForwardArc(ORIGIN, FORWARD_Z, 3.5, new Vec3(0.0, 2.0, 0.0)));
    }

    @Test
    void flattenAndNormalizeDropsVerticalComponentAndNormalizes() {
        Vec3 flat = BattleAxeCombat.flattenAndNormalize(new Vec3(0.0, 5.0, 3.0));
        assertTrue(Math.abs(flat.y()) < 1.0E-9, "vertical component must be dropped");
        assertTrue(Math.abs(flat.length() - 1.0) < 1.0E-9, "must be normalized");
    }

    @Test
    void flattenAndNormalizeFallsBackToPlusZWhenLookingStraightUp() {
        Vec3 flat = BattleAxeCombat.flattenAndNormalize(new Vec3(0.0, 1.0, 0.0));
        assertTrue(flat.equals(FORWARD_Z));
    }

    /**
     * {@link BattleAxeCombat#sweep} is generic precisely so these cases
     * (review finding #1) need no live {@code LivingEntity}/world: a fake
     * mutable ticker plus plain strings stand in for the real
     * attack-strength field and targets.
     */
    @Test
    void sweepForcesTheOriginalTickerBeforeEveryTarget() {
        int[] ticker = {7};
        List<Integer> seenBeforeEachAttack = new ArrayList<>();
        BattleAxeCombat.sweep(() -> ticker[0], value -> ticker[0] = value, target -> false,
                target -> {
                    // Simulate Player#attack always resetting the ticker to 0 by the time it returns.
                    seenBeforeEachAttack.add(ticker[0]);
                    ticker[0] = 0;
                },
                List.of("a", "b", "c"));
        assertEquals(List.of(7, 7, 7), seenBeforeEachAttack, "every target must see the original strength, not a prior target's reset value");
    }

    @Test
    void sweepRestoresTheOriginalTickerAfterTheLastTarget() {
        int[] ticker = {5};
        BattleAxeCombat.sweep(() -> ticker[0], value -> ticker[0] = value, target -> false,
                target -> ticker[0] = 0, List.of("a", "b"));
        assertEquals(5, ticker[0], "vanilla's own continuation of the primary hit must see the original strength too");
    }

    @Test
    void sweepSkipsExcludedTargetsEntirely() {
        int[] ticker = {3};
        List<String> attacked = new ArrayList<>();
        BattleAxeCombat.sweep(() -> ticker[0], value -> ticker[0] = value, target -> target.equals("skip"),
                attacked::add, List.of("skip", "hit"));
        assertEquals(List.of("hit"), attacked);
    }

    @Test
    void sweepWithNoEligibleTargetsStillRestoresTheOriginalTicker() {
        int[] ticker = {9};
        BattleAxeCombat.sweep(() -> ticker[0], value -> ticker[0] = value, target -> true,
                target -> ticker[0] = 0, List.of("skip"));
        assertEquals(9, ticker[0]);
    }

    @Test
    void sweepIsOrderIndependentForWhichTargetsSeeTheOriginalStrength() {
        int[] ticker = {4};
        List<Integer> forward = new ArrayList<>();
        BattleAxeCombat.sweep(() -> ticker[0], v -> ticker[0] = v, t -> false,
                t -> { forward.add(ticker[0]); ticker[0] = 0; }, List.of("a", "b", "c"));

        ticker[0] = 4;
        List<Integer> reversed = new ArrayList<>();
        BattleAxeCombat.sweep(() -> ticker[0], v -> ticker[0] = v, t -> false,
                t -> { reversed.add(ticker[0]); ticker[0] = 0; }, List.of("c", "b", "a"));

        assertEquals(forward, reversed, "every target sees the original strength regardless of iteration order");
    }
}
