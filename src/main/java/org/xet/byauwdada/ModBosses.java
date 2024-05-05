package org.xet.byauwdada;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.xet.byauwdada.entity.ImprovedZombie;

public class ModBosses implements ModInitializer {
    public static final EntityType<ImprovedZombie> IMPROVED_ZOMBIE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("improves", "zombie"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ImprovedZombie::new).dimensions(EntityDimensions.fixed(0.75f, 0.75f)).build()
    );

    @Override
    public void onInitialize() {
        /*
         * Register our Cube Entity's default attributes.
         * Attributes are properties or stats of the mobs, including things like attack damage and health.
         * The game will crash if the entity doesn't have the proper attributes registered in time.
         *
         * In 1.15, this was done by a method override inside the entity class.
         * Most vanilla entities have a static method (eg. ZombieEntity#createZombieAttributes) for initializing their attributes.
         */
        FabricDefaultAttributeRegistry.register(IMPROVED_ZOMBIE, ImprovedZombie.createWitherAttributes());
    }
}
