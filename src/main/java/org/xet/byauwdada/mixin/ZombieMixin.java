package org.xet.byauwdada.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ZombieEntity.class)
public abstract class ZombieMixin extends HostileEntity {

    @Shadow
    public abstract boolean isBaby();

    @Shadow
    public abstract void tick();

    protected ZombieMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean tryAttack(Entity target) {
        boolean bl = super.tryAttack(target);
        if (bl) {
            float f = this.getWorld().getLocalDifficulty(this.getBlockPos()).getLocalDifficulty();
            if (this.getMainHandStack().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
                target.setOnFireFor(2 * (int) f);
            }

            if (!this.isBaby() && this.random.nextBetween(0, 4) == 0) {
                Vec3d vel = target.getVelocity().multiply(3);
                target.setVelocity(vel);

                ItemEntity itemEntity = this.dropItem(Items.GUNPOWDER);
                if (itemEntity != null) {
                    itemEntity.setCovetedItem();
                }

                if (!this.getWorld().isClient) {
                    target.setOnFireFor(2);
                }

                this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            }
        }

        return bl;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static DefaultAttributeContainer.Builder createZombieAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 70.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_ARMOR, 10.0)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS);
    }
}
