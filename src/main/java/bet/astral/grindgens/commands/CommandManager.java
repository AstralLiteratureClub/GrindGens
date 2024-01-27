package bet.astral.grindgens.commands;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.manager.ItemCreator;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.generators.GeneratorTier;
import bet.astral.grindgens.models.generators.GeneratorType;
import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

public class CommandManager extends PaperCommandManager<CommandSender> {
	private final GrindGens plugin;

	public CommandManager(@NonNull GrindGens owningPlugin, @NonNull Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> commandExecutionCoordinator, @NonNull Function<CommandSender, CommandSender> commandSenderMapper, @NonNull Function<CommandSender, CommandSender> backwardsCommandSenderMapper) throws Exception {
		super(owningPlugin, commandExecutionCoordinator, commandSenderMapper, backwardsCommandSenderMapper);
		this.plugin = owningPlugin;

		registerBrigadier();
		registerAsynchronousCompletions();


		Command.Builder<CommandSender> gensBuilder =  commandBuilder("generators", ArgumentDescription.of("Lists all the generators in the server")).handler(context->{
			CommandSender sender = context.getSender();
			for (GeneratorType type : plugin.globalComponentManager().generatorTypes()){
				sender.sendMessage(type.name()+ " | Tiers: "+ type.maxTier());
			}
		});
		command(gensBuilder);

		command(gensBuilder
				.permission("grindgens.generator")
				.argument(StringArgument.of("generator")).argument(IntegerArgument.builder("tier"))
				.handler(context -> {
					GlobalComponentManager componentManager = plugin.globalComponentManager();
					GeneratorType type = componentManager.getGeneratorType(context.get("generator"));
					Player player = (Player) context.getSender();

					int tier = (int) context.getOptional("tier").orElse(1);
					ItemCreator itemCreator = plugin.itemCreator();
					player.getInventory().addItem(itemCreator.createGenerator(type, tier-1));
					player.sendMessage("Gave you a super duper generator!") ;
				}));

		command(commandBuilder("chunkhopper", ArgumentDescription.of("The main command of this plugin."), "hopper")
				.permission("grindgens.chunkhopper")
				.handler(context -> {
					try {
						Player player = (Player) context.getSender();
						ItemCreator itemCreator = plugin.itemCreator();
						player.getInventory().addItem(itemCreator.createHopper());
					} catch (IllegalArgumentException e) {
						context.getSender().sendMessage("Unknown Generator " + context.get("generator"));
					}
				}));
	}
}
