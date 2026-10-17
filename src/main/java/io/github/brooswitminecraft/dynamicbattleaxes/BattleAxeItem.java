package io.github.brooswitminecraft.dynamicbattleaxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * A battle-axe: a plain {@link AxeItem} with no behavioural overrides of its
 * own (the 180-degree sweep lives in {@link BattleAxeCombat}, a single
 * shared event listener, not per-item code) but a BINDING combat attribute
 * modifier set, decided on MINECRAFT-258: slightly higher damage, slightly
 * slower attack speed and slightly longer reach than the same-material
 * sword. See {@link BattleAxeMaterial} for the per-material damage math.
 */
public class BattleAxeItem extends AxeItem {
    /**
     * Vanilla's own sword attack-speed modifier is -2.4 (base 4.0 attribute,
     * sword ends at 1.6). "Slightly slower" than the same-material sword is
     * a flat 0.4 further below that, uniformly across all seven materials --
     * matching vanilla's convention that attack speed is not material-
     * dependent for swords either.
     */
    static final double ATTACK_SPEED_MODIFIER = -2.8;

    /**
     * No established battle-axe reach convention existed in this repository
     * (MINECRAFT-294 left it an explicit open unknown); MINECRAFT-258 adds a
     * flat, material-independent bonus on top of the vanilla base entity-
     * interaction range (3.0) for "slightly longer attack reach".
     */
    static final double REACH_BONUS = 0.5;

    private static final ResourceLocation ATTACK_DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath(DynamicBattleAxesMod.MODID, "battle_axe_attack_damage");
    private static final ResourceLocation ATTACK_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(DynamicBattleAxesMod.MODID, "battle_axe_attack_speed");
    private static final ResourceLocation REACH_ID =
            ResourceLocation.fromNamespaceAndPath(DynamicBattleAxesMod.MODID, "battle_axe_reach");

    private final BattleAxeMaterial material;

    public BattleAxeItem(BattleAxeMaterial material, Item.Properties properties) {
        super(material.tier(), properties);
        this.material = material;
    }

    public BattleAxeMaterial material() {
        return material;
    }

    /**
     * The battle-axe's {@code ATTRIBUTE_MODIFIERS} data component: attack
     * damage from {@link BattleAxeMaterial#attackDamageModifier()}, plus the
     * uniform attack-speed and reach modifiers above. Mirrors the shape of
     * vanilla's own {@code SwordItem}/tool attribute wiring, applied only to
     * the main hand like every vanilla melee weapon.
     */
    static ItemAttributeModifiers createAttributes(BattleAxeMaterial material) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(ATTACK_DAMAGE_ID, material.attackDamageModifier(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(ATTACK_SPEED_ID, ATTACK_SPEED_MODIFIER, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(REACH_ID, REACH_BONUS, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }
}
