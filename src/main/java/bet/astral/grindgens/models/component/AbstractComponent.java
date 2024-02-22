/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.component;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.internals.Ticked;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;

public abstract class AbstractComponent implements Component, TickedComponent, Ticked, Cloneable {
	protected final GrindGens grindGens;
	private final ComponentType type;
	private final Location location;
	private final GenPlayer genPlayer;
	private final Material blockMaterial;
	private final int id;
	private boolean isLoaded;
	private long lastLoaded;
	private boolean isDeleted = false;
	private boolean isNew = false;
	public AbstractComponent(GrindGens gens, ComponentType type, GenPlayer genPlayer, Location location, Material material, int id) {
		this.grindGens = gens;
		this.id = id;
		this.type = type;
		this.location = location;
		this.genPlayer = genPlayer;
		this.blockMaterial = material;
	}
	protected AbstractComponent(GrindGens gens, ComponentType type, GenPlayer genPlayer, Location location, Material material, int id, String data) {
		this.grindGens = gens;
		this.id = id;
		this.type = type;
		this.location = location;
		this.genPlayer = genPlayer;
		this.blockMaterial = material;
	}
	@Override
	public GrindGens grindGens(){
		return grindGens;
	}
	@Override
	public ComponentType type() {
		return type;
	}

	@Override
	public Location location() {
		return location;
	}

	@Override
	public GenPlayer genPlayer() {
		return genPlayer;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public Material blockMaterial() {
		return blockMaterial;
	}

	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
		this.lastLoaded = System.currentTimeMillis();
	}

	@Override
	public long lastLoaded() {
		return lastLoaded;
	}

	@Override
	public AbstractComponent clone() {
		try {
			// TODO: copy mutable state here, so the clone can't change the internals of the original
			return (AbstractComponent) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}

	@Override
	@ApiStatus.NonExtendable
	public void requestSave() {
		grindGens.globalComponentManager().save(this);
	}

	@Override
	public boolean deleted() {
		return isDeleted;
	}

	@Override
	public void setDeleted(boolean v) {
		isDeleted = v;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	@Override
	public void setNew(boolean v) {
		this.isNew = v;
	}
}
