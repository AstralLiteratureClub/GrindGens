/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.generators;

import bet.astral.grindgens.models.Tier;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;

import java.util.List;

public final class GeneratorType {
	private final String name;
	private final ItemStack sourceItem;
	private final Component upgradeMenuDisplay;
	private final List<Tier> dropRateTiers;
	private final List<Tier> valueTiers;
	private final int amountGiven;
	private final double cost;

	public GeneratorType(String name, ItemStack sourceItem, Component upgradeMenuDisplay, int newPlayerReceive, double cost, List<Tier> dropRateTiers, List<Tier> valueTiers) {
		this.name = name;
		this.sourceItem = sourceItem;
		this.upgradeMenuDisplay = upgradeMenuDisplay;
		this.amountGiven = newPlayerReceive;
		this.cost = cost;
		this.dropRateTiers = dropRateTiers;
		this.valueTiers = valueTiers;
	}
	public String name() {
		return name;
	}

	@Contract(pure = true)
	public ItemStack sourceItem() {
		return sourceItem;
	}

	@Contract(pure = true)
	public Component upgradeMenuDisplay(){
		return upgradeMenuDisplay;
	}

	public double defaultValue() {
		return valueTier(0).value();
	}

	@Contract(pure = true)
	public double defaultDropRate() {
		return dropRateTier(0).value();
	}

	public int maxValueTier(){
		return valueTiers.size()-1;
	}
	public int maxDropRateTier(){
		return dropRateTiers.size()-1;
	}

	public Tier valueTier(int tier){
		if (tier < 0) {
			tier = 0;
		}

		if (tier == 0){
			return valueTiers.get(0);
		} else if (tier > valueTiers.size()-1){
			tier = valueTiers.size()-1;
		}
		return valueTiers.get(tier);
	}
	public Tier dropRateTier(int tier){
		if (tier < 0) {
			tier = 0;
		}

		if (tier == 0){
			return dropRateTiers.get(0);
		} else if (tier > dropRateTiers.size()-1){
			tier = dropRateTiers.size()-1;
		}
		return dropRateTiers.get(tier);
	}


	public int amountGiven() {
		return amountGiven;
	}

	public double cost() {
		return cost;
	}
}
