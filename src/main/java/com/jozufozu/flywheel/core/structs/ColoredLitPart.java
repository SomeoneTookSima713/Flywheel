package com.jozufozu.flywheel.core.structs;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.util.Color;

import net.minecraft.client.renderer.LightTexture;

public abstract class ColoredLitPart extends InstancedPart implements FlatLit<ColoredLitPart> {

	public byte blockLight;
	public byte skyLight;

	public byte r = (byte) 0xFF;
	public byte g = (byte) 0xFF;
	public byte b = (byte) 0xFF;
	public byte a = (byte) 0xFF;

	public ColoredLitPart(StructType<?> type) {
		super(type);
	}

	@Override
	public ColoredLitPart setBlockLight(int blockLight) {
		this.blockLight = (byte) blockLight;
		markDirty();
		return this;
	}

	@Override
	public ColoredLitPart setSkyLight(int skyLight) {
		this.skyLight = (byte) skyLight;
		markDirty();
		return this;
	}

	@Override
	public int getPackedLight() {
		return LightTexture.pack(this.blockLight, this.skyLight);
	}

	public ColoredLitPart setColor(Color color) {
		this.r = (byte) color.getRed();
		this.g = (byte) color.getGreen();
		this.b = (byte) color.getBlue();
		this.a = (byte) color.getAlpha();
		markDirty();
		return this;
	}

	public ColoredLitPart setColor(int color) {
		return setColor(color, false);
	}

	public ColoredLitPart setColor(int color, boolean alpha) {
		byte r = (byte) ((color >> 16) & 0xFF);
		byte g = (byte) ((color >> 8) & 0xFF);
		byte b = (byte) (color & 0xFF);

		if (alpha) {
			byte a = (byte) ((color >> 24) & 0xFF);
			return setColor(r, g, b, a);
		} else {
			return setColor(r, g, b);
		}
	}

	public ColoredLitPart setColor(int r, int g, int b) {
		return setColor((byte) r, (byte) g, (byte) b);
	}

	public ColoredLitPart setColor(byte r, byte g, byte b) {
		this.r = r;
		this.g = g;
		this.b = b;
		markDirty();
		return this;
	}

	public ColoredLitPart setColor(byte r, byte g, byte b, byte a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		markDirty();
		return this;
	}
}