package online.kingdomkeys.kingdomkeys.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import online.kingdomkeys.kingdomkeys.capability.IGlobalCapabilities;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.entity.mob.EmeraldBluesEntity;

@OnlyIn(Dist.CLIENT)
public class AeroLayerPlayerRenderer<T extends LivingEntity> extends RenderLayer<T, PlayerModel<T>> {
	   public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident_riptide.png");
	   public static final String BOX = "box";
	   private final ModelPart box;

	public AeroLayerPlayerRenderer(RenderLayerParent<T, PlayerModel<T>> p_174540_, EntityModelSet p_174541_) {
	      super(p_174540_);
	      ModelPart modelpart = p_174541_.bakeLayer(ModelLayers.PLAYER_SPIN_ATTACK);
	      this.box = modelpart.getChild("box");
	   }

	@Override
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if(ModCapabilities.getGlobal(entitylivingbaseIn) != null) {
			IGlobalCapabilities globalData = ModCapabilities.getGlobal(entitylivingbaseIn);
            float scale = 1;

			if(globalData.getAeroTicks() > 0) {
				VertexConsumer vertexconsumer = bufferIn.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

				if (entitylivingbaseIn instanceof EmeraldBluesEntity) {
					matrixStackIn.pushPose();
					matrixStackIn.scale(scale, scale * 0.6F, scale);
					matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(ageInTicks * 20));

				} else {
					for (int i = 1; i <= globalData.getAeroLevel() + 1; ++i) {
						matrixStackIn.pushPose();
						float f = ageInTicks * 20;
						if (i % 2 == 0)
							f *= -1;
						matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(f));
						switch (globalData.getAeroLevel()) {
						case 0:
							scale = 0.75F * i;
							matrixStackIn.scale(scale, scale * 1.2F, scale);
							matrixStackIn.translate(0.0D, (double) (-0.4F + 0.8F * (float) i), 0.0D);
							break;
						case 1:
							scale = 0.85F * i;
							matrixStackIn.scale(scale, scale * 1F, scale);
							matrixStackIn.translate(0.0D, (double) (-0.8F + 0.8F * (float) i), 0.0D);
							break;
						case 2:
							scale = 0.7F * i;
							matrixStackIn.scale(scale, scale * 0.6F, scale);
							matrixStackIn.translate(0.0D, (double) (-1.2F + 0.6F * (float) i), 0.0D);
							break;

						}
					}
				}

				this.box.render(matrixStackIn, vertexconsumer, packedLightIn, OverlayTexture.NO_OVERLAY);
				matrixStackIn.popPose();
		         
			}
		}
	}
}
