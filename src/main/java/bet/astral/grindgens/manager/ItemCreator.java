package bet.astral.grindgens.manager;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.generators.GeneratorType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedList;
import java.util.List;

public class ItemCreator {
	private final GrindGens grindGens;

	public ItemCreator(GrindGens grindGens) {
		this.grindGens = grindGens;
	}

	public ItemStack createGenerator(GeneratorType generatorType, int tier){
		ItemStack itemStack = new ItemStack(generatorType.groundMaterial());
		ItemMeta meta = itemStack.getItemMeta();

		if (tier > generatorType.maxTier()){
			tier = generatorType.maxTier();
		} else if (tier < 0){
			tier = 0;
		}

		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		pdc.set(grindGens.generatorItemTypeKey(), PersistentDataType.STRING, generatorType.name());
		pdc.set(grindGens.generatorItemTierKey(), PersistentDataType.INTEGER, tier);

		meta.displayName(generatorType.displayname().decoration(TextDecoration.ITALIC, false));

		List<Component> lore = new LinkedList<>();

		split(lore);
		lore.add(Component.text("Tier", NamedTextColor.GREEN).append(Component.text(": ", NamedTextColor.GRAY)).append(Component.text(String.valueOf(tier+1))).decoration(TextDecoration.ITALIC, false));
		lore.add(Component.text("Type", NamedTextColor.GREEN).append(Component.text(": ", NamedTextColor.GRAY)).append(Component.text(generatorType.name().toLowerCase())).decoration(TextDecoration.ITALIC, false));
		split(lore);

		meta.lore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	public ItemStack createHopper(){
		ItemStack itemStack = new ItemStack(Material.HOPPER);
		ItemMeta meta = itemStack.getItemMeta();

		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		pdc.set(grindGens.chunkHopperItemKey(), PersistentDataType.BOOLEAN, true);

		meta.displayName(Component.text("Chunk Hopper", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

		List<Component> lore = new LinkedList<>();

		split(lore);
		lore.add(Component.text("Teleports all items to this", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
		lore.add(Component.text(" hopper as they are generated.", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
		split(lore);

		meta.lore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	public void split(List<Component> lore){
		Component component = Component.empty();
		boolean gray = true;
		for (int i = 0; i < 15; i++){
			gray = !gray;
			if (!gray){
				component = component.append(Component.text("-", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
			}else{
				component = component.append(Component.text("-", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
			}
		}
		lore.add(component);
	}
}
