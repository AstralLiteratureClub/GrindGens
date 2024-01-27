package bet.astral.grindgens.commands;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.generators.GeneratorType;
import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

public class GeneratorArgument<C> extends CommandArgument<C, GeneratorType> {
	private GeneratorArgument(
			final boolean required,
			final @NonNull String name,
			final @NonNull String defaultValue,
			final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
					@NonNull List<@NonNull String>> suggestionsProvider,
			final @NonNull ArgumentDescription defaultDescription
	) {
		super(required, name, new GeneratorArgumentParser<>(), defaultValue, GeneratorType.class, suggestionsProvider, defaultDescription);
	}

	public static <C> @NonNull CommandArgument<C, GeneratorType> of(final @NonNull String name) {
		return new GeneratorArgument.Builder<C>(name).asRequired().build();
	}

	public static <C> @NonNull CommandArgument<C, GeneratorType> optional(final @NonNull String name) {
		return new GeneratorArgument.Builder<C>(name).asOptional().build();
	}

	public static <C> @NonNull CommandArgument<C, GeneratorType> optional(
			final @NonNull String name,
			final @NonNull GeneratorType defaultType
	) {
		return new GeneratorArgument.Builder<C>(name).asOptionalWithDefault(defaultType.name()).build();
	}


	public static final class Builder<C> extends CommandArgument.Builder<C, GeneratorType> {

		private Builder(final @NonNull String name) {
			super(GeneratorType.class, name);
		}

		/**
		 * Builder a new boolean component
		 *
		 * @return Constructed component
		 */
		@Override
		public @NonNull GeneratorArgument<C> build() {
			return new GeneratorArgument<>(
					this.isRequired(),
					this.getName(),
					this.getDefaultValue(),
					this.getSuggestionsProvider(),
					this.getDefaultDescription()
			);
		}
	}






	public static class GeneratorArgumentParser<C> implements ArgumentParser<C, GeneratorType>{
		private final GrindGens grindGens = GrindGens.getPlugin(GrindGens.class);
		@Override
		public @NonNull ArgumentParseResult<@NonNull GeneratorType> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
			if (inputQueue.isEmpty()){
				return ArgumentParseResult.failure(new NoInputProvidedException(GeneratorType.class, commandContext));
			}
			String value = inputQueue.peek();
			GeneratorType generatorType = grindGens.globalComponentManager().getGeneratorType(value);
			if (generatorType == null){
				return ArgumentParseResult.failure(new GeneratorParseException(value, commandContext));
			}
			return ArgumentParseResult.success(generatorType);
		}

		@Override
		public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {

			List<String> suggestions = new ArrayList<>();
			for (GeneratorType type : grindGens.globalComponentManager().generatorTypes()){
				suggestions.add(type.name());
			}
			return suggestions;
		}
	}





	public static final class GeneratorParseException extends ParserException {

		private static final long serialVersionUID = 927476591631527552L;
		private final String input;

		/**
		 * Construct a new Player parse exception
		 *
		 * @param input   String input
		 * @param context Command context
		 */
		public GeneratorParseException(
				final @NonNull String input,
				final @NonNull CommandContext<?> context
		) {
			super(
					GeneratorArgumentParser.class,
					context,
					Caption.of("argument.parse.failure.generator"),
					CaptionVariable.of("input", input)
			);
			this.input = input;
		}

		/**
		 * Get the supplied input
		 *
		 * @return String value
		 */
		public @NonNull String getInput() {
			return this.input;
		}
	}
}
