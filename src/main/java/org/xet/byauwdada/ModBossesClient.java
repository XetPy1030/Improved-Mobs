package org.xet.byauwdada;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import org.xet.byauwdada.client.model.ImprovedZombieModel;
import org.xet.byauwdada.client.renderer.ImprovedZombieRenderer;

@Environment(EnvType.CLIENT)
public class ModBossesClient implements ClientModInitializer {
    public static final EntityModelLayer MODEL_IMPROVED_ZOMBIE_LAYER = new EntityModelLayer(new Identifier("improves", "zombie"), "main");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModBosses.IMPROVED_ZOMBIE, ImprovedZombieRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(MODEL_IMPROVED_ZOMBIE_LAYER, ImprovedZombieModel::getCustomTexturedModelData);
    }
}