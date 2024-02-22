/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.generators.Generator;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class GenPlayer {
	private final GrindGens grindGens;
	private final UUID uuid;
	private final List<Generator> generators;
	private int maxGenerators = 25;
	@Getter
	private double bitcoins;
	private boolean isNew;


	@ApiStatus.Internal
	public GenPlayer(GrindGens grindGens){
		this.grindGens = grindGens;
		this.uuid = UUID.randomUUID();
		this.generators = new LinkedList<>();
	}

	public GenPlayer(GrindGens grindGens, OfflinePlayer player) {
		this.grindGens = grindGens;
		this.uuid = player.getUniqueId();
		this.generators = new LinkedList<>();
	}

	public GenPlayer(GrindGens grindGens, UUID id) {
		this.grindGens = grindGens;
		this.uuid = id;
		this.generators = new ArrayList<>();
	}


	@ApiStatus.Internal
	public void addComponent(Component component) {
		if (component instanceof Generator generator){
			if (generators.contains(component)){
				return;
			}
			this.generators.add(generator);
		}
	}

	@ApiStatus.Internal
	public void removeComponent(Component component) {
		if (component instanceof Generator generator){
			this.generators.remove(generator);
		}
	}
	public List<Generator> generators() {
		return generators;
	}

	public UUID uuid() {
		return uuid;
	}

	public int maxGenerators() {
		return maxGenerators;
	}

	public GenPlayer setMaxGenerators(int maxGenerators) {
		this.maxGenerators = maxGenerators;
		return this;
	}


	public void requestSave() {
		if (this.grindGens.playerManager().requestedSaves.contains(this)){
			return;
		}
		this.grindGens.playerManager().requestedSaves.add(this);
	}

	public Set<Component> components() {
		return new HashSet<>(generators);
	}


	public void setBitcoin(double v) {
		this.bitcoins = v;
	}

	public boolean isNew() {
		return isNew;
	}

	public GenPlayer setNew(boolean aNew) {
		isNew = aNew;
		return this;
	}
}
