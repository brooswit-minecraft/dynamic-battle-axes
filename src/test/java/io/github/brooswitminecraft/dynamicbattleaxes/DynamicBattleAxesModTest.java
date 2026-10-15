package io.github.brooswitminecraft.dynamicbattleaxes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Registration coverage, and the review requirement this exists to pin down
 * going forward: none of the seven battle axes carry a combat attack-damage
 * or attack-speed modifier. Regressing this (e.g. a future change that adds
 * {@code .attributes(...)} back into
 * {@link DynamicBattleAxesMod#createBattleAxe(BattleAxeMaterial)}) would
 * fail {@link #noBattleAxeCarriesAnAttackAttributeModifier()}.
 *
 * <p>With {@code neoForge.unitTest.enable()} (build.gradle), this test
 * class runs in a bootstrapped NeoForge environment with this mod actually
 * loaded, so the real {@code RegisterEvent} has fired and
 * {@code DeferredItem.get()} returns the real production-registered item --
 * so these tests read exactly what registration produced, not a second,
 * separately-constructed item. (A plain, un-bootstrapped JUnit run would
 * instead throw on {@code .get()}; it would ALSO throw constructing a new
 * {@code Item} subclass directly once the real mod's registry event has run
 * and frozen the item registry -- {@code Item}'s constructor creates an
 * intrusive registry holder, which is only legal before that freeze.)
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
        DynamicBattleAxesMod.BATTLE_AXES.forEach((material, deferredItem) -> {
            ItemAttributeModifiers modifiers = deferredItem.get().components().get(DataComponents.ATTRIBUTE_MODIFIERS);
            assertTrue(modifiers != null && modifiers.modifiers().isEmpty(),
                    material.name() + " must carry no combat attribute modifiers (none are enacted by MINECRAFT-294)");
        });
    }
}
