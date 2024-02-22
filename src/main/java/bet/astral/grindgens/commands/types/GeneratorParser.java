/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.commands.types;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.generators.GeneratorType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class GeneratorParser<C> implements ArgumentParser<C, GeneratorType>, SuggestionProvider<C> {

	public static <C> @NotNull ParserDescriptor<C, GeneratorType> parser(){
		return ParserDescriptor.of(new GeneratorParser<>(), GeneratorType.class);
	}

	public static <C> CommandComponent.@NotNull Builder<C, GeneratorType> component(){
		return new CommandComponent.Builder<C, GeneratorType>().parser(parser());
	}

	@Override
	public @NonNull ArgumentParseResult<@NonNull GeneratorType> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {
		String input = commandInput.peekString();
		GrindGens grindGens = GrindGens.getPlugin(GrindGens.class);
		GlobalComponentManager componentManager = grindGens.globalComponentManager();
		GeneratorType type = componentManager.getGeneratorType(input);
		if (type == null) {
			return ArgumentParseResult.failure(new GeneratorParseException(input, commandContext));
		}
		commandInput.readString();
		return ArgumentParseResult.success(type);
	}
	@Override
	public @NonNull CompletableFuture<@NonNull ArgumentParseResult<GeneratorType>> parseFuture(@NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {
		return CompletableFuture.completedFuture(this.parse(commandContext, commandInput));
	}

	@Override
	public @NonNull CompletableFuture<@NonNull Iterable<@NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<C> context, @NonNull CommandInput input) {
		return CompletableFuture.completedFuture(GrindGens.getPlugin(GrindGens.class).globalComponentManager().generatorTypes().stream().map(GeneratorType::name).map(Suggestion::simple).collect(Collectors.toList()));
	}

	public static final class GeneratorParseException extends ParserException {
		private final String input;

		/**
		 * Construct a new Generator Type parse exception
		 *
		 * @param input   String input
		 * @param context Command context
		 */
		public GeneratorParseException(
				final @NonNull String input,
				final @NonNull CommandContext<?> context
		) {
			super(
					GeneratorType.class,
					context,
					StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_UUID,
					CaptionVariable.of("input", input)
			);
			this.input = input;
		}

		/**
		 * Returns the supplied input.
		 *
		 * @return string value
		 */
		public String input() {
			return this.input;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || this.getClass() != o.getClass()) {
				return false;
			}
			final GeneratorParseException that = (GeneratorParseException) o;
			return this.input.equals(that.input);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.input);
		}
	}
}
