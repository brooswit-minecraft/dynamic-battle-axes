package io.github.brooswitminecraft.dynamicbattleaxes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Tiers;

/** Pure checks on {@link BattleAxeMaterial}; no game bootstrap required. */
class BattleAxeMaterialTest {

    @Test
    void itemIdsAreMaterialPrefixedBattleAxes() {
        assertEquals("wood_battle_axe", BattleAxeMaterial.WOOD.itemId());
        assertEquals("stone_battle_axe", BattleAxeMaterial.STONE.itemId());
        assertEquals("copper_battle_axe", BattleAxeMaterial.COPPER.itemId());
        assertEquals("iron_battle_axe", BattleAxeMaterial.IRON.itemId());
        assertEquals("gold_battle_axe", BattleAxeMaterial.GOLD.itemId());
        assertEquals("diamond_battle_axe", BattleAxeMaterial.DIAMOND.itemId());
        assertEquals("netherite_battle_axe", BattleAxeMaterial.NETHERITE.itemId());
    }

    @Test
    void sixMaterialsUseTheirMatchingVanillaTier() {
        assertEquals(Tiers.WOOD, BattleAxeMaterial.WOOD.tier());
        assertEquals(Tiers.STONE, BattleAxeMaterial.STONE.tier());
        assertEquals(Tiers.IRON, BattleAxeMaterial.IRON.tier());
        assertEquals(Tiers.GOLD, BattleAxeMaterial.GOLD.tier());
        assertEquals(Tiers.DIAMOND, BattleAxeMaterial.DIAMOND.tier());
        assertEquals(Tiers.NETHERITE, BattleAxeMaterial.NETHERITE.tier());
    }

    @Test
    void copperUsesTheDedicatedNonVanillaTier() {
        assertNotNull(BattleAxeMaterial.COPPER.tier());
        assertEquals(BattleAxeTiers.COPPER, BattleAxeMaterial.COPPER.tier());
    }

    @Test
    void sameMaterialSwordDamageMatchesVanillaForMaterialsThatHaveOne() {
        assertEquals(4, BattleAxeMaterial.WOOD.sameMaterialSwordDamage().getAsInt());
        assertEquals(5, BattleAxeMaterial.STONE.sameMaterialSwordDamage().getAsInt());
        assertEquals(6, BattleAxeMaterial.IRON.sameMaterialSwordDamage().getAsInt());
        assertEquals(4, BattleAxeMaterial.GOLD.sameMaterialSwordDamage().getAsInt());
        assertEquals(7, BattleAxeMaterial.DIAMOND.sameMaterialSwordDamage().getAsInt());
        assertEquals(8, BattleAxeMaterial.NETHERITE.sameMaterialSwordDamage().getAsInt());
    }

    @Test
    void copperHasNoSameMaterialSwordComparisonBecauseVanillaHasNone() {
        assertFalse(BattleAxeMaterial.COPPER.sameMaterialSwordDamage().isPresent());
    }

    @Test
    void copperBaselineIsTheMINECRAFT258InterpolationOfStoneAndIron() {
        // MINECRAFT-258 decision: (stone=5 + iron=6) / 2, since vanilla has no copper sword.
        assertEquals(5.5F, BattleAxeMaterial.COPPER.sameMaterialSwordDamageBaseline(), 1.0E-6);
    }

    @Test
    void nonCopperBaselineMatchesItsVanillaSameMaterialSwordDamageExactly() {
        for (BattleAxeMaterial material : BattleAxeMaterial.values()) {
            if (material == BattleAxeMaterial.COPPER) {
                continue;
            }
            assertEquals(material.sameMaterialSwordDamage().getAsInt(), material.sameMaterialSwordDamageBaseline(), 1.0E-6, material.name());
        }
    }

    @Test
    void attackDamageModifierEqualsTheSameMaterialSwordBaseline() {
        // So that total axe damage (vanilla base 1 + this modifier) is exactly 1 higher than the sword baseline.
        for (BattleAxeMaterial material : BattleAxeMaterial.values()) {
            assertEquals(material.sameMaterialSwordDamageBaseline(), material.attackDamageModifier(), 1.0E-6, material.name());
        }
    }
}
