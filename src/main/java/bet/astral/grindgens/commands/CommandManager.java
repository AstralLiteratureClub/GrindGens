/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.commands;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.commands.types.GeneratorParser;
import bet.astral.grindgens.manager.ItemCreator;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.generators.Generator;
import bet.astral.grindgens.models.generators.GeneratorType;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.parser.flag.FlagContext;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;

import java.text.DecimalFormat;

public class CommandManager extends PaperCommandManager<CommandSender> {
	private final GrindGens plugin;

	static ExecutionCoordinator<CommandSender> executionCoordinator() {
		return ExecutionCoordinator.asyncCoordinator();
	}

	public CommandManager(GrindGens grindGens) {
		super(grindGens, executionCoordinator(), SenderMapper.identity());
		this.plugin = grindGens;

		// Brigadier
		if (hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER))
			registerBrigadier();
		// Tab Completion
		if (hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
			registerAsynchronousCompletions();

		parserRegistry()
				.registerParser(GeneratorParser.parser())
		;

		Command.Builder<CommandSender> genCommand = commandBuilder(
				"generator",
				Description.of("General command for giving/looking/debugging generators"),
				"generators", "gen", "gens")
				.permission("grindgens.generator")
				.handler(context -> {
					CommandSender sender = context.sender();
					sender.sendMessage("hey!");
				});

		command(genCommand);

		command(genCommand
				.literal("give")
				.permission("grindgens.generator.give")
				.required(
//						PlayerParser.playerComponent()
						GeneratorParser.component()
						.name("gen_give")
						.description(Description.of("The generator type to give"))
				)
				.flag(CommandFlag.builder("amount")
						.withDescription(Description.of("The amount of the generators to give"))
						.withComponent(
								IntegerParser.integerComponent()
										.parser(new IntegerParser<>(1, 64))
										.name("amount")
										.description(Description.of("Amount to give"))
										.required(true)
						)
				)
				.flag(
						CommandFlag.builder("drop-tier")
								.withDescription(Description.of("The drop rate tier of the generator"))
								.withComponent(
										IntegerParser.integerComponent()
												.parser(new IntegerParser<>(0, 8))
												.name("tier")
												.description(Description.of("Tier of the drop rate"))
												.required(true))
 				)
				.flag(
						CommandFlag.builder("value-tier")
								.withDescription(Description.of("The value tier of the generator"))
								.withComponent(
										IntegerParser.integerComponent()
												.parser(new IntegerParser<>(0, 8))
												.name("tier")
												.description(Description.of("Tier of value"))
												.required(true)
								)
				)
				.senderType(Player.class)
				.handler(context -> {
					Player player = context.sender();
					GeneratorType generatorType = context.get("gen_give");
					FlagContext flagContext = context.flags();
					int amount = (int) flagContext.getValue("amount").orElse(1);
					int tierDrop = (int) flagContext.getValue("drop-tier").orElse(1);
					int tierValue = (int) flagContext.getValue("value-tier").orElse(1);

					ItemCreator itemCreator = plugin.itemCreator();
					ItemStack item = itemCreator.createGenerator(generatorType, tierDrop, tierValue);
					item.setAmount(amount);

					player.getInventory().addItem(item);
					player.sendRichMessage("<green>You were given generator <gray>x<white>"+amount+" "+generatorType.name());
				})
		);

		command(
				genCommand
						.literal("list")
						.permission("grindgens.generator.list")
						.handler(context->{
							for (GeneratorType type : plugin.globalComponentManager().generatorTypes()) {
								context.sender()
										.sendRichMessage(
												"<white>Type: "+ type.name()
										);
							}
						})
		);

		command(
				genCommand
						.literal("info")
						.permission("grindgens.generator.info")
						.flag(
								CommandFlag.builder("tool")
										.withDescription(Description.of("Use player's tool instead of target block"))
						)
						.senderType(Player.class)
						.handler(context -> {
							Player player = context.sender();
							boolean isPlayerTool = context.flags().isPresent("tool");
							Generator generator = null;
							if (isPlayerTool){
								ItemStack itemStack = player.getInventory().getItemInMainHand();
								generator = plugin.itemCreator().asGeneratorDefaultInstance(itemStack);
								if (generator == null){
									player.sendRichMessage("<red>Your item is not a generator. Switch to a generator to see <underlined>debug</underlined> information of it");
									return;
								}
							} else{
								RayTraceResult rayTraceResult = player.rayTraceBlocks(15, FluidCollisionMode.NEVER);
								if (rayTraceResult == null || rayTraceResult.getHitBlock() == null){
									player.sendRichMessage("<red>Failed to find targeted block. Go near the block to see debug information of it!");
									return;
								}
								Block block = rayTraceResult.getHitBlock();
								Location location = block.getLocation();

								GlobalComponentManager componentManager = plugin.globalComponentManager();
								Component component = componentManager.get(location);
								if (component instanceof Generator gen){
									generator = gen;
								} else {
									if (component == null) {
										player.sendRichMessage("<red>There is no component where you are looking at. Maybe aim better.");
										return;
									} else {
										player.sendRichMessage("<red>The aimed block is not a generator but a <white>"+component.type().name());
										return;
									}
								}
							}

							player.sendMessage(
									MiniMessage.miniMessage().deserialize(
											"<gray>Com Type: <white>"+ generator.type().name()+"\n"+
											"<gray>Gen Type: <white>"+ generator.type().name()+"\n"+
											"<gray>Value Tier: <white>"+ generator.valueTier()+"\n"+
											"<gray>Drop Tier: <white>"+ generator.dropRateTier()+"\n"+
											"<gray>Drop-Rate (DPS): <white>"+ (generator.generatorType().dropRateTier(generator.dropRateTier()).value()*0.05)+"\n"+
											"<gray>Value: <white>"+ generator.generatorType().valueTier(generator.valueTier()).value()+"\n"+
											"<gray>Current Ticks: <white>"+ generator.ticks()));
						})
		);

		command(
				genCommand
						.literal("highlight")
						.permission("grindgens.generator.highlight")
						.senderType(Player.class)
						.handler(context->{
							Player player = context.sender();
							GenPlayer genPlayer = plugin.playerManager().asGenPlayer(player);
							for (Component component : genPlayer.components()) {
								if (component instanceof Generator) {
									try {
										plugin.glowingBlocks().setGlowing(component.location(), player, ChatColor.RED);
									} catch (ReflectiveOperationException e) {
										e.printStackTrace();
									}
								}
							}
						})
		);



		Command.Builder<CommandSender> maxGens = genCommand.literal("setmaxgens", "setmaxgenerators")
				.permission("grindgens.generator.setmax");

		command(maxGens.argument(PlayerParser.playerComponent().description(Description.of("Player to receive the update"))
				.name("player")
				.required())
				.argument(
						IntegerParser.integerComponent().parser(IntegerParser.integerParser(0, 1000)).description(Description.of("Amount to set to"))
								.required()
								.name("amount")
				)
				.handler(context->{
					CommandSender sender = context.sender();
					Player other = context.get("player");
					int max = context.get("amount");
					GenPlayer genPlayer = grindGens.playerManager().asGenPlayer(other);
					genPlayer.setMaxGenerators(max);
					genPlayer.requestSave();
					sender.sendRichMessage("<yellow>Updated "+other.getName()+"'s max generators to "+ max);
					other.sendRichMessage("<yellow>You can now place up to "+ max);
				})
		);

		command(genCommand.literal("reload")
				.permission("grindgens.reload")
				.handler(context->{
					grindGens.reloadConfig();
					context.sender().sendRichMessage("<green>Reloaded!");
				})
		);


		DecimalFormat decimalFormat = new DecimalFormat(".00000");

		command(commandBuilder("bitcoins", "btc", "bitcoin").senderType(Player.class)
				.handler(context->{
					Player player = context.sender();
					GenPlayer genPlayer = grindGens.playerManager().asGenPlayer(player);
					player.sendRichMessage("<green>Bitcoins: <white>"+ decimalFormat.format(genPlayer.getBitcoins()) +" btc");
				}));


		MinecraftHelp<CommandSender> help = MinecraftHelp.<CommandSender>builder()
				.commandManager(CommandManager.this)
				.audienceProvider(AudienceProvider.nativeAudience())
				.commandPrefix("/generators help")
				.colors(new MinecraftHelp.HelpColors() {
					@Override
					public @NonNull TextColor primary() {
						return NamedTextColor.YELLOW;
					}

					@Override
					public @NonNull TextColor highlight() {
						return NamedTextColor.WHITE;
					}

					@Override
					public @NonNull TextColor alternateHighlight() {
						return NamedTextColor.GOLD;
					}

					@Override
					public @NonNull TextColor text() {
						return NamedTextColor.GRAY;
					}

					@Override
					public @NonNull TextColor accent() {
						return NamedTextColor.GREEN;
					}
				})
				.maxResultsPerPage(7)
				.build();

		command(genCommand.literal("help", Description.of("The help sub command for /generator")).optional("query", StringParser.greedyStringParser(), DefaultValue.constant("")).handler(context->{
			help.queryCommands(context.get("query"), context.sender());
		}));
	}
}