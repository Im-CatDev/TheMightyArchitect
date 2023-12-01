package com.simibubi.mightyarchitect.control.weathering;

import java.util.List;

import com.simibubi.mightyarchitect.control.TemplateBlockAccess;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class HolePunchingWeatherer implements StepwiseWeathering {

	@Override
	public void step(TemplateBlockAccess level) {
		List<Stack> stacks = groundPlan().stacks;
		Stack stack = stacks.get(random().nextInt(stacks.size()));
		List<Room> floors = stack.getRooms();
		Room room = floors.get(stack.floors() - 1);
		int x = room.x + random().nextInt(room.width + 1);
		int y = level.getBounds().y + level.getBounds().height + 1;
		int z = room.z + random().nextInt(room.length + 1);
		MutableBlockPos dropPos = new BlockPos.MutableBlockPos(x, y, z);

		while (dropPos.getY() > level.getBounds().y) {
			BlockState blockState = get(level, dropPos);
			if (!blockState.isAir()) {
				activateAt(level, dropPos.immutable());
				break;
			}
			dropPos.move(Direction.DOWN);
		}
	}

	protected void activateAt(TemplateBlockAccess level, BlockPos pos) {
		boolean gravityHole = random().nextBoolean();
		boolean hardGravity = random().nextInt(4) != 0;
		int radius = random().nextInt(gravityHole ? 6 : 4) + 2;
		Vec3 center = Vec3.atCenterOf(pos);
		for (BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius),
			pos.offset(radius, radius, radius))) {
			double distanceToSqr = Vec3.atCenterOf(blockPos)
				.distanceToSqr(center);
			if (distanceToSqr > radius * radius)
				continue;
			if (distanceToSqr > (radius - 1) * (radius - 1) && random().nextBoolean())
				continue;
			if (gravityHole) {
				applyGravity(level, blockPos, hardGravity);
				continue;
			}
			set(level, blockPos, Blocks.AIR.defaultBlockState());
		}
	}

	@Override
	public boolean isDone() {
		return false;
	}

}
