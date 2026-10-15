package io.github.brooswitminecraft.dynamicbattleaxes;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * The seven battle-axe materials, paired with NON-BINDING evidence for a
 * future combat-balance task. Nothing on this enum is read by
 * {@link DynamicBattleAxesMod}'s registration -- these are documentation
 * values only, never wired into an {@code ItemAttributeModifiers}.
 *
 * <p>{@link #sameMaterialSwordDamage()} is vanilla's own total attack damage
 * (base 1 + sword's +3 + the tier's {@code attackDamageBonus}) for the
 * vanilla sword of the same material, where one exists. Vanilla ships no
 * copper sword (or axe), so that comparison is a genuine unknown for
 * copper, left empty rather than invented. {@link #proposedReach()} is left
 * empty for every material: no established project convention for
 * battle-axe reach was found, so this is an open unknown, not a guess.
 */
public enum BattleAxeMaterial {
    WOOD("wood", Tiers.WOOD, OptionalInt.of(4)),
    STONE("stone", Tiers.STONE, OptionalInt.of(5)),
    COPPER("copper", BattleAxeTiers.COPPER, OptionalInt.empty()),
    IRON("iron", Tiers.IRON, OptionalInt.of(6)),
    GOLD("gold", Tiers.GOLD, OptionalInt.of(4)),
    DIAMOND("diamond", Tiers.DIAMOND, OptionalInt.of(7)),
    NETHERITE("netherite", Tiers.NETHERITE, OptionalInt.of(8));

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

    /** Evidence only; see class Javadoc. Empty for copper: no vanilla copper sword exists to compare against. */
    public OptionalInt sameMaterialSwordDamage() {
        return sameMaterialSwordDamage;
    }

    /** Evidence only; always empty -- no established battle-axe reach convention was found for any material. */
    public OptionalDouble proposedReach() {
        return OptionalDouble.empty();
    }
}
