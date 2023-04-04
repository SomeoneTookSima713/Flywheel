package com.jozufozu.flywheel.backend.instancing.storage;

import java.util.List;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.TickableInstance;

public interface Storage<T> {
	Iterable<Instance> getAllInstances();

	int getInstanceCount();

	List<TickableInstance> getInstancesForTicking();

	List<DynamicInstance> getInstancesForUpdate();

	void add(T obj);

	void remove(T obj);

	void update(T obj);

	void recreateAll();

	void invalidate();
}
