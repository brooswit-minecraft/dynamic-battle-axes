package io.github.brooswitminecraft.dynamicbattleaxes;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/**
 * Tool tiers with no vanilla equivalent. Vanilla ships no copper tool, so
 * there is no established mining level/durability/enchantability to copy the
 * way {@link BattleAxeMaterial} copies vanilla's axe damage/speed for the
 * other six materials. These numbers are a linear interpolation between
 * {@link Tiers#STONE} and {@link Tiers#IRON} -- the two neighboring vanilla
 * tiers copper conventionally sits between -- and are NON-BINDING
 * placeholders, same as the rest of {@link BattleAxeMaterial}.
 */
final class BattleAxeTiers {
    static final Tier COPPER = new SimpleTier(
            BlockTags.INCORRECT_FOR_STONE_TOOL,
            Math.round((Tiers.STONE.getUses() + Tiers.IRON.getUses()) / 2.0F),
            interpolate(Tiers.STONE.getSpeed(), Tiers.IRON.getSpeed()),
            interpolate(Tiers.STONE.getAttackDamageBonus(), Tiers.IRON.getAttackDamageBonus()),
            Math.round((Tiers.STONE.getEnchantmentValue() + Tiers.IRON.getEnchantmentValue()) / 2.0F),
            () -> Ingredient.of(Items.COPPER_INGOT));

    private BattleAxeTiers() {
    }

    private static float interpolate(float stoneValue, float ironValue) {
        return (stoneValue + ironValue) / 2.0F;
    }
}
