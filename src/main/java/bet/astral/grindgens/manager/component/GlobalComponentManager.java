package bet.astral.grindgens.manager.component;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.component.*;
import bet.astral.grindgens.models.generators.Generator;
import bet.astral.grindgens.models.generators.GeneratorType;
import bet.astral.grindgens.models.hopper.ChunkHopper;
import bet.astral.grindgens.models.internals.ComponentLoader;
import bet.astral.grindgens.models.internals.PlayerLoader;
import bet.astral.grindgens.models.internals.SecureId;
import bet.astral.grindgens.models.internals.Ticked;
import bet.astral.grindgens.utils.Configuration;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class GlobalComponentManager implements Ticked, PlayerLoader, SecureId, ComponentLoader<Component>, ComponentManager {
	private final Set<GeneratorType> generatorTypes;
	private final Random random = new Random((System.nanoTime()*System.nanoTime())/System.currentTimeMillis());
	private final GrindGens grindGens;
	private final List<SubComponentManager<?>> subComponentManagers = new ArrayList<>();
	private final Map<Class<?>, List<SubComponentManager<?>>> componentManagers = new HashMap<>();

	public GlobalComponentManager(GrindGens grindGens) {
		this.grindGens = grindGens;
		ChunkHopperManager<?> chunkHopperManager = new ChunkHopperManager<>(grindGens, this);
		GeneratorManager<?> generatorManager = new GeneratorManager<>(grindGens, this);

		subComponentManagers.add(generatorManager);
		subComponentManagers.add(chunkHopperManager);

		componentManagers.put(Generator.class, new LinkedList<>());
		componentManagers.put(ChunkHopper.class, new LinkedList<>());
		componentManagers.put(UpgradeableComponent.class, new LinkedList<>());
		componentManagers.put(MenuReplacementComponent.class, new LinkedList<>());
		componentManagers.put(HologramComponent.class, new LinkedList<>());
		componentManagers.put(MinableComponent.class, new LinkedList<>());
		componentManagers.put(Component.class, new LinkedList<>());

		componentManagers.get(Component.class).addAll(List.of(generatorManager, chunkHopperManager));

		componentManagers.get(ChunkHopper.class).add(chunkHopperManager);
		componentManagers.get(Generator.class).add(generatorManager);

		componentManagers.get(UpgradeableComponent.class).addAll(List.of(generatorManager, chunkHopperManager));
		componentManagers.get(MinableComponent.class).addAll(List.of(generatorManager, chunkHopperManager));

		componentManagers.get(HologramComponent.class).addAll(List.of(chunkHopperManager));
		componentManagers.get(MenuReplacementComponent.class).addAll(List.of(chunkHopperManager));

		Configuration configuration = new Configuration();
		generatorTypes = configuration.loadGenerators(new File(grindGens.getDataFolder(), "generators.yml"));
	}

	public GeneratorType getGeneratorType(String generatorStringType) {
		return !generatorTypes.isEmpty() ? generatorTypes.stream().filter(type->type.name().equalsIgnoreCase(generatorStringType)).findAny().orElse(null) : null;
	}
	public Set<GeneratorType> generatorTypes(){
		return ImmutableSet.copyOf(generatorTypes);
	}


	private <C extends Component>  List<C> getInternal(SubComponentManager<?> componentManager, Chunk chunk, Class<C> type){
		List<C> found = componentManager.getInternal(chunk, type);
		if (found != null && found.isEmpty()){
			return null;
		}
		return found;
	}

	@Override
	public <C extends Component> @Nullable C get(@NotNull Location location, @NotNull Class<C> type) {
		List<SubComponentManager<?>> subComponentManagers = componentManagers.get(type);
		if (subComponentManagers == null || subComponentManagers.isEmpty()){
			return null;
		}
		for (SubComponentManager<?> subComponentManager : subComponentManagers){
			if (subComponentManager.getInternal(location, type) != null){
				return subComponentManager.getInternal(location, type);
			}
		}
		return null;
	}

	@Override
	public @Nullable <C extends Component> List<@NotNull C> get(@NotNull Chunk chunk, @NotNull Class<C> type) {
		C found = null;
		List<SubComponentManager<?>> subComponentManagers = componentManagers.get(type);
		if (subComponentManagers == null || subComponentManagers.isEmpty()){
			return null;
		}
		for (SubComponentManager<?> subComponentManager : subComponentManagers){
			if (subComponentManager.getInternal(chunk, type) != null){
				return subComponentManager.getInternal(chunk, type);
			}
		}
		return null;
	}

	@Override
	public void save(@NotNull Component component) {
		if (componentManagers.get(component.getClass()) == null){
			return;
		}
		//noinspection unchecked
		SubComponentManager<Component> subComponentManager = (SubComponentManager<Component>) componentManagers.get(component.getClass()).get(0);
		subComponentManager.saveInternal(component);
	}

	@Override
	public void delete(@NotNull Component component) {
		if (componentManagers.get(component.getClass()) == null || componentManagers.get(component.getClass()).isEmpty()){
			return;
		}
		//noinspection unchecked
		SubComponentManager<Component> subComponentManager = (SubComponentManager<Component>) componentManagers.get(component.getClass()).get(0);
		subComponentManager.deleteInternal(component);
	}

	@Override
	public void tickIfCan(@NotNull SubComponentManager<?> subComponentManager) {
		if (subComponentManager.canTick()){
			subComponentManager.tick();
		}
	}

	@Override
	public Random random() {
		return random;
	}

	@Override
	public void load(Component component) {
		for (SubComponentManager<?> subComponentManager : subComponentManagers){
			subComponentManager.loadInternalDef(component);
		}
	}

	@Override
	public void unload(Component component) {
		for (SubComponentManager<?> subComponentManager : subComponentManagers){
			subComponentManager.unloadInternalDef(component);
		}
	}

	@Override
	public void load(GenPlayer genPlayer) {
		for (SubComponentManager<?> subComponentManager : subComponentManagers){
			subComponentManager.load(genPlayer);
		}
	}

	@Override
	public void unload(GenPlayer genPlayer) {
		for (SubComponentManager<?> subComponentManager : subComponentManagers){
			subComponentManager.unload(genPlayer);
		}
	}

	@Override
	public int requestSecureId() {
		return random.nextInt(1, Integer.MAX_VALUE);
	}

	@Override
	public boolean canTick() {
		return true;
	}

	@Override
	public void tick() throws IllegalStateException {
		for (SubComponentManager<?> subComponentManager : subComponentManagers){
			tickIfCan(subComponentManager);
		}
	}

	public GrindGens grindGens() {
		return grindGens;
	}
}
