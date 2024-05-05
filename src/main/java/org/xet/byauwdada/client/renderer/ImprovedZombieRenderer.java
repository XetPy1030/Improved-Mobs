package org.xet.byauwdada.client.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.WitherEntityRenderer;
import net.minecraft.client.render.entity.feature.WitherArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WitherEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.xet.byauwdada.ModBossesClient;
import org.xet.byauwdada.client.model.ImprovedZombieModel;
import org.xet.byauwdada.entity.ImprovedZombie;

public class ImprovedZombieRenderer extends MobEntityRenderer<ImprovedZombie, ImprovedZombieModel<ImprovedZombie>> {
    private static final Identifier INVULNERABLE_TEXTURE = new Identifier("textures/entity/wither/wither_invulnerable.png");
    private static final Identifier TEXTURE = new Identifier("textures/entity/wither/wither.png");

    public ImprovedZombieRenderer(EntityRendererFactory.Context context) {
        super(context, new ImprovedZombieModel<>(context.getPart(EntityModelLayers.WITHER)), 1.0F);
        this.addFeature(new ImprovedZombieArmorFeatureRenderer(this, context.getModelLoader()));
    }

    protected int getBlockLight(ImprovedZombie witherEntity, BlockPos blockPos) {
        return 15;
    }

    public Identifier getTexture(ImprovedZombie witherEntity) {
        int i = witherEntity.getInvulnerableTimer();
        return i > 0 && (i > 80 || i / 5 % 2 != 1) ? INVULNERABLE_TEXTURE : TEXTURE;
    }

    protected void scale(ImprovedZombie witherEntity, MatrixStack matrixStack, float f) {
        float g = 2.0F;
        int i = witherEntity.getInvulnerableTimer();
        if (i > 0) {
            g -= ((float)i - f) / 220.0F * 0.5F;
        }

        matrixStack.scale(g, g, g);
    }
}
