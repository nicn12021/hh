package com.robloxify.experience;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** A small sliding platform: a 3x3 slab that travels along an axis and carries whoever stands on it. */
public final class Mover {
	public final BlockPos start;
	public final Direction direction;
	public final int travel;
	public final int periodTicks;
	public int offset;
	public int ticks;
	private int step = 1;

	public Mover(BlockPos start, Direction direction, int travel, int periodTicks) {
		this.start = start;
		this.direction = direction;
		this.travel = travel;
		this.periodTicks = periodTicks;
	}

	/** Reverses direction once the platform reaches the end of its track. */
	public void periodTicksReversed() {
		this.step = -this.step;
	}

	public int step() {
		return step;
	}

	public BlockPos current() {
		return start.relative(direction, offset);
	}
}
