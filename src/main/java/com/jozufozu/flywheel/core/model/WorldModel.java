package com.jozufozu.flywheel.core.model;

import java.util.Collection;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.util.BufferBuilderReader;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class WorldModel implements IModel {

	private final BufferBuilderReader reader;
	private final String name;

	public WorldModel(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks, String name) {
		reader = new BufferBuilderReader(ModelUtil.getBufferBuilderFromTemplate(renderWorld, layer, blocks));
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void buffer(VecBuffer vertices) {
		for (int i = 0; i < vertexCount(); i++) {
			vertices.putVec3(reader.getX(i), reader.getY(i), reader.getZ(i));

			vertices.putVec3(reader.getNX(i), reader.getNY(i), reader.getNZ(i));

			vertices.putVec2(reader.getU(i), reader.getV(i));

			vertices.putColor(reader.getR(i), reader.getG(i), reader.getB(i), reader.getA(i));

			int light = reader.getLight(i);

			byte block = (byte) (LightTexture.block(light) << 4);
			byte sky = (byte) (LightTexture.sky(light) << 4);

			vertices.putVec2(block, sky);
		}
	}

	@Override
	public int vertexCount() {
		return reader.getVertexCount();
	}

	@Override
	public VertexFormat format() {
		return Formats.COLORED_LIT_MODEL;
	}

}
