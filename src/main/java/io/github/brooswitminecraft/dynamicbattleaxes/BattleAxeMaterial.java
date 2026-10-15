package io.github.brooswitminecraft.dynamicbattleaxes;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * The seven battle-axe materials and their NON-BINDING placeholder combat
 * numbers (attack damage add-on, attack speed add-on), recorded here so a
 * future balance pass can find and replace them in one place.
 *
 * <p>Wood/stone/iron/gold/diamond/netherite reuse vanilla's existing
 * per-tier {@code AxeItem} damage/speed values verbatim (MINECRAFT-294 is
 * scoped to registration/recipes, not combat balance, so no new numbers are
 * chosen for them). Copper has no vanilla axe or sword to copy, so its
 * values are a linear interpolation between stone and iron -- the two
 * vanilla tiers copper conventionally sits between -- and are exactly as
 * non-binding as everything else here.
 */
public enum BattleAxeMaterial {
    WOOD("wood", Tiers.WOOD, 6.0F, -3.2F),
    STONE("stone", Tiers.STONE, 7.0F, -3.2F),
    COPPER("copper", BattleAxeTiers.COPPER, interpolate(7.0F, 6.0F), interpolate(-3.2F, -3.1F)),
    IRON("iron", Tiers.IRON, 6.0F, -3.1F),
    GOLD("gold", Tiers.GOLD, 6.0F, -3.0F),
    DIAMOND("diamond", Tiers.DIAMOND, 5.0F, -3.0F),
    NETHERITE("netherite", Tiers.NETHERITE, 5.0F, -3.0F);

    private final String id;
    private final Tier tier;
    private final float attackDamageBaseline;
    private final float attackSpeedBaseline;

    BattleAxeMaterial(String id, Tier tier, float attackDamageBaseline, float attackSpeedBaseline) {
        this.id = id;
        this.tier = tier;
        this.attackDamageBaseline = attackDamageBaseline;
        this.attackSpeedBaseline = attackSpeedBaseline;
    }

    /** Registry name, e.g. {@code iron_battle_axe}. */
    public String itemId() {
        return id + "_battle_axe";
    }

    public Tier tier() {
        return tier;
    }

    /** Non-binding: same role as vanilla {@code AxeItem.createAttributes}'s own {@code attackDamage} argument. */
    public float attackDamageBaseline() {
        return attackDamageBaseline;
    }

    /** Non-binding: same role as vanilla {@code AxeItem.createAttributes}'s own {@code attackSpeed} argument. */
    public float attackSpeedBaseline() {
        return attackSpeedBaseline;
    }

    static float interpolate(float stoneValue, float ironValue) {
        return (stoneValue + ironValue) / 2.0F;
    }
}
