/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.inventory.upgrade;

import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.generators.Generator;
import bet.astral.grindgens.models.generators.GeneratorType;
import bet.astral.grindgens.models.inventory.UpgradeMenu;
import bet.astral.grindgens.utils.TriState;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public abstract class AbstractUpgradeMenu<T extends Component> implements UpgradeMenu<T> {
	@NotNull
	protected static Inventory createUpgradeMenu(UpgradeMenu<Generator> generatorUpgradeMenu){
		Generator generator = generatorUpgradeMenu.component();
		GeneratorType type = generator.generatorType();
		return Bukkit.createInventory(generatorUpgradeMenu, 9, type.upgradeMenuDisplay());
	}

	private final DecimalFormat percentageFormat = new DecimalFormat(".0");
	@NotNull
	private final T component;
	protected Inventory inventory;
	private boolean locked;

	public AbstractUpgradeMenu(@NotNull T component) {
		this.component = component;
		this.locked = true;
	}
	public AbstractUpgradeMenu(@NotNull T component, boolean locked) {
		this(component);
		this.locked = locked;
	}

	@Override
	public void lock() {
		locked = true;
	}

	@Override
	public void unlock() {
		locked = false;
	}

	@Override
	public boolean isLocked() {
		return locked;
	}

	@Override
	public @NotNull T component() {
		return component;
	}

	@Override
	public @NotNull Inventory getInventory() {
		if (inventory == null){
			throw new RuntimeException("Upgrade menu is not initialized!");
		}
		return inventory;
	}

	@ApiStatus.OverrideOnly
	public @NotNull AbstractUpgradeMenu clone(){
		return null;
	}


	@NotNull
	public net.kyori.adventure.text.Component value(double currentValue, double upgradeValue, boolean inverted) {
		TriState state = currentValue == upgradeValue ? TriState.SAME : !inverted && (currentValue < upgradeValue) ? TriState.HIGHER : TriState.LOWER;
		TextColor sameColor = NamedTextColor.YELLOW;
		TextColor worseColor = NamedTextColor.RED;
		TextColor betterColor = NamedTextColor.GREEN;
		//noinspection UnnecessaryUnicodeEscape

		String plusMinus = state == TriState.SAME ? "\u00B1" : state == TriState.LOWER ? "-" : "+";

		double percentage = state == TriState.SAME ?
				0 :
				state == TriState.LOWER ?
						(currentValue/upgradeValue)
						: (upgradeValue/currentValue);
		percentage = (percentage*100);
		if (percentage<0)
			percentage*=-1;

		String formattedPercentage = percentageFormat.format(percentage).replace(",0", "");
		if (formattedPercentage.isEmpty() || formattedPercentage.isBlank())
			formattedPercentage = "0";
		if (state == TriState.SAME){
			return net.kyori.adventure.text.Component.text(currentValue, sameColor);
			/*
			.appendSpace().append(
			net.kyori.adventure.text.Component.text("(", NamedTextColor.GRAY)).append(
			net.kyori.adventure.text.Component.text(plusMinus+formattedPercentage, sameColor)).append(
			net.kyori.adventure.text.Component.text("%", NamedTextColor.WHITE)).append(
			net.kyori.adventure.text.Component.text(")", NamedTextColor.GRAY));
			 */
		}
		return net.kyori.adventure.text.Component.text(currentValue, state == TriState.HIGHER ? worseColor : betterColor).append(
				net.kyori.adventure.text.Component.text(" -> ", NamedTextColor.GRAY)).append(
				net.kyori.adventure.text.Component.text(upgradeValue, state == TriState.LOWER ? worseColor : betterColor).append(
				net.kyori.adventure.text.Component.text(" (", NamedTextColor.GRAY)).append(
				net.kyori.adventure.text.Component.text(plusMinus, state == TriState.LOWER ? worseColor : betterColor).append(
				net.kyori.adventure.text.Component.text(formattedPercentage, state == TriState.LOWER ? worseColor : betterColor).append(
				net.kyori.adventure.text.Component.text("%", NamedTextColor.WHITE))).append(
				net.kyori.adventure.text.Component.text(")", NamedTextColor.GRAY))));
	}

	@NotNull
	public net.kyori.adventure.text.Component displayname(int tier) {
		return net.kyori.adventure.text.Component.text("Tier ", NamedTextColor.GRAY).append(net.kyori.adventure.text.Component.text(tier, NamedTextColor.YELLOW));
	}

}
