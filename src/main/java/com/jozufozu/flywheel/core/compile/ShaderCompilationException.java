package com.jozufozu.flywheel.core.compile;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;

public class ShaderCompilationException extends ShaderLoadingException {

	private final int shaderHandle;

	private String errors = "";

	public ShaderCompilationException(String message, int shaderHandle) {
		super(message);
		this.shaderHandle = shaderHandle;
	}

	public ShaderLoadingException withErrorLog(CompilationContext ctx) {
		if (this.shaderHandle == -1)
			return this;

		this.errors = ctx.parseErrors(GL20.glGetShaderInfoLog(this.shaderHandle));

		return this;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + '\n' + this.errors;
	}
}