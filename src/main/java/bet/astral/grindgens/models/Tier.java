/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models;

// The coming state of this class is unclear as there might be more upgrade options ad "value"
// and "speed" in different upgrade trees.
public class Tier {
	private double cost;
	private double value;

	public Tier(double cost, double value) {
		this.cost = cost;
		this.value = value;
	}

	public double cost() {
		return cost;
	}

	public double value() {
		return value;
	}
}
