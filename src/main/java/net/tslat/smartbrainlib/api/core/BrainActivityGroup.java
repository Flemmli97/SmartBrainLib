package net.tslat.smartbrainlib.api.core;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.tslat.smartbrainlib.api.SmartBrainOwner;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrainActivityGroup<T extends LivingEntity & SmartBrainOwner<T>> {
	private final Activity activity;
	private int priorityStart = 0;
	private final List<Task<? super T>> behaviours = new ObjectArrayList<>();
	private final Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> activityStartMemoryConditions = new ObjectOpenHashSet<>();
	@Nullable
	private Set<MemoryModuleType<?>> wipedMemoriesOnFinish = null;

	public BrainActivityGroup(Activity activity) {
		this.activity = activity;
	}

	public BrainActivityGroup<T> priority(int priorityStart) {
		this.priorityStart = priorityStart;

		return this;
	}

	public BrainActivityGroup<T> behaviours(Task<? super T>... behaviours) {
		this.behaviours.addAll(new ObjectArrayList<>(behaviours));

		return this;
	}

	public BrainActivityGroup<T> onlyStartWithMemoryStatus(MemoryModuleType<?> memory, MemoryModuleStatus status) {
		this.activityStartMemoryConditions.add(Pair.of(memory, status));

		return this;
	}

	public BrainActivityGroup<T> wipeMemoriesWhenFinished(MemoryModuleType<?>... memories) {
		if (this.wipedMemoriesOnFinish == null) {
			this.wipedMemoriesOnFinish = new ObjectOpenHashSet<>(memories);
		}
		else {
			this.wipedMemoriesOnFinish.addAll(new ObjectOpenHashSet<>(memories));
		}

		return this;
	}

	public BrainActivityGroup<T> requireAndWipeMemoriesOnUse(MemoryModuleType<?>... memories) {
		for (MemoryModuleType<?> memory : memories) {
			onlyStartWithMemoryStatus(memory, MemoryModuleStatus.VALUE_PRESENT);
		}

		wipeMemoriesWhenFinished(memories);

		return this;
	}

	public Activity getActivity() {
		return this.activity;
	}

	public List<Task<? super T>> getBehaviours() {
		return this.behaviours;
	}

	public int getPriorityStart() {
		return this.priorityStart;
	}

	public Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getActivityStartMemoryConditions() {
		return this.activityStartMemoryConditions;
	}

	public Set<MemoryModuleType<?>> getWipedMemoriesOnFinish() {
		return this.wipedMemoriesOnFinish != null ? this.wipedMemoriesOnFinish : new HashSet<>();
	}

	public ImmutableList<Pair<Integer, Task<? super T>>> pairBehaviourPriorities() {
		int priority = this.priorityStart;
		ImmutableList.Builder<Pair<Integer, Task<? super T>>> pairedBehaviours = ImmutableList.builder();

		for (Task<? super T> behaviour : this.behaviours) {
			pairedBehaviours.add(Pair.of(priority++, behaviour));
		}

		return pairedBehaviours.build();
	}

	public static <T extends LivingEntity & SmartBrainOwner<T>> BrainActivityGroup<T> empty() {
		return new BrainActivityGroup<>(Activity.REST);
	}

	public static <T extends LivingEntity & SmartBrainOwner<T>> BrainActivityGroup<T> coreTasks(Task<? super T>... behaviours) {
		return new BrainActivityGroup<T>(Activity.CORE).priority(0).behaviours(behaviours);
	}

	public static <T extends LivingEntity & SmartBrainOwner<T>> BrainActivityGroup<T> idleTasks(Task<? super T>... behaviours) {
		return new BrainActivityGroup<T>(Activity.IDLE).priority(10).behaviours(behaviours);
	}

	public static <T extends LivingEntity & SmartBrainOwner<T>> BrainActivityGroup<T> fightTasks(Task<? super T>... behaviours) {
		return new BrainActivityGroup<T>(Activity.FIGHT).priority(10).behaviours(behaviours).requireAndWipeMemoriesOnUse(MemoryModuleType.ATTACK_TARGET);
	}
}
