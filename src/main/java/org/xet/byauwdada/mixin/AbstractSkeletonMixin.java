package org.xet.byauwdada.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonMixin extends HostileEntity implements RangedAttackMob {
    @Final
    @Shadow
    private final BowAttackGoal<AbstractSkeletonEntity> bowAttackGoal = new BowAttackGoal(this, 1.3, 10, 20.0F);

    @Shadow
    @Final
    private MeleeAttackGoal meleeAttackGoal;

    @Shadow
    protected abstract PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier);

    protected AbstractSkeletonMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void updateAttackType() {
        if (this.getWorld() != null && !this.getWorld().isClient) {
            this.goalSelector.remove(this.meleeAttackGoal);
            this.goalSelector.remove(this.bowAttackGoal);
            ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
            if (itemStack.isOf(Items.BOW)) {
                int i = 10;
                if (this.getWorld().getDifficulty() != Difficulty.HARD) {
                    i = 30;
                }

                this.bowAttackGoal.setAttackInterval(i);
                this.goalSelector.add(4, this.bowAttackGoal);
            } else {
                this.goalSelector.add(4, this.meleeAttackGoal);
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void shootAt(LivingEntity target, float pullProgress) {
        int typeAttack = this.random.nextBetween(0, 5);

        if (typeAttack <= 2) {
            ItemStack arrowStack = this.getProjectileType(this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)));
            PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(arrowStack, pullProgress);
            double x = target.getX() - this.getX();
            double y = target.getBodyY(0.3333333333333333) - persistentProjectileEntity.getY();
            double z = target.getZ() - this.getZ();
            double g = Math.sqrt(x * x + z * z);
            persistentProjectileEntity.setVelocity(x, y + g * 0.20000000298023224, z, 1.6F, (float) (14 - this.getWorld().getDifficulty().getId() * 4));
            this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.getWorld().spawnEntity(persistentProjectileEntity);
        } else if (typeAttack <= 4) {
            double x = target.getX() - this.getX();
            double y = target.getBodyY(0.5) - this.getBodyY(0.5);
            double z = target.getZ() - this.getZ();

            SmallFireballEntity smallFireballEntity = new SmallFireballEntity(
                    this.getWorld(), this, x, y, z
            );
            smallFireballEntity.setPosition(smallFireballEntity.getX(), this.getBodyY(0.5) + 0.5, smallFireballEntity.getZ());
            this.getWorld().spawnEntity(smallFireballEntity);
        } else if (typeAttack == 5) {
            for (int i = 0; i < 30; i ++) {
                ItemStack arrowStack = this.getProjectileType(this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)));
                PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(arrowStack, pullProgress);
                double x = this.random.nextTriangular(0, 20);
                double y = this.random.nextTriangular(0, 20);
                double z = this.random.nextTriangular(0, 20);
                double g = Math.sqrt(x * x + z * z);
                persistentProjectileEntity.setVelocity(
                        x, y + g * 0.20000000298023224, z,
                        3F,
                        (float) (14 - this.getWorld().getDifficulty().getId() * 6)
                );
                this.getWorld().spawnEntity(persistentProjectileEntity);
            }
            this.playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        }
    }
}
