package bet.astral.grindgens.listeners;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.hopper.ChunkHopper;
import org.bukkit.Chunk;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class ChunkHopperListener implements Listener {
	private final GrindGens grindGens;
	@Contract(pure = true)
	public ChunkHopperListener(GrindGens grindGens){
		this.grindGens = grindGens;
	}
	@EventHandler
	public void onItemDrop(@NotNull ItemSpawnEvent event){
		boolean disabled = true;
		if (disabled)
			return;
		Item item = event.getEntity();
		PersistentDataContainer container = item.getPersistentDataContainer();
		if (container.has(grindGens.dropOwnerKey())){
			Chunk chunk = event.getLocation().getChunk();
			List<ChunkHopper> chunkHoppers = grindGens.globalComponentManager().get(chunk, ChunkHopper.class);
			if (chunkHoppers == null){
				return;
			}
			Random random = grindGens.globalComponentManager().random();
			ChunkHopper randomHopper = chunkHoppers.get(random.nextInt(0, chunkHoppers.size()-1));
			randomHopper.receive(item);
		}
	}
}
