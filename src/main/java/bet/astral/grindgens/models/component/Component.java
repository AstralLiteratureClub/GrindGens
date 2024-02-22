/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.component;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.GenPlayer;
import org.bukkit.Location;
import org.bukkit.Material;

public interface Component extends TickedComponent, Cloneable {
	GrindGens grindGens();
	ComponentType type();
	Location location();
	GenPlayer genPlayer();
	int id();
	Material blockMaterial();

	Component clone();


	void requestSave();

	boolean deleted();
	void setDeleted(boolean v);


	boolean isNew();
	void setNew(boolean v);
}
