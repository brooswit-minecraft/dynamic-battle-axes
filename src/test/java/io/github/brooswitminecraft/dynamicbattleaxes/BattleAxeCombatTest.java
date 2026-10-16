package io.github.brooswitminecraft.dynamicbattleaxes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
