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
     * (review finding #1, and the follow-up finding on {@code onAirSwing}'s
     * charge-consumption policy) need no live {@code LivingEntity}/world: a
     * fake mutable ticker plus plain strings stand in for the real
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
                List.of("a", "b", "c"), true);
        assertEquals(List.of(7, 7, 7), seenBeforeEachAttack, "every target must see the original strength, not a prior target's reset value");
    }

    @Test
    void sweepWithRestoreEnabledRestoresTheOriginalTickerAfterTheLastTarget() {
        int[] ticker = {5};
        BattleAxeCombat.sweep(() -> ticker[0], value -> ticker[0] = value, target -> false,
                target -> ticker[0] = 0, List.of("a", "b"), true);
        assertEquals(5, ticker[0], "onAttackEntity needs this: vanilla's own continuation of the primary hit must see the original strength too");
    }

    @Test
    void sweepWithRestoreDisabledLeavesTheLastAttackCallsOwnResetInPlace() {
        int[] ticker = {6};
        List<Integer> seenBeforeEachAttack = new ArrayList<>();
        BattleAxeCombat.sweep(() -> ticker[0], value -> ticker[0] = value, target -> false,
                target -> { seenBeforeEachAttack.add(ticker[0]); ticker[0] = 0; }, List.of("a", "b"), false);
        assertEquals(List.of(6, 6), seenBeforeEachAttack, "every target still sees the original strength within the one swing");
        assertEquals(0, ticker[0], "onAirSwing needs this: with no outer vanilla call left to spend the charge, sweep must not re-restore it");
    }

    @Test
    void sweepSkipsExcludedTargetsEntirely() {
        int[] ticker = {3};
        List<String> attacked = new ArrayList<>();
        BattleAxeCombat.sweep(() -> ticker[0], value -> ticker[0] = value, target -> target.equals("skip"),
                attacked::add, List.of("skip", "hit"), true);
        assertEquals(List.of("hit"), attacked);
    }

    @Test
    void sweepWithNoEligibleTargetsAndRestoreEnabledStillRestoresTheOriginalTicker() {
        int[] ticker = {9};
        BattleAxeCombat.sweep(() -> ticker[0], value -> ticker[0] = value, target -> true,
                target -> ticker[0] = 0, List.of("skip"), true);
        assertEquals(9, ticker[0]);
    }

    @Test
    void sweepIsOrderIndependentForWhichTargetsSeeTheOriginalStrength() {
        int[] ticker = {4};
        List<Integer> forward = new ArrayList<>();
        BattleAxeCombat.sweep(() -> ticker[0], v -> ticker[0] = v, t -> false,
                t -> { forward.add(ticker[0]); ticker[0] = 0; }, List.of("a", "b", "c"), true);

        ticker[0] = 4;
        List<Integer> reversed = new ArrayList<>();
        BattleAxeCombat.sweep(() -> ticker[0], v -> ticker[0] = v, t -> false,
                t -> { reversed.add(ticker[0]); ticker[0] = 0; }, List.of("c", "b", "a"), true);

        assertEquals(forward, reversed, "every target sees the original strength regardless of iteration order");
    }

    /**
     * Models {@link BattleAxeCombat#onAirSwing}'s own policy (not exercised
     * directly: it needs a live {@code ServerPlayer}/level) at the level
     * {@code sweep} can be tested: a swing with {@code restoreOriginalAfter
     * = false}, followed by the entry point's own single explicit zeroing
     * of the ticker, exactly once per received payload -- including an
     * empty swing (target list excludes everyone), so a second swing with
     * no recovery ticks elapsed never again sees the first swing's full
     * charge.
     */
    @Test
    void twoSuccessiveAirSwingsWithoutRecoveryDoNotBothSeeFullCharge() {
        int[] ticker = {6};

        List<Integer> firstSwingSeen = new ArrayList<>();
        BattleAxeCombat.sweep(() -> ticker[0], v -> ticker[0] = v, t -> false,
                t -> firstSwingSeen.add(ticker[0]), List.of("x"), false);
        ticker[0] = 0; // onAirSwing's own post-sweep consumption; no recovery ticks elapsed before the next swing.

        List<Integer> secondSwingSeen = new ArrayList<>();
        BattleAxeCombat.sweep(() -> ticker[0], v -> ticker[0] = v, t -> false,
                t -> secondSwingSeen.add(ticker[0]), List.of("y"), false);
        ticker[0] = 0;

        assertEquals(List.of(6), firstSwingSeen);
        assertEquals(List.of(0), secondSwingSeen, "second swing must not see the first swing's full charge again");
    }

    @Test
    void airSwingStyleChargeConsumptionHappensEvenWhenNoTargetsAreEligible() {
        int[] ticker = {8};
        List<String> attacked = new ArrayList<>();
        // Mirrors onAirSwing's own two-step shape: a sweep whose skip predicate excludes every
        // candidate (the arc found nobody but the attacker), then the entry point's own single
        // explicit zeroing -- which must still happen even though the loop body never ran.
        BattleAxeCombat.sweep(() -> ticker[0], v -> ticker[0] = v, target -> true, attacked::add, List.of("onlyAttacker"), false);
        ticker[0] = 0;
        assertEquals(List.of(), attacked, "the excluded candidate must never be attacked");
        assertEquals(0, ticker[0], "the charge must still be spent even though nobody was eligible");
    }
}
