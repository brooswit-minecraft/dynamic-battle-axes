package io.github.brooswitminecraft.dynamicbattleaxes;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void sixMaterialsReuseVanillaAxeDamageAndSpeedVerbatim() {
        // Same baseline numbers vanilla Items.java passes to AxeItem.createAttributes
        // for the same-material vanilla axe -- no new balance decision made here.
        assertEquals(6.0F, BattleAxeMaterial.WOOD.attackDamageBaseline());
        assertEquals(-3.2F, BattleAxeMaterial.WOOD.attackSpeedBaseline());
        assertEquals(7.0F, BattleAxeMaterial.STONE.attackDamageBaseline());
        assertEquals(-3.2F, BattleAxeMaterial.STONE.attackSpeedBaseline());
        assertEquals(6.0F, BattleAxeMaterial.IRON.attackDamageBaseline());
        assertEquals(-3.1F, BattleAxeMaterial.IRON.attackSpeedBaseline());
        assertEquals(6.0F, BattleAxeMaterial.GOLD.attackDamageBaseline());
        assertEquals(-3.0F, BattleAxeMaterial.GOLD.attackSpeedBaseline());
        assertEquals(5.0F, BattleAxeMaterial.DIAMOND.attackDamageBaseline());
        assertEquals(-3.0F, BattleAxeMaterial.DIAMOND.attackSpeedBaseline());
        assertEquals(5.0F, BattleAxeMaterial.NETHERITE.attackDamageBaseline());
        assertEquals(-3.0F, BattleAxeMaterial.NETHERITE.attackSpeedBaseline());
    }

    @Test
    void copperBaselineIsTheMidpointOfStoneAndIron() {
        assertEquals(6.5F, BattleAxeMaterial.COPPER.attackDamageBaseline());
        assertEquals(-3.15F, BattleAxeMaterial.COPPER.attackSpeedBaseline(), 0.0001F);
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
}
