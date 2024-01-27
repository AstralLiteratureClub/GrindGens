package bet.astral.grindgens;

import bet.astral.grindgens.commands.CommandManager;
import bet.astral.grindgens.listeners.*;
import bet.astral.grindgens.manager.DropManager;
import bet.astral.grindgens.manager.ItemCreator;
import bet.astral.grindgens.manager.PlayerManager;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.internals.Ticked;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

public final class GrindGens extends JavaPlugin implements Ticked  {
    private final NamespacedKey dropValueKey = new NamespacedKey("grindgens", "dropvalue");
    private final NamespacedKey dropOwnerKey = new NamespacedKey("grindgens", "dropowner");
    private final NamespacedKey generatorItemTypeKey = new NamespacedKey("grindgens", "generatorid");
    private final NamespacedKey generatorItemTierKey = new NamespacedKey("grindgens", "generatortier");
    private final NamespacedKey chunkHopperItemKey = new NamespacedKey("grindgens", "chunkhopper");
    private final NamespacedKey menuActionName = new NamespacedKey("grindgens", "menu.type");
    private final NamespacedKey menuUpgradeTiers = new NamespacedKey("grindgens", "menu.upgrades");
    private PlayerManager playerManager;
    private DropManager dropManager;
    private GlobalComponentManager componentGlobalComponentManager;
    private ItemCreator itemCreator;
    private VaultEconomy economy;

    @Override
    public void onEnable() {
        itemCreator = new ItemCreator(this);
        playerManager = new PlayerManager(this);
        componentGlobalComponentManager = new GlobalComponentManager(this);
        dropManager = new DropManager(this);

        getServer().getScheduler().runTaskTimer(this, this::tick, 100, 1);

        try {
            new CommandManager(this, AsynchronousCommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        registerListener(new ChunkHopperListener(this));
        registerListener(new ComponentInventoryReplacementListener(this));
        registerListener(new ComponentMenuListener(this));
        registerListener(new ComponentPlacementListener(this));
        registerListener(new ConnectionListener(this));
        if (getServer().getPluginManager().getPlugin("RoseStacker") == null){
            registerListener(new RoseStackerListener(this));
        }

        economy = new VaultEconomy(this);

        getLogger().info("Hello! I am enabled now!");
    }

    @Override
    public void onDisable() {

        getLogger().info("Hello! I am disabled now!");
    }

    public void registerListener(Listener listener){
        getServer().getPluginManager().registerEvents(listener, this);
    }


    /**
     * Returns the value key for dropped items to retrieve from dropped items
     * @return value key
     */
    public NamespacedKey dropValueKey() {
        return dropValueKey;
    }

    /**
     * Returns the owner key for dropped items to retrieve the owner
     * @return owner key
     */
    public NamespacedKey dropOwnerKey() {
        return dropOwnerKey;
    }

    /**
     * Returns the key used in item stacks to store what generator it is when placed
     * @return key
     */
    public NamespacedKey generatorItemTypeKey() {
        return generatorItemTypeKey;
    }

    /**
     * Returns the key used in item stacks to store what generator tier it is when placed
     * @return key
     */
    public NamespacedKey generatorItemTierKey() {
        return generatorItemTierKey;
    }

    /**
     * Returns the key to handle chunk hoppers when inside an inventory
     * @return key
     */
    public NamespacedKey chunkHopperItemKey() {
        return chunkHopperItemKey;
    }

    public NamespacedKey menuActionName() {
        return menuActionName;
    }

    public NamespacedKey menuUpgradeTiers() {
        return menuUpgradeTiers;
    }

    public PlayerManager playerManager() {
        return playerManager;
    }

    public DropManager dropManager() {
        return dropManager;
    }

    public GlobalComponentManager globalComponentManager(){
        return componentGlobalComponentManager;
    }

    public ItemCreator itemCreator() {
        return itemCreator;
    }

    public VaultEconomy economy() {
        return economy;
    }

    @Override
    public boolean canTick() {
        return true;
    }

    @Override
    public void tick() throws IllegalStateException {
        componentGlobalComponentManager.tick();
        dropManager.tick();
    }
}
