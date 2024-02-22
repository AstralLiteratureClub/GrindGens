/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.inventory;

import bet.astral.grindgens.models.component.Component;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface UpgradeMenu<T extends Component> extends InventoryHolder, Cloneable, Lockable {
	@NotNull
	T component();
	@NotNull
	UpgradeMenu<T> clone();
	void reload();

	net.kyori.adventure.text.Component value(double currentValue, double newValue, boolean invertColors);
	net.kyori.adventure.text.Component displayname(int tier);
}
