package io.github.brooswitminecraft.dynamicbattleaxes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import io.github.brooswitminecraft.magicavoxelloader.vox.MinecraftItemModel;
import io.github.brooswitminecraft.magicavoxelloader.vox.MinecraftItemModelConverter;
import io.github.brooswitminecraft.magicavoxelloader.vox.VoxAssetLoader;
import io.github.brooswitminecraft.magicavoxelloader.vox.VoxelModel;

/**
 * Exercises the real shared-loader asset path (MINECRAFT-258) for each of
 * the seven packaged {@code .vox} battle-axe models: each one is its own
 * actual MagicaVoxel 150 file, materialized by the
 * {@code materializeBattleAxeVox} Gradle task from its reviewable hex
 * source, and must pass through {@code VoxAssetLoader.load} and
 * {@code MinecraftItemModelConverter.convert} exactly as
 * {@code DynamicBattleAxesMod} does at common setup -- demonstrating the
 * usable items and working asset path together, per the handoff's
 * acceptance checks. No NeoForge/game bootstrap required: the loader's vox
 * API is plain Java.
 */
class BattleAxeVoxAssetTest {

    @Test
    void everyMaterialsPackagedVoxAssetLoadsAndConverts() throws IOException {
        for (BattleAxeMaterial material : BattleAxeMaterial.values()) {
            String path = "/assets/dynamicbattleaxes/vox/" + material.itemId() + ".vox";
            try (InputStream stream = getClass().getResourceAsStream(path)) {
                assertNotNull(stream, "missing packaged asset " + path);
                VoxelModel model = VoxAssetLoader.load(stream);
                assertVoxelModelWithinPublishedStandard(model, material.name());

                MinecraftItemModel itemModel = MinecraftItemModelConverter.convert(model);
                assertEquals(model.voxels().size(), itemModel.elements().size(), material.name());
            }
        }
    }

    private static void assertVoxelModelWithinPublishedStandard(VoxelModel model, String label) {
        assertTrue(model.width() >= 1 && model.width() <= 16, label);
        assertTrue(model.height() >= 1 && model.height() <= 16, label);
        assertTrue(model.depth() >= 1 && model.depth() <= 16, label);
        assertTrue(model.voxels().size() <= 1024, label);
        assertFalse(model.voxels().isEmpty(), label);
        model.voxels().forEach(voxel -> assertTrue(voxel.paletteIndex() >= 1 && voxel.paletteIndex() <= 255, label));
    }
}
