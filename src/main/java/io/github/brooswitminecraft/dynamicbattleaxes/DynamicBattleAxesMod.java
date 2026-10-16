package io.github.brooswitminecraft.dynamicbattleaxes;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import io.github.brooswitminecraft.magicavoxelloader.vox.MinecraftItemModelConverter;
import io.github.brooswitminecraft.magicavoxelloader.vox.VoxAssetLoader;
import io.github.brooswitminecraft.magicavoxelloader.vox.VoxelModel;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entry point and registry for Dynamic Battle Axes (MINECRAFT-285 / MINECRAFT-294 / MINECRAFT-258). */
@Mod(DynamicBattleAxesMod.MODID)
public class DynamicBattleAxesMod {
    public static final String MODID = "dynamicbattleaxes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    /** One registration per {@link BattleAxeMaterial}, keyed by material for lookup in tests and the creative tab. */
    public static final Map<BattleAxeMaterial, DeferredItem<BattleAxeItem>> BATTLE_AXES = registerBattleAxes();

    /** Each material's own real MagicaVoxel model, loaded individually at common setup (MINECRAFT-258). */
    private static volatile Map<BattleAxeMaterial, VoxelModel> battleAxeVoxelModels;

    private static Map<BattleAxeMaterial, DeferredItem<BattleAxeItem>> registerBattleAxes() {
        Map<BattleAxeMaterial, DeferredItem<BattleAxeItem>> axes = new EnumMap<>(BattleAxeMaterial.class);
        for (BattleAxeMaterial material : BattleAxeMaterial.values()) {
            axes.put(material, ITEMS.register(material.itemId(), () -> createBattleAxe(material)));
        }
        return axes;
    }

    /**
     * The actual item-construction factory registration uses. Carries the
     * BINDING combat attribute modifiers decided on MINECRAFT-258 (see
     * {@link BattleAxeItem#createAttributes}); {@link BattleAxeMaterial}'s
     * damage math feeds directly into the real {@code ItemAttributeModifiers}
     * data component here, no longer non-binding evidence only.
     * Package-private: {@code DynamicBattleAxesModTest} reaches this exact
     * factory through {@code BATTLE_AXES}'s real, registered
     * {@code DeferredItem}s rather than calling it a second time directly
     * -- constructing a second {@code Item} once the registry has already
     * frozen (as it has by the time tests run) would itself throw.
     */
    static BattleAxeItem createBattleAxe(BattleAxeMaterial material) {
        return new BattleAxeItem(material,
                new Item.Properties().attributes(BattleAxeItem.createAttributes(material)));
    }

    public DynamicBattleAxesMod(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::loadBattleAxeVoxelModels);
        LOGGER.info("Dynamic Battle Axes loaded");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            BATTLE_AXES.values().forEach(event::accept);
        }
    }

    /**
     * Loads each of the seven packaged {@code .vox} assets through the
     * shared library's real loader/converter -- one call per material, so
     * all seven actually pass through the asset path, not just the first.
     */
    private void loadBattleAxeVoxelModels(FMLCommonSetupEvent ignored) {
        Map<BattleAxeMaterial, VoxelModel> models = new EnumMap<>(BattleAxeMaterial.class);
        for (BattleAxeMaterial material : BattleAxeMaterial.values()) {
            String path = "/assets/" + MODID + "/vox/" + material.itemId() + ".vox";
            try (InputStream stream = getClass().getResourceAsStream(path)) {
                if (stream == null) {
                    throw new IllegalStateException("Packaged " + path + " is missing");
                }
                models.put(material, VoxAssetLoader.load(stream));
            } catch (IOException e) {
                throw new IllegalStateException("Unable to load packaged MagicaVoxel asset " + path, e);
            }
        }
        battleAxeVoxelModels = Map.copyOf(models);
    }

    public static VoxelModel voxelModel(BattleAxeMaterial material) {
        Map<BattleAxeMaterial, VoxelModel> models = battleAxeVoxelModels;
        if (models == null) {
            throw new IllegalStateException("Battle-axe models requested before common setup");
        }
        return models.get(material);
    }

    public static io.github.brooswitminecraft.magicavoxelloader.vox.MinecraftItemModel itemModel(BattleAxeMaterial material) {
        return MinecraftItemModelConverter.convert(voxelModel(material));
    }
}
