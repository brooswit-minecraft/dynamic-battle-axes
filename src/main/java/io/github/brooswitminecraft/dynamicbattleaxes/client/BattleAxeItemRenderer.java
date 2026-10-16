package io.github.brooswitminecraft.dynamicbattleaxes.client;

import io.github.brooswitminecraft.dynamicbattleaxes.BattleAxeMaterial;
import io.github.brooswitminecraft.dynamicbattleaxes.DynamicBattleAxesMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Renders a battle-axe's packaged {@code .vox} model as a scaled native
 * block-model voxel grid, same mechanism as the shared library's own demo
 * item renderer. Palette index 1 (the handle) always renders as a wooden
 * block; palette index 2 (the head) renders as a block representative of
 * this specific axe's material -- the fitting-artwork, material-at-a-glance
 * presentation MINECRAFT-258 calls for, kept intentionally simple per that
 * decision comment's "rough art is not a blocker".
 */
public final class BattleAxeItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final Block HANDLE_BLOCK = Blocks.STRIPPED_OAK_WOOD;

    private final BattleAxeMaterial material;

    public BattleAxeItemRenderer(BattleAxeMaterial material) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.material = material;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        var model = DynamicBattleAxesMod.voxelModel(material);
        Block headBlock = headBlock(material);
        float cell = 1.0F / Math.max(model.width(), Math.max(model.height(), model.depth()));
        poses.pushPose();
        poses.translate(-0.5D, -0.5D, -0.5D);
        for (var voxel : model.voxels()) {
            Block block = voxel.paletteIndex() == 2 ? headBlock : HANDLE_BLOCK;
            poses.pushPose();
            poses.translate(voxel.x() * cell, voxel.y() * cell, voxel.z() * cell);
            poses.scale(cell, cell, cell);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(), poses, buffers, light, overlay);
            poses.popPose();
        }
        poses.popPose();
    }

    private static Block headBlock(BattleAxeMaterial material) {
        return switch (material) {
            case WOOD -> Blocks.STRIPPED_OAK_WOOD;
            case STONE -> Blocks.COBBLESTONE;
            case COPPER -> Blocks.COPPER_BLOCK;
            case IRON -> Blocks.IRON_BLOCK;
            case GOLD -> Blocks.GOLD_BLOCK;
            case DIAMOND -> Blocks.DIAMOND_BLOCK;
            case NETHERITE -> Blocks.NETHERITE_BLOCK;
        };
    }
}
