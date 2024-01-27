package bet.astral.grindgens.models.component;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.internals.Ticked;
import org.bukkit.Location;
import org.bukkit.Material;

public abstract class AbstractComponent implements Component, TickedComponent, Ticked {
	protected final GrindGens grindGens;
	private final ComponentType type;
	private final Location location;
	private final GenPlayer genPlayer;
	private final Material blockMaterial;
	private final int id;
	private boolean isLoaded;
	private long lastLoaded;
	public AbstractComponent(GrindGens gens, ComponentType type, GenPlayer genPlayer, Location location, Material material, int id) {
		this.grindGens = gens;
		this.id = id;
		this.type = type;
		this.location = location;
		this.genPlayer = genPlayer;
		this.blockMaterial = material;
	}

	public Location hologramLocation(){ return null; }

	public ComponentType type() {
		return type;
	}

	public Location location() {
		return location;
	}

	public GenPlayer genPlayer() {
		return genPlayer;
	}

	public int id() {
		return id;
	}

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
}
