package net.revilodev.runic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.effect.ModMobEffects;
import org.joml.Matrix4f;

public class FrozenIceLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "renderlayer/ice.png");
    private static final int COLOR = 0xAAFFFFFF;

    public FrozenIceLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.hasEffect(ModMobEffects.FROZEN)) {
            return;
        }

        poseStack.pushPose();
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        float minX = -0.5F;
        float maxX = 0.5F;
        float minY = 0.0F;
        float maxY = 2.0F;
        float minZ = -0.5F;
        float maxZ = 0.5F;

        face(vertices, matrix, pose, packedLight, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, 0.0F, 0.0F, 1.0F);
        face(vertices, matrix, pose, packedLight, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, 0.0F, 0.0F, -1.0F);
        face(vertices, matrix, pose, packedLight, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, -1.0F, 0.0F, 0.0F);
        face(vertices, matrix, pose, packedLight, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, 1.0F, 0.0F, 0.0F);
        face(vertices, matrix, pose, packedLight, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, 0.0F, 1.0F, 0.0F);
        face(vertices, matrix, pose, packedLight, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, 0.0F, -1.0F, 0.0F);
        poseStack.popPose();
    }

    private static void face(VertexConsumer vertices, Matrix4f matrix, PoseStack.Pose pose, int light,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float nx, float ny, float nz) {
        vertex(vertices, matrix, pose, light, x1, y1, z1, 0.0F, 1.0F, nx, ny, nz);
        vertex(vertices, matrix, pose, light, x2, y2, z2, 1.0F, 1.0F, nx, ny, nz);
        vertex(vertices, matrix, pose, light, x3, y3, z3, 1.0F, 0.0F, nx, ny, nz);
        vertex(vertices, matrix, pose, light, x4, y4, z4, 0.0F, 0.0F, nx, ny, nz);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, PoseStack.Pose pose, int light,
                               float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        vertices.addVertex(matrix, x, y, z)
                .setColor(COLOR)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
