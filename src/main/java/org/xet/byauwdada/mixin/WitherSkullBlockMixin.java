package org.xet.byauwdada.mixin;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.WitherSkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.xet.byauwdada.ModBosses;
import org.xet.byauwdada.entity.ImprovedZombie;

@Mixin(WitherSkullBlock.class)
public class WitherSkullBlockMixin {
    @Nullable
    private static BlockPattern witherBossPattern;

    @Nullable
    private static BlockPattern witherUpgradeBossPattern;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static void onPlaced(World world, BlockPos pos, SkullBlockEntity blockEntity) {
        if (!world.isClient) {
            BlockState blockState = blockEntity.getCachedState();
            boolean bl = blockState.isOf(Blocks.WITHER_SKELETON_SKULL) || blockState.isOf(Blocks.WITHER_SKELETON_WALL_SKULL);
            if (bl && pos.getY() >= world.getBottomY() && world.getDifficulty() != Difficulty.PEACEFUL) {
                BlockPattern.Result result = getWitherBossPattern().searchAround(world, pos);
                if (result != null) {
                    WitherEntity improvedZombieEntity = (WitherEntity) EntityType.WITHER.create(world);
                    if (improvedZombieEntity != null) {
                        CarvedPumpkinBlock.breakPatternBlocks(world, result);
                        BlockPos blockPos = result.translate(1, 2, 0).getBlockPos();
                        improvedZombieEntity.refreshPositionAndAngles((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.55, (double)blockPos.getZ() + 0.5, result.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
                        improvedZombieEntity.bodyYaw = result.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
                        improvedZombieEntity.onSummoned();

                        for (ServerPlayerEntity serverPlayerEntity : world.getNonSpectatingEntities(ServerPlayerEntity.class, improvedZombieEntity.getBoundingBox().expand(50.0))) {
                            Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity, improvedZombieEntity);
                        }

                        world.spawnEntity(improvedZombieEntity);
                        CarvedPumpkinBlock.updatePatternBlocks(world, result);
                    }
                }

                BlockPattern.Result resultUpgrade = getWitherUpgradeBossPattern().searchAround(world, pos);
                if (resultUpgrade != null) {
                    ImprovedZombie improvedZombieEntity = (ImprovedZombie) ModBosses.IMPROVED_ZOMBIE.create(world);
                    if (improvedZombieEntity != null) {
                        CarvedPumpkinBlock.breakPatternBlocks(world, resultUpgrade);
                        BlockPos blockPos = resultUpgrade.translate(1, 2, 0).getBlockPos();
                        improvedZombieEntity.refreshPositionAndAngles((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.55, (double)blockPos.getZ() + 0.5, result.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
                        improvedZombieEntity.bodyYaw = resultUpgrade.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
                        improvedZombieEntity.onSummoned();

                        for (ServerPlayerEntity serverPlayerEntity : world.getNonSpectatingEntities(ServerPlayerEntity.class, improvedZombieEntity.getBoundingBox().expand(50.0))) {
                            Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity, improvedZombieEntity);
                        }

                        world.spawnEntity(improvedZombieEntity);
                        CarvedPumpkinBlock.updatePatternBlocks(world, resultUpgrade);
                    }
                }
            }
        }
    }

    private static BlockPattern getWitherBossPattern() {
        if (witherBossPattern == null) {
            witherBossPattern = BlockPatternBuilder.start().aisle("^^^", "###", "~#~").where('#', (pos) -> {
                return pos.getBlockState().isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
            }).where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL)))).where('~', (pos) -> {
                return pos.getBlockState().isAir();
            }).build();
        }

        return witherBossPattern;
    }

    private static BlockPattern getWitherUpgradeBossPattern() {
        if (witherUpgradeBossPattern == null) {
            witherUpgradeBossPattern = BlockPatternBuilder.start().aisle("^#^", "###", "^#^").where('#', (pos) -> {
                return pos.getBlockState().isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
            }).where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL)))).where('~', (pos) -> {
                return pos.getBlockState().isAir();
            }).build();
        }

        return witherUpgradeBossPattern;
    }
}
