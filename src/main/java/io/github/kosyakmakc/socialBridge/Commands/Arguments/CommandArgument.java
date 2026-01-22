package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import java.io.StringReader;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class CommandArgument<T> {

    public static CommandArgument<String> ofWord(String name)
    {
        return ofWord(name, new String[0]);
    }
    public static CommandArgument<String> ofWord(String name, String[] suggestions)
    {
        return ofWord(name, () -> CompletableFuture.completedFuture(suggestions));
    }

    public static CommandArgument<String> ofWord(String name, Supplier<CompletableFuture<String[]>> suggestionProvider)
    {
        return new WordCommandArgument(name, suggestionProvider);
    }

    public static CommandArgument<String> ofString(String name)
    {
        return ofString(name, new String[0]);
    }

    public static CommandArgument<String> ofString(String name, String[] suggestions)
    {
        return ofString(name, () -> CompletableFuture.completedFuture(suggestions));
    }

    public static CommandArgument<String> ofString(String name, Supplier<CompletableFuture<String[]>> suggestionProvider)
    {
        return new StringCommandArgument(name, suggestionProvider);
    }

    public static CommandArgument<String> ofGreedyString(String name)
    {
        return ofGreedyString(name, new String[0]);
    }

    public static CommandArgument<String> ofGreedyString(String name, String[] suggestions)
    {
        return ofGreedyString(name, () -> CompletableFuture.completedFuture(suggestions));
    }

    public static CommandArgument<String> ofGreedyString(String name, Supplier<CompletableFuture<String[]>> suggestionProvider)
    {
        return new GreedyStringCommandArgument(name, suggestionProvider);
    }

    public static CommandArgument<Integer> ofInteger(String name)
    {
        return ofInteger(name, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static CommandArgument<Integer> ofInteger(String name, int min, int max)
    {
        return new IntegerCommandArgument(name, min, max);
    }

    public static CommandArgument<Long> ofLong(String name)
    {
        return ofLong(name, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public static CommandArgument<Long> ofLong(String name, Long min, Long max)
    {
        return new LongCommandArgument(name, min, max);
    }

    public static CommandArgument<Float> ofFloat(String name)
    {
        return ofFloat(name, Float.MIN_VALUE, Float.MAX_VALUE);
    }

    public static CommandArgument<Float> ofFloat(String name, Float min, Float max)
    {
        return new FloatCommandArgument(name, min, max);
    }

    public static CommandArgument<Double> ofDouble(String name)
    {
        return ofDouble(name, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public static CommandArgument<Double> ofDouble(String name, Double min, Double max)
    {
        return new DoubleCommandArgument(name, min, max);
    }

    public static CommandArgument<Boolean> ofBoolean(String name)
    {
        return new BooleanCommandArgument(name);
    }

    public abstract CommandArgumentDataType getDataType();

    public abstract String getName();

    public abstract T getValue(StringReader args) throws ArgumentFormatException;
}
