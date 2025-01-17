/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.component;

import bet.astral.grindgens.models.inventory.UpgradeMenu;
import org.bukkit.entity.Player;

/**
 * Implemented by those components which can be upgraded
 */
public interface UpgradeableComponent<T extends Component> extends Component {
	UpgradeMenu<T> upgradeMenu();
	UpgradeMenu<T> lockedMenu();
	boolean hasUpgradePermission(Player player);
	void open(Player player);
	void closeMenus();
}