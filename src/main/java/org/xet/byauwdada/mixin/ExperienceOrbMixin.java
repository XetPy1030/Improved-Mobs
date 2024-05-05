package org.xet.byauwdada.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbMixin extends Entity {

    @Shadow protected abstract int repairPlayerGears(PlayerEntity player, int amount);

    @Shadow private int amount;

    @Shadow private int pickingCount;

    public ExperienceOrbMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * @author
     * @reason
     */
    @ModifyConstant(method = "onPlayerCollision", constant = @Constant(intValue = 2))
    private int injected(int value) {
        return 0;
    }
}
