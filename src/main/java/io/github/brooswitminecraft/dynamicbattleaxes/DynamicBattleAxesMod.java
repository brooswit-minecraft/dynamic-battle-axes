package io.github.brooswitminecraft.dynamicbattleaxes;

import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entry point and registry for Dynamic Battle Axes (MINECRAFT-285 / MINECRAFT-294). */
@Mod(DynamicBattleAxesMod.MODID)
public class DynamicBattleAxesMod {
    public static final String MODID = "dynamicbattleaxes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    /** One registration per {@link BattleAxeMaterial}, keyed by material for lookup in tests and the creative tab. */
    public static final Map<BattleAxeMaterial, DeferredItem<BattleAxeItem>> BATTLE_AXES = registerBattleAxes();

    /**
     * No {@code .attributes(...)} call: this registers each item with no
     * attack-damage/attack-speed modifiers at all (the default empty
     * {@code ItemAttributeModifiers}), so no combat value is enacted here.
     * {@link BattleAxeMaterial}'s baseline numbers are non-binding evidence
     * for a future balance task, not inputs to this registration.
     */
    private static Map<BattleAxeMaterial, DeferredItem<BattleAxeItem>> registerBattleAxes() {
        Map<BattleAxeMaterial, DeferredItem<BattleAxeItem>> axes = new EnumMap<>(BattleAxeMaterial.class);
        for (BattleAxeMaterial material : BattleAxeMaterial.values()) {
            axes.put(material, ITEMS.register(material.itemId(),
                    () -> new BattleAxeItem(material.tier(), new Item.Properties())));
        }
        return axes;
    }

    public DynamicBattleAxesMod(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        LOGGER.info("Dynamic Battle Axes loaded");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            BATTLE_AXES.values().forEach(event::accept);
        }
    }
}
