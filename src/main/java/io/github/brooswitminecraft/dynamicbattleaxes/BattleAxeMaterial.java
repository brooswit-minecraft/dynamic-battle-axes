package io.github.brooswitminecraft.dynamicbattleaxes;

import java.util.OptionalInt;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * The seven battle-axe materials, now carrying the BINDING combat balance
 * decided on MINECRAFT-258 (see that ticket's decision comment): every
 * battle-axe's total attack damage is its same-material sword's total
 * damage (vanilla's base 1 + sword's own +3 + the tier's
 * {@code attackDamageBonus}) plus 1 -- "slightly higher damage" than the
 * same-material sword. Vanilla ships no copper sword (or axe), so
 * {@link #sameMaterialSwordDamage()} interpolates the stone/iron total
 * (5, 6) to 5.5, the copper baseline MINECRAFT-258 establishes.
 *
 * <p>Attack speed and reach are NOT material-dependent, matching vanilla's
 * own sword convention (every vanilla sword shares the same -2.4 attack
 * speed modifier regardless of material): see
 * {@link BattleAxeItem#ATTACK_SPEED_MODIFIER} and
 * {@link BattleAxeItem#REACH_BONUS}.
 */
public enum BattleAxeMaterial {
    WOOD("wood", Tiers.WOOD, OptionalInt.of(4)),
    STONE("stone", Tiers.STONE, OptionalInt.of(5)),
    COPPER("copper", BattleAxeTiers.COPPER, OptionalInt.empty()),
    IRON("iron", Tiers.IRON, OptionalInt.of(6)),
    GOLD("gold", Tiers.GOLD, OptionalInt.of(4)),
    DIAMOND("diamond", Tiers.DIAMOND, OptionalInt.of(7)),
    NETHERITE("netherite", Tiers.NETHERITE, OptionalInt.of(8));

    /** Interpolated stone/iron baseline MINECRAFT-258 establishes for copper: (5 + 6) / 2. */
    private static final float COPPER_SAME_MATERIAL_SWORD_DAMAGE = 5.5F;

    private final String id;
    private final Tier tier;
    private final OptionalInt sameMaterialSwordDamage;

    BattleAxeMaterial(String id, Tier tier, OptionalInt sameMaterialSwordDamage) {
        this.id = id;
        this.tier = tier;
        this.sameMaterialSwordDamage = sameMaterialSwordDamage;
    }

    /** Registry name, e.g. {@code iron_battle_axe}. */
    public String itemId() {
        return id + "_battle_axe";
    }

    public Tier tier() {
        return tier;
    }

    /**
     * Same-material vanilla sword's total attack damage (base 1 + sword's
     * +3 + tier bonus), where a vanilla sword of this material exists.
     * Empty only for copper's integer form; use
     * {@link #sameMaterialSwordDamageBaseline()} for the actual (possibly
     * interpolated) baseline this ticket's balance math is built on.
     */
    public OptionalInt sameMaterialSwordDamage() {
        return sameMaterialSwordDamage;
    }

    /** The baseline MINECRAFT-258 actually balances against: vanilla's value, or copper's interpolated one. */
    public float sameMaterialSwordDamageBaseline() {
        return sameMaterialSwordDamage.isPresent() ? sameMaterialSwordDamage.getAsInt() : COPPER_SAME_MATERIAL_SWORD_DAMAGE;
    }

    /**
     * The battle-axe's own {@code ATTACK_DAMAGE} attribute-modifier value
     * (vanilla's base 1 is added on top of this by the attribute system
     * itself): {@link #sameMaterialSwordDamageBaseline()}, which makes the
     * axe's total damage exactly 1 higher than the same-material sword.
     */
    public float attackDamageModifier() {
        return sameMaterialSwordDamageBaseline();
    }
}
