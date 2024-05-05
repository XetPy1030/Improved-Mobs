package org.xet.byauwdada.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.entity.feature.SkinOverlayOwner;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class ImprovedZombie extends HostileEntity implements SkinOverlayOwner, RangedAttackMob {
    public ImprovedZombie(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = (ServerBossBar)(new ServerBossBar(this.getDisplayName(), BossBar.Color.PURPLE, BossBar.Style.PROGRESS)).setDarkenSky(true);
        this.moveControl = new FlightMoveControl(this, 10, false);
        this.setHealth(this.getMaxHealth());
        this.experiencePoints = 50;
    }

    public static DefaultAttributeContainer.Builder createCustomWitherAttributes() {
        return WitherEntity.createWitherAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 2)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 2)
                ;
    }




    private static final TrackedData<Integer> TRACKED_ENTITY_ID_1;
    private static final TrackedData<Integer> TRACKED_ENTITY_ID_2;
    private static final TrackedData<Integer> TRACKED_ENTITY_ID_3;
    private static final List<TrackedData<Integer>> TRACKED_ENTITY_IDS;
    private static final TrackedData<Integer> INVUL_TIMER;
    private static final int DEFAULT_INVUL_TIMER = 220;
    private final float[] sideHeadPitches = new float[2];
    private final float[] sideHeadYaws = new float[2];
    private final float[] prevSideHeadPitches = new float[2];
    private final float[] prevSideHeadYaws = new float[2];
//    private final int[] skullCooldowns = new int[2];
    private final int[] skullCooldowns = new int[2];
