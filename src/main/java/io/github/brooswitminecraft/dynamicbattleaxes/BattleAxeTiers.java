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
 * choice, and is NON-BINDING. {@code attackDamageBonus} stays 0 here
 * (no bonus): it is {@link Tier}'s own attack-damage field, which vanilla's
 * {@code SwordItem}/{@code AxeItem} read internally, but this mod does not
 * -- MINECRAFT-258's actual combat balance is enacted through
 * {@link BattleAxeItem#createAttributes}'s own explicit
 * {@code ItemAttributeModifiers} instead (see {@link BattleAxeMaterial}),
 * so this field being 0 is simply unused, not a scope hold.
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
