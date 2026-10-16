package io.github.brooswitminecraft.dynamicbattleaxes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Registration coverage, and (since MINECRAFT-258 enacts the combat balance
 * MINECRAFT-294 deliberately left non-binding) the review requirement this
 * now exists to pin down going forward: every battle axe carries exactly
 * the attack-damage/speed/reach modifiers that ticket's decision comment
 * records -- each 1 higher-damage, 0.4 slower, and 0.5 longer-reach than
 * the same-material sword baseline. Regressing either direction -- losing
 * the modifiers again, or drifting their values -- fails
 * {@link #everyBattleAxeCarriesItsDecidedAttackAttributeModifiers()}.
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
    void everyBattleAxeCarriesItsDecidedAttackAttributeModifiers() {
        DynamicBattleAxesMod.BATTLE_AXES.forEach((material, deferredItem) -> {
            ItemAttributeModifiers modifiers = deferredItem.get().components().get(DataComponents.ATTRIBUTE_MODIFIERS);
            assertTrue(modifiers != null && !modifiers.modifiers().isEmpty(), material.name() + " must carry its MINECRAFT-258 attribute modifiers");

            Map<Holder<Attribute>, Double> byAttribute = new HashMap<>();
            modifiers.modifiers().forEach(entry -> byAttribute.put(entry.attribute(), entry.modifier().amount()));

            assertEquals(material.attackDamageModifier(), byAttribute.get(Attributes.ATTACK_DAMAGE), 1.0E-6, material.name());
            assertEquals(BattleAxeItem.ATTACK_SPEED_MODIFIER, byAttribute.get(Attributes.ATTACK_SPEED), 1.0E-6, material.name());
            assertEquals(BattleAxeItem.REACH_BONUS, byAttribute.get(Attributes.ENTITY_INTERACTION_RANGE), 1.0E-6, material.name());
        });
    }

    @Test
    void everyBattleAxeDealsExactlyOneMoreDamageThanItsSameMaterialSwordBaseline() {
        for (BattleAxeMaterial material : BattleAxeMaterial.values()) {
            assertEquals(material.sameMaterialSwordDamageBaseline() + 1.0F, 1.0F + material.attackDamageModifier(), 1.0E-6, material.name());
        }
    }
}
