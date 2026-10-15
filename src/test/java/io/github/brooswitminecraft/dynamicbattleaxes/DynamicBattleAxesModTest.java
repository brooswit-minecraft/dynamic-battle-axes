package io.github.brooswitminecraft.dynamicbattleaxes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Registration coverage, and the review requirement this exists to pin down
 * going forward: none of the seven battle axes carry a combat attack-damage
 * or attack-speed modifier. Regressing this (e.g. a future change that adds
 * {@code .attributes(...)} back into {@link DynamicBattleAxesMod#registerBattleAxes})
 * would fail {@link #noBattleAxeCarriesAnAttackAttributeModifier()}.
 *
 * <p>{@code DeferredItem.get()} is NOT used here: it requires the NeoForge
 * registry event to have fired, which never happens outside a running game,
 * so it would throw in a plain unit test. These tests instead check the
 * {@code DeferredItem}'s stored id (safe unbound) and construct a
 * {@code BattleAxeItem} directly with the same arguments
 * {@link DynamicBattleAxesMod#registerBattleAxes()} uses, to inspect what
 * registration would actually produce.
 */
class DynamicBattleAxesModTest {

    @Test
    void allSevenMaterialsAreRegistered() {
        assertEquals(BattleAxeMaterial.values().length, DynamicBattleAxesMod.BATTLE_AXES.size());
        for (BattleAxeMaterial material : BattleAxeMaterial.values()) {
            assertTrue(DynamicBattleAxesMod.BATTLE_AXES.containsKey(material), material.name());
        }
    }

    @Test
    void everyRegisteredItemIdMatchesItsMaterial() {
        DynamicBattleAxesMod.BATTLE_AXES.forEach((material, deferredItem) ->
                assertEquals("dynamicbattleaxes:" + material.itemId(), deferredItem.getId().toString(), material.name()));
    }

    @Test
    void noBattleAxeCarriesAnAttackAttributeModifier() {
        for (BattleAxeMaterial material : BattleAxeMaterial.values()) {
            Item item = new BattleAxeItem(material.tier(), new Item.Properties());
            ItemAttributeModifiers modifiers = item.components().get(DataComponents.ATTRIBUTE_MODIFIERS);
            assertTrue(modifiers != null && modifiers.modifiers().isEmpty(),
                    material.name() + " must carry no combat attribute modifiers (none are enacted by MINECRAFT-294)");
        }
    }
}
