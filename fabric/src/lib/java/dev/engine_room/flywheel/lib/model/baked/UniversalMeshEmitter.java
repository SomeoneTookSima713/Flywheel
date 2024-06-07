package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

class UniversalMeshEmitter implements VertexConsumer {
	private final Reference2ReferenceMap<RenderType, MeshEmitter> emitterMap;
	private final WrapperModel wrapperModel = new WrapperModel();

	@UnknownNullability
	private RenderType defaultLayer;
	@UnknownNullability
	private BufferBuilder currentDelegate;

    UniversalMeshEmitter(Reference2ReferenceMap<RenderType, MeshEmitter> emitterMap) {
		this.emitterMap = emitterMap;
    }

	public void prepare(RenderType defaultLayer) {
		this.defaultLayer = defaultLayer;
	}

	public void clear() {
		wrapperModel.setWrapped(null);
	}

	public BakedModel wrapModel(BakedModel model) {
		wrapperModel.setWrapped(model);
		return wrapperModel;
	}

	private void prepareForGeometry(RenderMaterial material) {
		BlendMode blendMode = material.blendMode();
		RenderType layer = blendMode == BlendMode.DEFAULT ? defaultLayer : blendMode.blockRenderLayer;
		boolean shade = !material.disableDiffuse();
		currentDelegate = emitterMap.get(layer).getBuffer(shade);
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		currentDelegate.addVertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		currentDelegate.setColor(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		currentDelegate.setUv(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		currentDelegate.setUv1(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		currentDelegate.setUv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		currentDelegate.setNormal(x, y, z);
		return this;
	}

	// todo - parameter mappings
	@Override
	public void addVertex(float f, float g, float h, int i, float j, float k, int l, int m, float n, float o, float p) {
		currentDelegate.addVertex(f, g, h, i, j, k, l, m, n, o, p);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay) {
		currentDelegate.putBulkData(pose, quad, red, green, blue, alpha, light, overlay);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightnesses, float red, float green, float blue, float alpha, int[] lights, int overlay, boolean readExistingColor) {
		currentDelegate.putBulkData(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
	}

	private class WrapperModel extends ForwardingBakedModel {
		private final RenderContext.QuadTransform quadTransform = quad -> {
			UniversalMeshEmitter.this.prepareForGeometry(quad.material());
			return true;
		};

		public void setWrapped(@Nullable BakedModel wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public boolean isVanillaAdapter() {
			return false;
		}

		@Override
		public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
			context.pushTransform(quadTransform);
			super.emitBlockQuads(level, state, pos, randomSupplier, context);
			context.popTransform();
		}
	}
}