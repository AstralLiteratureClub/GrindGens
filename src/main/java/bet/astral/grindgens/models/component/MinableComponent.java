/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.component;

import org.bukkit.inventory.ItemStack;

public interface MinableComponent extends Component{
	ItemStack getBlock();
}
