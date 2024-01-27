package bet.astral.grindgens.models.generators;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.manager.DropManager;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.component.AbstractComponent;
import bet.astral.grindgens.models.component.ComponentType;
import bet.astral.grindgens.models.component.MinableComponent;
import bet.astral.grindgens.models.component.UpgradeableComponent;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.hopper.ChunkHopper;
import bet.astral.grindgens.models.inventory.UpgradeMenu;
import bet.astral.grindgens.models.inventory.UpgradeMenuGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class Generator extends AbstractComponent implements UpgradeableComponent<Generator>, MinableComponent {
	private final GeneratorType type;
	private final Location dropLocation;
	private final int tier;
	private UpgradeMenu<Generator> upgradeMenu;
	private UpgradeMenu<Generator> lockedUpgradeMenu;
	private int ticks;

	public Generator(GrindGens grindGens, GenPlayer genPlayer, GeneratorType type, Location location, int id, int tier) {
		super(grindGens, ComponentType.GENERATOR, genPlayer, location, type.groundMaterial(), id);
		this.type = type;
		this.dropLocation = new Location(location.getWorld(), location.getBlockX()+0.5, location.getBlockY()+1.0, location.getBlockZ()+0.5);
		this.tier = tier;
	}
	public Generator(GrindGens grindGens, GenPlayer genPlayer, GeneratorType type, GlobalComponentManager generatorManager, Location location){
		super(grindGens, ComponentType.GENERATOR, genPlayer, location, type.groundMaterial(), generatorManager.requestSecureId());
		this.type = type;
		this.dropLocation = new Location(location.getWorld(), location.getBlockX()+0.5, location.getBlockY()+1.0, location.getBlockZ()+0.5);
		this.tier = 1;
		this.ticks = 0;
	}


	@Override
	public boolean canTick() {
		return isLoaded();
	}

	@Override
	public void tick() throws IllegalStateException {
		if (!canTick()){
			throw new IllegalStateException("Cannot tick generator while it's not loaded! "+ id());
		}
		ticks-=1;
		if (ticks == -1){
			GeneratorTier genTier = type.getTier(tier);
			if (genTier == null){
				return;
			}
			ticks = genTier.dropRate();
		} else{
			return;
		}

		Location location = dropLocation;

		World world = location.getWorld();
		world.dropItem(location, type.dropMaterial(), item->{
			DropManager dropManager = grindGens.dropManager();
			dropManager.create(this, item);
			GlobalComponentManager globalComponentManager = grindGens.globalComponentManager();
			List<ChunkHopper> chunkHoppers = globalComponentManager.get(location.getChunk(), ChunkHopper.class);
			if (chunkHoppers == null){
				return;
			}
			Random random = globalComponentManager.random();
			int amount = chunkHoppers.size()-1;
			ChunkHopper hopper;
			if (amount==0){
				hopper = chunkHoppers.get(0);
			} else {
				hopper = chunkHoppers.get(random.nextInt(1, chunkHoppers.size())-1);
			}
			hopper.receive(item);
		});
	}

	public GrindGens grindGens() {
		return grindGens;
	}

	public GeneratorType generatorType() {
		return type;
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
	public void closeMenus() {
		if (upgradeMenu == null){
			return;
		}
		upgradeMenu.getInventory().close();
		lockedUpgradeMenu.getInventory().close();
	}

	@Override
	public int maxTier() {
		return type.maxTier();
	}

	public int tier() {
		return tier;
	}

	@Override
	public double upgradeCost() {
		return 1000;
	}

	@Override
	public boolean upgrade(Player player) {
		return false;
	}

	public int ticks() {
		return ticks;
	}


	@Override
	public UpgradeMenu<Generator> upgradeMenu() {
		if (upgradeMenu == null){
			upgradeMenu = new UpgradeMenuGenerator(this);
		}
		return lockedMenu();
	}

	@Override
	public UpgradeMenu<Generator> lockedMenu() {
		if (upgradeMenu == null){
			upgradeMenu = new UpgradeMenuGenerator(this);
		}
		if (this.lockedUpgradeMenu == null){
			lockedUpgradeMenu = upgradeMenu.clone();
			lockedUpgradeMenu.lock();;
		}
		return lockedUpgradeMenu;
	}

	@Override
	public boolean hasUpgradePermission(Player player) {
		return true;
	}

	@Override
	public ItemStack getBlock() {
		return grindGens.itemCreator().createGenerator(type, tier);
	}
}
