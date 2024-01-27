package bet.astral.grindgens.models.inventory;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.VaultEconomy;
import bet.astral.grindgens.models.generators.Generator;
import bet.astral.grindgens.models.generators.GeneratorTier;
import bet.astral.grindgens.models.generators.GeneratorType;
import bet.astral.grindgens.utils.TriState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class UpgradeMenuGenerator extends AbstractUpgradeMenu<Generator> implements UpgradeMenu<Generator> {
	@NotNull
	protected static Inventory createUpgradeMenu(UpgradeMenu<Generator> generatorUpgradeMenu){
		Generator generator = generatorUpgradeMenu.component();
		GeneratorType type = generator.generatorType();
		return Bukkit.createInventory(generatorUpgradeMenu, 9, type.displayname());
	}


	public UpgradeMenuGenerator(@NotNull Generator component) {
		super(component);
		super.inventory = createUpgradeMenu(this);
		setup();
	}
	public UpgradeMenuGenerator(@NotNull Generator component, boolean isLocked) {
		super(component, isLocked);
		super.inventory = createUpgradeMenu(this);
		setup();
	}

	private void setup(){
		Generator generator = component();
		ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta meta = background.getItemMeta();
		meta.displayName(Component.text(""));
		meta.addItemFlags(ItemFlag.values());
		background.setItemMeta(meta);

		for (int i = 0; i < 9; i++){
			inventory.setItem(i, background);
		}
		for (int i = 0; i < 9; i++){

			inventory.setItem(i, upgradeButton(i, generator));
		}
	}


	@NotNull
	protected ItemStack upgradeButton(int tier, Generator generator) {
		GeneratorType type = generator.generatorType();
		GeneratorTier generatorTier = type.getTier(tier);
		GeneratorTier currentTier = type.getTier(generator.tier());
		TriState state = tier == generator.tier() ? TriState.SAME : tier < generator.tier() ? TriState.LOWER : TriState.HIGHER;
		VaultEconomy vaultEconomy = generator.grindGens().economy();

		if (generatorTier== null){
			Material material = Material.GRAY_STAINED_GLASS;
			ItemStack itemStack = new ItemStack(material);
			ItemMeta meta = itemStack.getItemMeta();
			meta.displayName(Component.text("No Upgrade Found", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
			meta.addItemFlags(ItemFlag.values());
			itemStack.setItemMeta(meta);
			return itemStack;
		}
		Material material = Material.GOLD_BLOCK;
		if (tier % 2 == 0){
			material = Material.GOLD_INGOT;
		}
		ItemStack itemStack = new ItemStack(material);
		ItemMeta meta = itemStack.getItemMeta() != null ? Bukkit.getItemFactory().getItemMeta(material) : itemStack.getItemMeta();

		itemStack.setAmount(tier+1);

		List<Component> lore = new LinkedList<>();
		lore.add(Component.empty());
		int upgradeDropRate = generatorTier.dropRate();
		int currentDropRate = currentTier.dropRate();
		double costToPurchase = generatorTier.cost();
		double upgradeValue = generatorTier.value();
		double currentValue = currentTier.value();

		// TODO - Add way to see cost to upgrade from current tier to this tier
		Component cost = Component.text(" | ", NamedTextColor.DARK_GRAY).append(Component.text("Cost ", NamedTextColor.GRAY).append(Component.text(costToPurchase, NamedTextColor.YELLOW)));
		Component value = Component.text(" | ", NamedTextColor.DARK_GRAY).append(Component.text("Value ", NamedTextColor.GRAY).append(value(currentValue, upgradeValue, false).decoration(TextDecoration.ITALIC, false)));
		Component dropRate = Component.text(" | ", NamedTextColor.DARK_GRAY).append(Component.text("DPS ", NamedTextColor.GRAY).append(value(currentDropRate, upgradeDropRate, true).decoration(TextDecoration.ITALIC, true)));
		lore.add(cost.decoration(TextDecoration.ITALIC, false));
		lore.add(value.decoration(TextDecoration.ITALIC, false));
		lore.add(dropRate.decoration(TextDecoration.ITALIC, false));
		lore.add(Component.empty());

		Integer[] menuUpgrades = new Integer[]{};
		String menuAction = "nothing";

		Component info1 = Component.text("Already Purchased", NamedTextColor.GRAY);
		if (state == TriState.HIGHER) {
			List<Integer> upgrades = new LinkedList<>();
			int tiers = 0;
			info1 = Component.text("Click to purchase tier(s) ", NamedTextColor.GRAY);
			double purchaseCost = 0;
			boolean isNew = true;
			for (int i = 0; i < 8; i++){
				if (i <= generator.tier()){
					continue;
				}
				GeneratorTier typeTier = type.getTier(i);
				if (typeTier == null){
					continue;
				}
				if (!isNew) {
					info1 = info1.append(Component.text(", ", NamedTextColor.GRAY));
				}
				isNew = false;
				info1 = info1.append(Component.text(i+1, NamedTextColor.YELLOW));

				purchaseCost+=typeTier.cost();
				tiers++;
				upgrades.add(i);
			}
			lore.add(info1.decoration(TextDecoration.ITALIC, false));

			Component info2 = Component.text("Cost for ", NamedTextColor.GRAY).append(Component.text(tiers, NamedTextColor.YELLOW)).append(Component.text(" tier(s) for ", NamedTextColor.GRAY))
					.append(Component.text(vaultEconomy.format(purchaseCost), NamedTextColor.YELLOW));
			lore.add(info2.decoration(TextDecoration.ITALIC, false));
			menuUpgrades = upgrades.toArray(Integer[]::new);
			menuAction = "upgrade";
		} else {
			lore.add(info1.decoration(TextDecoration.ITALIC, false));
		}

		meta.lore(lore);

		Component status = state == TriState.SAME ? Component.text("Current", NamedTextColor.YELLOW) : null;
		if (state != TriState.SAME)
			status = state == TriState.LOWER ? Component.text("Upgraded", NamedTextColor.GREEN) : Component.text("Not Upgraded", NamedTextColor.RED);

		Component displayname = displayname(tier+1, status);

		meta.displayName(displayname.decoration(TextDecoration.ITALIC, false));
		if (state == TriState.LOWER || state == TriState.SAME)
			meta.addEnchant(Enchantment.LUCK, 1, true);

		meta.addItemFlags(ItemFlag.values());


		PersistentDataContainer pdc = meta.getPersistentDataContainer();

		GrindGens grindGens = component().grindGens();


		pdc.set(grindGens.menuActionName(), PersistentDataType.STRING, menuAction);
		int[] tiers = new int[menuUpgrades.length];
		for (int i = 0; i < menuUpgrades.length; i++) {
			tiers[i] = menuUpgrades[i];
		}
		pdc.set(grindGens.menuUpgradeTiers(), PersistentDataType.INTEGER_ARRAY, tiers);

		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@NotNull
	private static Component displayname(int tier, Component status) {
		Component displayname = Component.text("Tier ", NamedTextColor.GRAY).append(Component.text(tier, NamedTextColor.YELLOW));
		return displayname.append(Component.text(" (", NamedTextColor.GRAY)
						.append(status))
						.append(Component.text(")", NamedTextColor.GRAY));
	}

	@Override
	public @NotNull UpgradeMenuGenerator clone() {
		return new UpgradeMenuGenerator(component(), isLocked());
	}
}
