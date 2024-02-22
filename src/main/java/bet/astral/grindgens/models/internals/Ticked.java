/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.internals;

public interface Ticked {
	/**
	 * Returns true if this class can tick
	 * @return can tick
	 */
	boolean canTick();

	/**
	 * Ticks this class
	 * @throws IllegalStateException if the class cannot tick
	 */
	void tick() throws IllegalStateException;

}