//    private final int[] chargedSkullCooldowns = new int[2];
    private final int[] chargedSkullCooldowns = new int[2];
    private int blockBreakingCooldown;
    private final ServerBossBar bossBar;
    private static final Predicate<LivingEntity> CAN_ATTACK_PREDICATE;
    private static final TargetPredicate HEAD_TARGET_PREDICATE;

    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world);
        birdNavigation.setCanPathThroughDoors(false);
        birdNavigation.setCanSwim(true);
        birdNavigation.setCanEnterOpenDoors(true);
        return birdNavigation;
    }

    protected void initGoals() {
        this.goalSelector.add(0, new ImprovedZombie.DescendAtHalfHealthGoal());
        this.goalSelector.add(2, new ProjectileAttackGoal(this, 1.0, 40, 20.0F));
        this.goalSelector.add(5, new FlyGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(2, new ActiveTargetGoal(this, LivingEntity.class, 0, false, false, CAN_ATTACK_PREDICATE));
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(TRACKED_ENTITY_ID_1, 0);
        this.dataTracker.startTracking(TRACKED_ENTITY_ID_2, 0);
        this.dataTracker.startTracking(TRACKED_ENTITY_ID_3, 0);
        this.dataTracker.startTracking(INVUL_TIMER, 0);
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Invul", this.getInvulnerableTimer());
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setInvulTimer(nbt.getInt("Invul"));
        if (this.hasCustomName()) {
            this.bossBar.setName(this.getDisplayName());
        }

    }

    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_WITHER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WITHER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITHER_DEATH;
    }

    public void tickMovement() {
        Vec3d vec3d = this.getVelocity().multiply(1.0, 0.6, 1.0);
        if (!this.getWorld().isClient && this.getTrackedEntityId(0) > 0) {
            Entity entity = this.getWorld().getEntityById(this.getTrackedEntityId(0));
            if (entity != null) {
                double d = vec3d.y;
                if (this.getY() < entity.getY() || !this.shouldRenderOverlay() && this.getY() < entity.getY() + 5.0) {
                    d = Math.max(0.0, d);
                    d += 0.3 - d * 0.6000000238418579;
                }

                vec3d = new Vec3d(vec3d.x, d, vec3d.z);
                Vec3d vec3d2 = new Vec3d(entity.getX() - this.getX(), 0.0, entity.getZ() - this.getZ());
                if (vec3d2.horizontalLengthSquared() > 9.0) {
                    Vec3d vec3d3 = vec3d2.normalize();
                    vec3d = vec3d.add(vec3d3.x * 0.3 - vec3d.x * 0.6, 0.0, vec3d3.z * 0.3 - vec3d.z * 0.6);
                }
            }
        }

        this.setVelocity(vec3d);
        if (vec3d.horizontalLengthSquared() > 0.05) {
            this.setYaw((float) MathHelper.atan2(vec3d.z, vec3d.x) * 57.295776F - 90.0F);
        }

        super.tickMovement();

        int i;
        for(i = 0; i < 2; ++i) {
            this.prevSideHeadYaws[i] = this.sideHeadYaws[i];
            this.prevSideHeadPitches[i] = this.sideHeadPitches[i];
        }

        int j;
        for(i = 0; i < 2; ++i) {
            j = this.getTrackedEntityId(i + 1);
            Entity entity2 = null;
            if (j > 0) {
                entity2 = this.getWorld().getEntityById(j);
            }

            if (entity2 != null) {
                double e = this.getHeadX(i + 1);
                double f = this.getHeadY(i + 1);
                double g = this.getHeadZ(i + 1);
                double h = entity2.getX() - e;
                double k = entity2.getEyeY() - f;
                double l = entity2.getZ() - g;
                double m = Math.sqrt(h * h + l * l);
                float n = (float)(MathHelper.atan2(l, h) * 57.2957763671875) - 90.0F;
                float o = (float)(-(MathHelper.atan2(k, m) * 57.2957763671875));
                this.sideHeadPitches[i] = this.getNextAngle(this.sideHeadPitches[i], o, 40.0F);
                this.sideHeadYaws[i] = this.getNextAngle(this.sideHeadYaws[i], n, 10.0F);
            } else {
                this.sideHeadYaws[i] = this.getNextAngle(this.sideHeadYaws[i], this.bodyYaw, 10.0F);
            }
        }

        boolean bl = this.shouldRenderOverlay();

        for(j = 0; j < 3; ++j) {
            double p = this.getHeadX(j);
            double q = this.getHeadY(j);
            double r = this.getHeadZ(j);
            this.getWorld().addParticle(ParticleTypes.SMOKE, p + this.random.nextGaussian() * 0.30000001192092896, q + this.random.nextGaussian() * 0.30000001192092896, r + this.random.nextGaussian() * 0.30000001192092896, 0.0, 0.0, 0.0);
            if (bl && this.getWorld().random.nextInt(4) == 0) {
                this.getWorld().addParticle(ParticleTypes.ENTITY_EFFECT, p + this.random.nextGaussian() * 0.30000001192092896, q + this.random.nextGaussian() * 0.30000001192092896, r + this.random.nextGaussian() * 0.30000001192092896, 0.699999988079071, 0.699999988079071, 0.5);
            }
        }

        if (this.getInvulnerableTimer() > 0) {
            for(j = 0; j < 3; ++j) {
                this.getWorld().addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + this.random.nextGaussian(), this.getY() + (double)(this.random.nextFloat() * 3.3F), this.getZ() + this.random.nextGaussian(), 0.699999988079071, 0.699999988079071, 0.8999999761581421);
            }
        }

    }

    protected void mobTick() {
        int i;
        if (this.getInvulnerableTimer() > 0) {
            i = this.getInvulnerableTimer() - 1;
            this.bossBar.setPercent(1.0F - (float)i / 220.0F);
            if (i <= 0) {
                this.getWorld().createExplosion(this, this.getX(), this.getEyeY(), this.getZ(), 7.0F, false, World.ExplosionSourceType.MOB);
                if (!this.isSilent()) {
                    this.getWorld().syncGlobalEvent(WorldEvents.WITHER_SPAWNS, this.getBlockPos(), 0);
                }
            }

            this.setInvulTimer(i);
            if (this.age % 10 == 0) {
                this.heal(10.0F);
            }

        } else {
            super.mobTick();

            int j;
            for(i = 1; i < 3; ++i) {
                if (this.age >= this.skullCooldowns[i - 1]) {
//                    this.skullCooldowns[i - 1] = this.age + 10 + this.random.nextInt(10);
                    this.skullCooldowns[i - 1] = this.random.nextInt(3);
                    if (this.getWorld().getDifficulty() == Difficulty.NORMAL || this.getWorld().getDifficulty() == Difficulty.HARD) {
                        int[] var10000 = this.chargedSkullCooldowns;
                        int var10001 = i - 1;
                        int var10003 = var10000[i - 1];
                        var10000[var10001] = var10000[i - 1] + 1;
//                        if (var10003 > 15) {
                        if (var10003 > 4) {
                            float f = 10.0F;
                            float g = 5.0F;
                            double d = MathHelper.nextDouble(this.random, this.getX() - 10.0, this.getX() + 10.0);
                            double e = MathHelper.nextDouble(this.random, this.getY() - 5.0, this.getY() + 5.0);
                            double h = MathHelper.nextDouble(this.random, this.getZ() - 10.0, this.getZ() + 10.0);
                            this.shootSkullAt(i + 1, d, e, h, true);
                            this.chargedSkullCooldowns[i - 1] = 0;
                        }
                    }

                    j = this.getTrackedEntityId(i);
                    if (j > 0) {
                        LivingEntity livingEntity = (LivingEntity)this.getWorld().getEntityById(j);
                        if (livingEntity != null && this.canTarget(livingEntity) && !(this.squaredDistanceTo(livingEntity) > 900.0) && this.canSee(livingEntity)) {
                            this.shootSkullAt(i + 1, livingEntity);
//                            this.skullCooldowns[i - 1] = this.age + 40 + this.random.nextInt(20);
                            this.skullCooldowns[i - 1] = this.random.nextInt(10);
                            this.chargedSkullCooldowns[i - 1] = 0;
                        } else {
                            this.setTrackedEntityId(i, 0);
                        }
                    } else {
                        List<LivingEntity> list = this.getWorld().getTargets(LivingEntity.class, HEAD_TARGET_PREDICATE, this, this.getBoundingBox().expand(20.0, 8.0, 20.0));
                        if (!list.isEmpty()) {
                            LivingEntity livingEntity2 = (LivingEntity)list.get(this.random.nextInt(list.size()));
                            this.setTrackedEntityId(i, livingEntity2.getId());
                        }
                    }
                }
            }

            if (this.getTarget() != null) {
                this.setTrackedEntityId(0, this.getTarget().getId());
            } else {
                this.setTrackedEntityId(0, 0);
            }

            if (this.blockBreakingCooldown > 0) {
                --this.blockBreakingCooldown;
                if (this.blockBreakingCooldown == 0 && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                    i = MathHelper.floor(this.getY());
                    j = MathHelper.floor(this.getX());
                    int k = MathHelper.floor(this.getZ());
                    boolean bl = false;

                    for(int l = -1; l <= 1; ++l) {
                        for(int m = -1; m <= 1; ++m) {
                            for(int n = 0; n <= 3; ++n) {
                                int o = j + l;
                                int p = i + n;
                                int q = k + m;
                                BlockPos blockPos = new BlockPos(o, p, q);
                                BlockState blockState = this.getWorld().getBlockState(blockPos);
                                if (canDestroy(blockState)) {
                                    bl = this.getWorld().breakBlock(blockPos, true, this) || bl;
                                }
                            }
                        }
                    }

                    if (bl) {
                        this.getWorld().syncWorldEvent((PlayerEntity)null, WorldEvents.WITHER_BREAKS_BLOCK, this.getBlockPos(), 0);
                    }
                }
            }

            if (this.age % 20 == 0) {
                this.heal(1.0F);
            }

            this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        }
    }

    public static boolean canDestroy(BlockState block) {
        return !block.isAir() && !block.isIn(BlockTags.WITHER_IMMUNE);
    }

    public void onSummoned() {
        this.setInvulTimer(220);
        this.bossBar.setPercent(0.0F);
        this.setHealth(this.getMaxHealth() / 3.0F);
    }

    public void slowMovement(BlockState state, Vec3d multiplier) {
    }

    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    private double getHeadX(int headIndex) {
        if (headIndex <= 0) {
            return this.getX();
        } else {
            float f = (this.bodyYaw + (float)(180 * (headIndex - 1))) * 0.017453292F;
            float g = MathHelper.cos(f);
            return this.getX() + (double)g * 1.3;
        }
    }

    private double getHeadY(int headIndex) {
        return headIndex <= 0 ? this.getY() + 3.0 : this.getY() + 2.2;
    }

    private double getHeadZ(int headIndex) {
        if (headIndex <= 0) {
            return this.getZ();
        } else {
            float f = (this.bodyYaw + (float)(180 * (headIndex - 1))) * 0.017453292F;
            float g = MathHelper.sin(f);
            return this.getZ() + (double)g * 1.3;
        }
    }

    private float getNextAngle(float prevAngle, float desiredAngle, float maxDifference) {
        float f = MathHelper.wrapDegrees(desiredAngle - prevAngle);
        if (f > maxDifference) {
            f = maxDifference;
        }

        if (f < -maxDifference) {
            f = -maxDifference;
        }

        return prevAngle + f;
    }

    private void shootSkullAt(int headIndex, LivingEntity target) {
        this.shootSkullAt(headIndex, target.getX(), target.getY() + (double)target.getStandingEyeHeight() * 0.5, target.getZ(), headIndex == 0 && this.random.nextFloat() < 0.001F);
    }

    private void shootSkullAt(int headIndex, double targetX, double targetY, double targetZ, boolean charged) {
        if (!this.isSilent()) {
            this.getWorld().syncWorldEvent((PlayerEntity)null, WorldEvents.WITHER_SHOOTS, this.getBlockPos(), 0);
        }

        double d = this.getHeadX(headIndex);
        double e = this.getHeadY(headIndex);
        double f = this.getHeadZ(headIndex);
        double g = targetX - d;
        double h = targetY - e;
        double i = targetZ - f;
        WitherSkullEntity witherSkullEntity = new WitherSkullEntity(this.getWorld(), this, g, h, i);
        witherSkullEntity.setOwner(this);
        if (charged) {
            witherSkullEntity.setCharged(true);
        }

        witherSkullEntity.setPos(d, e, f);
        this.getWorld().spawnEntity(witherSkullEntity);
    }

    public void shootAt(LivingEntity target, float pullProgress) {
        this.shootSkullAt(0, target);
    }

    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (!source.isIn(DamageTypeTags.WITHER_IMMUNE_TO) && !(source.getAttacker() instanceof ImprovedZombie)) {
            if (this.getInvulnerableTimer() > 0 && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                return false;
            } else {
                Entity entity;
                if (this.shouldRenderOverlay()) {
                    entity = source.getSource();
                    if (entity instanceof PersistentProjectileEntity) {
                        return false;
                    }
                }

                entity = source.getAttacker();
                if (entity != null && !(entity instanceof PlayerEntity) && entity instanceof LivingEntity && ((LivingEntity)entity).getGroup() == this.getGroup()) {
                    return false;
                } else {
                    if (this.blockBreakingCooldown <= 0) {
                        this.blockBreakingCooldown = 20;
                    }

                    for(int i = 0; i < this.chargedSkullCooldowns.length; ++i) {
                        int[] var10000 = this.chargedSkullCooldowns;
                        var10000[i] += 3;
                    }

                    return super.damage(source, amount);
                }
            }
        } else {
            return false;
        }
    }

    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        for (int i = 0; i < 20; i++) {
            ItemEntity itemEntity = this.dropItem(Items.NETHER_STAR);
            if (itemEntity != null) {
                itemEntity.setCovetedItem();
            }
        }
    }

    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == Difficulty.PEACEFUL && this.isDisallowedInPeaceful()) {
            this.discard();
        } else {
            this.despawnCounter = 0;
        }
    }

    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        return false;
    }

    public static DefaultAttributeContainer.Builder createWitherAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.6000000238418579).add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6000000238418579).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0).add(EntityAttributes.GENERIC_ARMOR, 4.0);
    }

    public float getHeadYaw(int headIndex) {
        return this.sideHeadYaws[headIndex];
    }

    public float getHeadPitch(int headIndex) {
        return this.sideHeadPitches[headIndex];
    }

    public int getInvulnerableTimer() {
        return (Integer)this.dataTracker.get(INVUL_TIMER);
    }

    public void setInvulTimer(int ticks) {
        this.dataTracker.set(INVUL_TIMER, ticks);
    }

    public int getTrackedEntityId(int headIndex) {
        return (Integer)this.dataTracker.get((TrackedData)TRACKED_ENTITY_IDS.get(headIndex));
    }

    public void setTrackedEntityId(int headIndex, int id) {
        this.dataTracker.set((TrackedData)TRACKED_ENTITY_IDS.get(headIndex), id);
    }

    public boolean shouldRenderOverlay() {
        return this.getHealth() <= this.getMaxHealth() / 2.0F;
    }

    public EntityGroup getGroup() {
        return EntityGroup.UNDEAD;
    }

    protected boolean canStartRiding(Entity entity) {
        return false;
    }

    public boolean canUsePortals() {
        return false;
    }

    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        return effect.getEffectType() == StatusEffects.WITHER ? false : super.canHaveStatusEffect(effect);
    }

    static {
        TRACKED_ENTITY_ID_1 = DataTracker.registerData(ImprovedZombie.class, TrackedDataHandlerRegistry.INTEGER);
        TRACKED_ENTITY_ID_2 = DataTracker.registerData(ImprovedZombie.class, TrackedDataHandlerRegistry.INTEGER);
        TRACKED_ENTITY_ID_3 = DataTracker.registerData(ImprovedZombie.class, TrackedDataHandlerRegistry.INTEGER);
        TRACKED_ENTITY_IDS = ImmutableList.of(TRACKED_ENTITY_ID_1, TRACKED_ENTITY_ID_2, TRACKED_ENTITY_ID_3);
        INVUL_TIMER = DataTracker.registerData(ImprovedZombie.class, TrackedDataHandlerRegistry.INTEGER);
        CAN_ATTACK_PREDICATE = (entity) -> {
            return entity.getGroup() != EntityGroup.UNDEAD && entity.isMobOrPlayer();
        };
        HEAD_TARGET_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(20.0).setPredicate(CAN_ATTACK_PREDICATE);
    }

    class DescendAtHalfHealthGoal extends Goal {
        public DescendAtHalfHealthGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP, Goal.Control.LOOK));
        }

        public boolean canStart() {
            return ImprovedZombie.this.getInvulnerableTimer() > 0;
        }
    }
}
