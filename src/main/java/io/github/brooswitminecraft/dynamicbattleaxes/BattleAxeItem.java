package io.github.brooswitminecraft.dynamicbattleaxes;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

/**
 * A battle-axe. Deliberately a plain {@link AxeItem} with no overrides: this
 * registers the item as a distinct type from vanilla axes for future
 * gameplay work, without itself enacting any combat behavior (no sweep arc,
 * no PvP/friendly-fire semantics -- those are explicitly out of scope for
 * MINECRAFT-294).
 */
public class BattleAxeItem extends AxeItem {
    public BattleAxeItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
    }
}
