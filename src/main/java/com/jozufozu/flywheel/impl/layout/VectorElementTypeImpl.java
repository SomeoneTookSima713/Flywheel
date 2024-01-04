package com.jozufozu.flywheel.impl.layout;

import org.jetbrains.annotations.Range;

import com.jozufozu.flywheel.api.layout.ValueRepr;
import com.jozufozu.flywheel.api.layout.VectorElementType;

final class VectorElementTypeImpl implements VectorElementType {
	private final ValueRepr repr;
	@Range(from = 2, to = 4)
	private final int size;
	private final int byteSize;

	VectorElementTypeImpl(ValueRepr repr, @Range(from = 2, to = 4) int size) {
		if (size < 2 || size > 4) {
			throw new IllegalArgumentException("Vector element size must be in range [2, 4]!");
		}

		this.repr = repr;
		this.size = size;
		byteSize = repr.byteSize() * size;
	}

	@Override
	public ValueRepr repr() {
		return repr;
	}

	@Override
	@Range(from = 2, to = 4)
	public int size() {
		return size;
	}

	@Override
	public int byteSize() {
		return byteSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + repr.hashCode();
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		VectorElementTypeImpl other = (VectorElementTypeImpl) obj;
		return repr == other.repr && size == other.size;
	}
}
