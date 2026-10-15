package io.github.brooswitminecraft.dynamicbattleaxes;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/**
 * A tool tier with no vanilla equivalent. Vanilla ships no copper tool, so
 * there is no established mining level/durability/enchantability to copy the
 * way {@link BattleAxeMaterial} copies vanilla's axe for the other six
 * materials.
 *
 * <p>Mining-speed, durability ({@code uses}), and enchantability are
 * non-combat tool configuration that {@link Tier} requires some value for
 * regardless; interpolating between {@link Tiers#STONE} and
 * {@link Tiers#IRON} -- the two neighboring vanilla tiers copper
 * conventionally sits between -- is the minimal, convention-following
 * choice, and is NON-BINDING. {@code attackDamageBonus} is a combat value,
 * so it is NOT interpolated/invented here: it is 0 (no bonus), matching
 * that MINECRAFT-294 does not enact combat balance for any material (see
 * {@link DynamicBattleAxesMod}, which never calls
 * {@code AxeItem.createAttributes} for any of the seven items).
 */
final class BattleAxeTiers {
    static final Tier COPPER = new SimpleTier(
            BlockTags.INCORRECT_FOR_STONE_TOOL,
            Math.round((Tiers.STONE.getUses() + Tiers.IRON.getUses()) / 2.0F),
            interpolate(Tiers.STONE.getSpeed(), Tiers.IRON.getSpeed()),
            0.0F,
            Math.round((Tiers.STONE.getEnchantmentValue() + Tiers.IRON.getEnchantmentValue()) / 2.0F),
            () -> Ingredient.of(Items.COPPER_INGOT));

    private BattleAxeTiers() {
    }

    private static float interpolate(float stoneValue, float ironValue) {
        return (stoneValue + ironValue) / 2.0F;
    }
}
