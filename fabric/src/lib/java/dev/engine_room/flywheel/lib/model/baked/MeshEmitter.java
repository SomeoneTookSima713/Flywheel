package dev.engine_room.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

import java.util.Map;

class MeshEmitter {
	private final RenderType renderType;
	private final ByteBufferBuilder buffer;
	private BufferBuilder bufferBuilder;

	private BakedModelBufferer.@UnknownNullability ResultConsumer resultConsumer;
	private boolean currentShade;

	MeshEmitter(RenderType renderType) {
		this.renderType = renderType;
		this.buffer = new ByteBufferBuilder(renderType.bufferSize());
		this.bufferBuilder = new BufferBuilder(buffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
	}

	public void prepare(BakedModelBufferer.ResultConsumer resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	public void end() {
		emit();
		resultConsumer = null;
	}

	public BufferBuilder getBuffer(boolean shade) {
		prepareForGeometry(shade);
		return bufferBuilder;
	}

	void prepareForGeometry(boolean shade) {
		bufferBuilder = new BufferBuilder(buffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

		if (shade != currentShade) {
			emit();
			bufferBuilder = new BufferBuilder(buffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		}

		currentShade = shade;
	}

	void emit() {
		MeshData meshData = bufferBuilder.build();

		if (meshData != null) {
			resultConsumer.accept(renderType, currentShade, meshData);
			meshData.close();
		}
	}
}
