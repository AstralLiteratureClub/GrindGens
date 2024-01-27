package bet.astral.grindgens.models.hopper;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.component.*;
import bet.astral.grindgens.models.inventory.UpgradeMenuChunkHopper;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ChunkHopper extends AbstractComponent implements HologramComponent, UpgradeableComponent<ChunkHopper>, MenuReplacementComponent, MinableComponent {
	private long nextFetch = System.currentTimeMillis();
	private final Location hologramLocation;
	private final Hologram hologram;
	private UpgradeMenuChunkHopper chunkHopperMenu;
	private UpgradeMenuChunkHopper chunkHopperMenuClone;
	private Inventory inventory;
	private final int maxTier = 2;
	private int tier = 1;

	public ChunkHopper(GrindGens grindGens, GenPlayer genPlayer, Location location, int id) {
		super(grindGens, ComponentType.HOPPER, genPlayer, location, Material.HOPPER, id);

		this.hologramLocation = location.clone().add(0.5, 1.4, 0.5);
		this.hologram = new Hologram(hologramName(), location, false);
		this.hologram.getPage(0).setLine(0, "§aChunk Hopper");

		inventory = Bukkit.createInventory(this, InventoryType.DISPENSER, net.kyori.adventure.text.Component.text("Chunk Hopper"));
	}
	public ChunkHopper(GlobalComponentManager componentManager, GenPlayer genPlayer, Location location) {
		super(componentManager.grindGens(), ComponentType.HOPPER, genPlayer, location, Material.HOPPER, componentManager.requestSecureId());

		this.hologramLocation = location.clone().add(0.5, 1.4, 0.5);
		this.hologram = new Hologram(hologramName(), location, false);
		this.hologram.getPage(0).setLine(0, "§aChunk Hopper");
	}

	@Override
	public Location hologramLocation() {
		return hologramLocation;
	}

	public boolean receive(Item item) {
		if (nextFetch < System.currentTimeMillis()){
			nextFetch=System.currentTimeMillis()+10000L;
			Block block = location().getBlock();
			if (block.getType() != Material.HOPPER){
				throw new RuntimeException("Block is not a hopper, but it is supposed to be! Maybe an exploit to destroy hoppers?");
			}
		}
		item.teleport(location().clone().add(0.5, 1, 0.5));
		return true;
	}


	@Override
	public Hologram hologram() {
		return hologram;
	}

	@Override
	public String hologramName() {
		return "CHUNK-HOPPER-"+id();
	}

	@Override
	public void closeMenus() {

	}

	@Override
	public UpgradeMenuChunkHopper upgradeMenu() {
		if (chunkHopperMenu == null){
			chunkHopperMenu = new UpgradeMenuChunkHopper(this);
		}
		return lockedMenu();
	}

	@Override
	public UpgradeMenuChunkHopper lockedMenu() {
		if (chunkHopperMenu == null){
			chunkHopperMenu = new UpgradeMenuChunkHopper(this);
		}
		if (this.chunkHopperMenuClone == null){
			chunkHopperMenuClone = chunkHopperMenu.clone();
			chunkHopperMenuClone.lock();;
		}
		return chunkHopperMenuClone;
	}

	@Override
	public boolean hasUpgradePermission(Player player) {
		return false;
	}

	@Override
	public void open(Player player) {
		if (hasUpgradePermission(player)){
			player.openInventory(upgradeMenu().getInventory());
		} else {
			player.openInventory(lockedMenu().getInventory());
		}
	}

	@Override
	public int maxTier() {
		return maxTier;
	}

	@Override
	public int tier() {
		return tier;
	}

	@Override
	public double upgradeCost() {
		return 500;
	}

	@Override
	public boolean upgrade(Player player) {
		return false;
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inventory;
	}

	@Override
	public boolean canTick() {
		return false;
	}

	@Override
	public void tick() throws IllegalStateException {
		throw new IllegalStateException("Chunk generators are not able to be ticked!");
	}

	@Override
	public ItemStack getBlock() {
		return grindGens.itemCreator().createHopper();
	}
}
