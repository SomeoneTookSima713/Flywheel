package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.api.instance.IDynamicInstance;
import com.jozufozu.flywheel.backend.api.instance.ITickableInstance;
import com.jozufozu.flywheel.backend.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancingEngine;
import com.jozufozu.flywheel.light.LightUpdater;
import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public abstract class InstanceManager<T> implements InstancingEngine.OriginShiftListener {

	public final MaterialManager materialManager;

	private final Set<T> queuedAdditions;
	private final Set<T> queuedUpdates;

	protected final Map<T, AbstractInstance> instances;
	protected final Object2ObjectOpenHashMap<T, ITickableInstance> tickableInstances;
	protected final Object2ObjectOpenHashMap<T, IDynamicInstance> dynamicInstances;

	protected int frame;
	protected int tick;

	public InstanceManager(MaterialManager materialManager) {
		this.materialManager = materialManager;
		this.queuedUpdates = new HashSet<>(64);
		this.queuedAdditions = new HashSet<>(64);
		this.instances = new HashMap<>();

		this.dynamicInstances = new Object2ObjectOpenHashMap<>();
		this.tickableInstances = new Object2ObjectOpenHashMap<>();
	}

	/**
	 * Is the given object capable of being instanced at all?
	 *
	 * @return false if on object cannot be instanced.
	 */
	protected abstract boolean canInstance(T obj);

	/**
	 * Is the given object currently capable of being instanced?
	 *
	 * <p>
	 *     This won't be the case for TEs or entities that are outside of loaded chunks.
	 * </p>
	 *
	 * @return true if the object is currently capable of being instanced.
	 */
	protected abstract boolean canCreateInstance(T obj);

	@Nullable
	protected abstract AbstractInstance createRaw(T obj);

	/**
	 * Ticks the InstanceManager.
	 *
	 * <p>
	 *     {@link ITickableInstance}s get ticked.
	 *     <br>
	 *     Queued updates are processed.
	 * </p>
	 */
	public void tick(double cameraX, double cameraY, double cameraZ) {
		tick++;
		processQueuedUpdates();

		// integer camera pos as a micro-optimization
		int cX = (int) cameraX;
		int cY = (int) cameraY;
		int cZ = (int) cameraZ;

		if (tickableInstances.size() > 0) {
			tickableInstances.object2ObjectEntrySet().parallelStream().forEach(e -> {
				ITickableInstance instance = e.getValue();
				if (!instance.decreaseTickRateWithDistance()) {
					instance.tick();
					return;
				}

				BlockPos pos = instance.getWorldPosition();

				int dX = pos.getX() - cX;
				int dY = pos.getY() - cY;
				int dZ = pos.getZ() - cZ;

				if ((tick % getUpdateDivisor(dX, dY, dZ)) == 0) instance.tick();
			});
		}
	}

	public void beginFrame(Camera info) {
		frame++;
		processQueuedAdditions();

		Vector3f look = info.getLookVector();
		float lookX = look.x();
		float lookY = look.y();
		float lookZ = look.z();

		// integer camera pos
		int cX = (int) info.getPosition().x;
		int cY = (int) info.getPosition().y;
		int cZ = (int) info.getPosition().z;

		if (dynamicInstances.size() > 0) {
			dynamicInstances.object2ObjectEntrySet()
					.parallelStream()
					.forEach(e -> {
						IDynamicInstance dyn = e.getValue();
						if (!dyn.decreaseFramerateWithDistance() || shouldFrameUpdate(dyn.getWorldPosition(), lookX, lookY, lookZ, cX, cY, cZ))
							dyn.beginFrame();
					});
		}
	}

	public void add(T obj) {
		if (!Backend.getInstance()
				.canUseInstancing()) return;

		if (canInstance(obj)) {
			addInternal(obj);
		}
	}

	public void queueAdd(T obj) {
		if (!Backend.getInstance()
				.canUseInstancing()) return;

		synchronized (queuedAdditions) {
			queuedAdditions.add(obj);
		}
	}

	public void queueUpdate(T obj) {
		if (!Backend.getInstance()
				.canUseInstancing()) return;
		synchronized (queuedUpdates) {
			queuedUpdates.add(obj);
		}
	}

	/**
	 * Update the instance associated with an object.
	 *
	 * <p>
	 *     By default this is the only hook an IInstance has to change its internal state. This is the lowest frequency
	 *     update hook IInstance gets. For more frequent updates, see {@link ITickableInstance} and
	 *     {@link IDynamicInstance}.
	 * </p>
	 *
	 * @param obj the object to update.
	 */
	public void update(T obj) {
		if (!Backend.getInstance()
				.canUseInstancing()) return;

		if (canInstance(obj)) {
			AbstractInstance instance = getInstance(obj);

			if (instance != null) {

				// resetting instances is by default used to handle block state changes.
				if (instance.shouldReset()) {
					// delete and re-create the instance.
					// resetting an instance supersedes updating it.
					removeInternal(obj, instance);
					createInternal(obj);
				} else {
					instance.update();
				}
			}
		}
	}

	public void remove(T obj) {
		if (!Backend.getInstance()
				.canUseInstancing()) return;

		if (canInstance(obj)) {
			AbstractInstance instance = getInstance(obj);
			if (instance != null) removeInternal(obj, instance);
		}
	}

	public void invalidate() {
		instances.values().forEach(AbstractInstance::remove);
		instances.clear();
		dynamicInstances.clear();
		tickableInstances.clear();
	}

	@Nullable
	protected <I extends T> AbstractInstance getInstance(I obj) {
		if (!Backend.getInstance()
				.canUseInstancing()) return null;

		return instances.get(obj);
	}

	protected void processQueuedAdditions() {
		if (queuedAdditions.isEmpty()) {
			return;
		}

		ArrayList<T> queued;

		synchronized (queuedAdditions) {
			queued = new ArrayList<>(queuedAdditions);
			queuedAdditions.clear();
		}

		if (!queued.isEmpty()) {
			queued.forEach(this::addInternal);
		}
	}

	protected void processQueuedUpdates() {
		ArrayList<T> queued;

		synchronized (queuedUpdates) {
			queued = new ArrayList<>(queuedUpdates);
			queuedUpdates.clear();
		}

		if (queued.size() > 0) {
			queued.forEach(this::update);
		}
	}

	protected boolean shouldFrameUpdate(BlockPos worldPos, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		int dX = worldPos.getX() - cX;
		int dY = worldPos.getY() - cY;
		int dZ = worldPos.getZ() - cZ;

		// is it more than 2 blocks behind the camera?
		int dist = 2;
		float dot = (dX + lookX * dist) * lookX + (dY + lookY * dist) * lookY + (dZ + lookZ * dist) * lookZ;
		if (dot < 0) return false;

		return (frame % getUpdateDivisor(dX, dY, dZ)) == 0;
	}

	// 1 followed by the prime numbers
	private static final int[] divisorSequence = new int[] { 1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31 };
	protected int getUpdateDivisor(int dX, int dY, int dZ) {
		int dSq = dX * dX + dY * dY + dZ * dZ;

		int i = (dSq / 2048);

		return divisorSequence[Mth.clamp(i, 0, divisorSequence.length - 1)];
	}

	protected void addInternal(T obj) {
		if (!Backend.getInstance()
				.canUseInstancing()) return;

		AbstractInstance instance = instances.get(obj);

		if (instance == null && canCreateInstance(obj)) {
			createInternal(obj);
		}
	}

	protected void removeInternal(T obj, AbstractInstance instance) {
		instance.remove();
		instances.remove(obj);
		dynamicInstances.remove(obj);
		tickableInstances.remove(obj);
		LightUpdater.get(instance.world)
				.removeListener(instance);
	}

	@Nullable
	protected AbstractInstance createInternal(T obj) {
		AbstractInstance renderer = createRaw(obj);

		if (renderer != null) {
			renderer.init();
			renderer.updateLight();
			LightUpdater.get(renderer.world)
					.addListener(renderer);
			instances.put(obj, renderer);

			if (renderer instanceof ITickableInstance r) {
				tickableInstances.put(obj, r);
				r.tick();
			}

			if (renderer instanceof IDynamicInstance r) {
				dynamicInstances.put(obj, r);
				r.beginFrame();
			}
		}

		return renderer;
	}

	@Override
	public void onOriginShift() {
		ArrayList<T> instancedTiles = new ArrayList<>(instances.keySet());
		invalidate();
		instancedTiles.forEach(this::add);
	}

	public void detachLightListeners() {
		for (AbstractInstance value : instances.values()) {
			LightUpdater.get(value.world).removeListener(value);
		}
	}
}
