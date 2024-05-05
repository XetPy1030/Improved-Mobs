package org.xet.byauwdada.client.renderer;

import net.minecraft.client.render.entity.feature.EnergySwirlOverlayFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.WitherEntityModel;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.xet.byauwdada.client.model.ImprovedZombieModel;
import org.xet.byauwdada.entity.ImprovedZombie;

public class ImprovedZombieArmorFeatureRenderer extends EnergySwirlOverlayFeatureRenderer<ImprovedZombie, ImprovedZombieModel<ImprovedZombie>> {
    private static final Identifier SKIN = new Identifier("textures/entity/wither/wither_armor.png");
    private final ImprovedZombieModel<ImprovedZombie> model;

    public ImprovedZombieArmorFeatureRenderer(FeatureRendererContext<ImprovedZombie, ImprovedZombieModel<ImprovedZombie>> context, EntityModelLoader loader) {
        super(context);
        this.model = new ImprovedZombieModel<>(loader.getModelPart(EntityModelLayers.WITHER_ARMOR));
    }

    protected float getEnergySwirlX(float partialAge) {
        return MathHelper.cos(partialAge * 0.02F) * 3.0F;
    }

    protected Identifier getEnergySwirlTexture() {
        return SKIN;
    }

    protected EntityModel<ImprovedZombie> getEnergySwirlModel() {
        return this.model;
    }
}
