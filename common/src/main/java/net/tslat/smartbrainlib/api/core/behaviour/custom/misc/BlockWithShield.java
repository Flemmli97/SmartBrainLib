package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemUseAnimation;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;
import java.util.function.Predicate;

/**
 * Makes the entity use (block) using a shield if it's currently in the entity's hands
 */
public class BlockWithShield<E extends LivingEntity> extends ExtendedBehaviour<E> {
	protected InteractionHand hand = InteractionHand.MAIN_HAND;

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return List.of();
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		if (entity.getMainHandItem().getUseAnimation() == ItemUseAnimation.BLOCK) {
			this.hand = InteractionHand.MAIN_HAND;

			return true;
		}
		else if (entity.getOffhandItem().getUseAnimation() == ItemUseAnimation.BLOCK) {
			this.hand = InteractionHand.OFF_HAND;

			return true;
		}

		return false;
	}

	@Override
	protected void start(E entity) {
		entity.startUsingItem(this.hand);
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return entity.isUsingItem() && entity.getUseItem().getUseAnimation() == ItemUseAnimation.BLOCK;
    }

	@Override
	protected void stop(E entity) {
		if (entity.getUseItem().getUseAnimation() == ItemUseAnimation.BLOCK)
			entity.stopUsingItem();
	}
}