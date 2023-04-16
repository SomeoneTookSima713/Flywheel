package com.jozufozu.flywheel.lib.vertex;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.lib.math.RenderMath;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class PosTexNormalVertexList extends AbstractVertexList {
	private static final int STRIDE = 23;

	@Override
	public float x(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE);
	}

	@Override
	public float y(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE + 4);
	}

	@Override
	public float z(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE + 8);
	}

	@Override
	public float r(int index) {
		return 1;
	}

	@Override
	public float g(int index) {
		return 1;
	}

	@Override
	public float b(int index) {
		return 1;
	}

	@Override
	public float a(int index) {
		return 1;
	}

	@Override
	public float u(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE + 12);
	}

	@Override
	public float v(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE + 16);
	}

	@Override
	public int overlay(int index) {
		return OverlayTexture.NO_OVERLAY;
	}

	@Override
	public int light(int index) {
		return LightTexture.FULL_BRIGHT;
	}

	@Override
	public float normalX(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * STRIDE + 20));
	}

	@Override
	public float normalY(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * STRIDE + 21));
	}

	@Override
	public float normalZ(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * STRIDE + 22));
	}

	@Override
	public void x(int index, float x) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE, x);
	}

	@Override
	public void y(int index, float y) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE + 4, y);
	}

	@Override
	public void z(int index, float z) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE + 8, z);
	}

	@Override
	public void r(int index, float r) {
	}

	@Override
	public void g(int index, float g) {
	}

	@Override
	public void b(int index, float b) {
	}

	@Override
	public void a(int index, float a) {
	}

	@Override
	public void u(int index, float u) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE + 12, u);
	}

	@Override
	public void v(int index, float v) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE + 16, v);
	}

	@Override
	public void overlay(int index, int overlay) {
	}

	@Override
	public void light(int index, int light) {
	}

	@Override
	public void normalX(int index, float normalX) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 20, RenderMath.nb(normalX));
	}

	@Override
	public void normalY(int index, float normalY) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 21, RenderMath.nb(normalY));
	}

	@Override
	public void normalZ(int index, float normalZ) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 22, RenderMath.nb(normalZ));
	}

	@Override
	public void write(MutableVertexList dst, int srcIndex, int dstIndex) {
		if (getClass() == dst.getClass()) {
			long dstPtr = ((PosTexNormalVertexList) dst).ptr;
			MemoryUtil.memCopy(ptr + srcIndex * STRIDE, dstPtr + dstIndex * STRIDE, STRIDE);
		} else {
			super.write(dst, srcIndex, dstIndex);
		}
	}

	@Override
	public void write(MutableVertexList dst, int srcStartIndex, int dstStartIndex, int vertexCount) {
		if (getClass() == dst.getClass()) {
			long dstPtr = ((PosTexNormalVertexList) dst).ptr;
			MemoryUtil.memCopy(ptr + srcStartIndex * STRIDE, dstPtr + dstStartIndex * STRIDE, vertexCount * STRIDE);
		} else {
			super.write(dst, srcStartIndex, dstStartIndex, vertexCount);
		}
	}

	@Override
	public void writeAll(MutableVertexList dst) {
		if (getClass() == dst.getClass()) {
			long dstPtr = ((PosTexNormalVertexList) dst).ptr;
			MemoryUtil.memCopy(ptr, dstPtr, vertexCount * STRIDE);
		} else {
			super.writeAll(dst);
		}
	}
}