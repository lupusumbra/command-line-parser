package com.lupusumbra.util.commandline;

import com.lupusumbra.util.commandline.annotation.Command;
import com.lupusumbra.util.commandline.annotation.Option;
import com.lupusumbra.util.commandline.annotation.Parameters;
import com.lupusumbra.util.commandline.annotation.SubCommand;
import com.lupusumbra.util.commandline.exception.CommandLineParserException;
import com.lupusumbra.util.commandline.exception.UnknownOptionException;
import javafx.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public final class CommandLineParser {
    //string padding
    private static final String PADDING = String.format("%36s", "");
    //word splitter regex
    private static final Pattern WORD_SPLIT_REGEX = Pattern.compile("(?<!^)\\s+");
    //option prefix regex
    private static final Pattern OPTION_PREFIX = Pattern.compile("^-{1,2}");
    //option Comparator
    private static final Comparator<String> OPTION_COMPARATOR = Comparator.comparing(CommandLineParser::stripOptionPrefix);
    //CommandLineParser name
    private final String name;
    //options lookup by name
    private final Map<String, OptionParser> optionsByName = new HashMap<>();
    //options lookup by short-format name
    private final Map<String, OptionParser> optionsByShortKey = new HashMap<>();
    //options lookup by long-format name
    private final Map<String, OptionParser> optionsByLongKey = new HashMap<>();
    //SubCommand lookup by name
    private final Map<String, CommandLineParser> subCommands = new HashMap<>();
    //Command Argument Object
    private Object data = null;
    //The Command annotation
    private final Command annotation;
    //List of additional, non-option parameters
    private final List<String> parameters = new ArrayList<>();
    //selected flag
    private boolean selected;
    //selected SubCommand object
    private CommandLineParser selectedSubCommand = null;
    private String usageKeyShort = null;
    private String getUsageKeyLong = null;
    private Consumer<String> usage;

    /**
     * Creates a new instance of {@link CommandLineParser}
     * @param data the data Object
     */
    private CommandLineParser(final Object data) {
        this(data.getClass(), data);
    }

    /**
     * Creates a new instance of {@link CommandLineParser}
     * @param dataClass the data Object class
     */
    private CommandLineParser(final Class<?> dataClass) {
        this(dataClass, newInstance(dataClass));
    }

    /**
     * Creates a new instance of {@link CommandLineParser}
     * @param dataClass the data Object class
     * @param data the data Object
     */
    private CommandLineParser(final Class<?> dataClass, final Object data) {
        this.data = data;
        if (this.data == null)
            throw new CommandLineParserException("The Command Data Object *MUST* not be null, and *MUST* have a no-args public constructor");
        this.annotation = getAnnotation(dataClass, Command.class);
        if (this.annotation == null)
            throw new CommandLineParserException("A Command Data Object class *MUST* be annotated with @Command: " + dataClass.getName());
        this.name = this.annotation.name();
        fieldAnnotations(dataClass, SubCommand.class, this::analyzeSubCommandAnnotation);
        fieldAnnotations(dataClass, Option.class, this::analyzeCommandLineOptionAnnotation);
        fieldAnnotations(dataClass, Parameters.class, this::analyzeParametersAnnotation);
        Arrays.stream(this.annotation.usage()).forEach(this::setUsageKey);
        this.usage(this::defaultUsagePrinter);
    }

    /**
     * sets the usage key
     * @param key the usage key
     */
    private void setUsageKey(String key) {
        if(isTokenShortKey(key)) this.usageKeyShort = stripOptionPrefix(key);
        else this.getUsageKeyLong = stripOptionPrefix(key);
    }

    /**
     * defines the usage processor
     * @param usage usage message consumer
     * @return the {@link CommandLineParser}
     */
    public CommandLineParser usage(Consumer<String> usage) {
        this.usage = usage;
        return this;
    }

    /**
     * the default usage printer
     * @param message the usage message
     */
    public void defaultUsagePrinter(String message) {
        System.err.println(message);
    }

    /**
     * Check if this {@link CommandLineParser} has been selected
     * @return true if selected
     */
    public final boolean selected() {
        return this.selected;
    }

    /**
     * Returns the data object
     * @param <T> the desired Type
     * @return the data object
     */
    public final <T> T data() {
        //noinspection unchecked
        return (T) this.data;
    }

    /**
     * Get the {@link CommandLineParser} name
     * @return the name
     */
    public final String name() {
        return this.name;
    }

    /**
     * Returns a list of parameters
     * @return the list of parameters
     */
    public final List<String> parameters() {
        return this.parameters;
    }

    /**
     * Gets the SubCommand by registered name
     * @param name the registered name
     * @return the SubCommand
     */
    public final CommandLineParser subCommand(String name) {
        return this.subCommands.get(name);
    }

    /**
     * Get a list of all registered SubCommands
     * @return the list of SubCommands
     */
    public final Set<String> subCommands() {
        return this.subCommands.keySet();
    }

    /**
     * Register a {@link CommandLineParser} as a SubCommand
     * @param name the name of the SubCommand
     * @param commandArgsClass the Command Args Object class
     * @return the SubCommand
     */
    private CommandLineParser registerSubCommand(String name, Class<?> commandArgsClass) {
        return this.registerSubCommand(name, new CommandLineParser(commandArgsClass));
    }

    /**
     * Register a {@link CommandLineParser} as a SubCommand
     * @param name the name of the SubCommand
     * @param subCommand the SubCommand
     * @return the SubCommand
     */
    private CommandLineParser registerSubCommand(String name, CommandLineParser subCommand) {
        this.subCommands.put(name, subCommand);
        return subCommand;
    }

    /**
     * Analyzes a {@link Field} annotated with {@link SubCommand}
     * @param fieldAnnotation a {@link Pair} of {@link Field} and {@link SubCommand}
     */
    private void analyzeSubCommandAnnotation(Pair<Field,SubCommand> fieldAnnotation) {
        final CommandLineParser subCommand = this.registerSubCommand(fieldAnnotation.getValue().name(), fieldAnnotation.getKey().getType());
        setObjectValue(this.data, fieldAnnotation.getKey(), subCommand.data());
    }

    /**
     * Analyzes a {@link Field} annotated with {@link Parameters}
     * @param fieldAnnotation a {@link Pair} of {@link Field} and {@link Parameters}
     */
    private void analyzeParametersAnnotation(final Pair<Field,Parameters> fieldAnnotation) {
        setObjectValue(data, fieldAnnotation.getKey(), this.parameters);
    }

    /**
     * Analyzes a {@link Field} annotated with {@link Option}
     * @param fieldAnnotation a {@link Pair} of {@link Field} and {@link Option}
     */
    private void analyzeCommandLineOptionAnnotation(Pair<Field,Option> fieldAnnotation) {
        final Field field = fieldAnnotation.getKey();
        if(!field.isAccessible()) {
            try {
                field.setAccessible(true);
            } catch (Exception ignored) {
                throw new CommandLineParserException(String.format("@Option on field '%s' is invalid: it must be public, or you must have permission to call setAccessible on the field", field));
            }
        }
        final Option option = fieldAnnotation.getValue();
        final String shortKey = stripOptionPrefix(option.shortKey());
        final String longKey = stripOptionPrefix(option.longKey());
        final boolean invalidName = option.name().isEmpty();
        final boolean invalidKey = shortKey.isEmpty() && longKey.isEmpty();
        final boolean invalidConfig = option.hidden() && option.required();
        final boolean invalidAnnotation = invalidName || invalidKey || invalidConfig;
        if (invalidAnnotation)
            throw new CommandLineParserException(String.format("@Option on field '%s' is invalid: %s", field, invalidName ? "a name is required" : invalidKey ? "shortKey, longKey, or both must be selected" : "hidden and required cannot be requested for the same option"));
        final OptionParser optionParser = new OptionParser(field, option);
        final String optionName = option.name();
        if (this.optionsByName.containsKey(optionName)) {
            final OptionParser lastOptionParser = this.optionsByName.get(optionName);
            throw new CommandLineParserException(String.format("The '%s' option has been defined on multiple fields ('%s', '%s').", option, lastOptionParser.field.getName(), optionParser.field.getName()));
        }
        this.optionsByName.put(optionName, optionParser);
        if (!shortKey.isEmpty()) this.optionsByShortKey.put(shortKey, optionParser);
        if (!longKey.isEmpty()) this.optionsByLongKey.put(longKey, optionParser);
    }

    /**
     * Generates a String representation of the {@link CommandLineParser}
     * @return the String representation
     */
    @Override
    public String toString() {
        return "CommandLineParser{" +
                "name='" + name + '\'' +
                (subCommands.isEmpty()?"":(",subCommands=" + Arrays.toString(subCommands.keySet().toArray()))) +
                '}';
    }

    /**
     * generates a help String
     * @return the help String
     */
    private String toUsage() {
        final StringBuilder rval = new StringBuilder();
        this.appendDescription(rval,this.annotation.descriptions());
        final List<String> sorted = new ArrayList<>(this.optionsByName.size());
        this.optionsByName.values().stream().filter(option -> !option.annotation.hidden()).forEach(option -> sorted.add(option.toString()));
        sorted.sort(OPTION_COMPARATOR);
        sorted.forEach(optionString -> rval.append('\n').append(optionString));
        rval.append(formatSentences(this.annotation.detailedDescription(), true));
        return rval.toString();
    }

    /**
     * Append description to StringBuilder
     * @param rval the target {@link StringBuilder}
     * @param descriptions the String array containing descriptions
     */
    private void appendDescription(StringBuilder rval, String[] descriptions) {
        if(descriptions.length>0) {
            rval.append(descriptions[0]);
            if (descriptions.length > 1) {
                final String[] remaining = new String[descriptions.length - 1];
                System.arraycopy(descriptions,1,remaining,0,remaining.length);
                rval.append(formatSentences(remaining, false));
            }
        }
    }

    /**
     * Lookup an {@link OptionParser} by its registered name
     * @param name the registered name of the option parser (scoped by Command)
     * @return the OptionParser, or null if it doesn't exist
     */
    public final OptionParser optionParserByName(final String name) {
        return this.optionsByName.get(name);
    }

    /**
     * Lookup an {@link OptionParser} by its short-format key
     * @param key the short-format key
     * @return the OptionParser, or null if it doesn't exist
     */
    private OptionParser optionParserByShortKey(final String key) {
        return this.optionsByShortKey.get(key);
    }

    /**
     * Lookup an {@link OptionParser} by its long-format key
     * @param key the long-format key
     * @return the OptionParser, or null if it doesn't exist
     */
    private OptionParser optionParserByLongKey(final String key) {
        return this.optionsByLongKey.get(stripOptionPrefix(key));
    }

    /**
     * Checks if key is a short-format key for a boolean Option
     * @param key the key
     * @return true if key is a short-format key for a boolean Option
     */
    private boolean isBooleanShortKey(final String key) {
        final Class<?> type = this.optionsByShortKey.containsKey(key) ? this.optionParserByShortKey(key).fieldType : null;
        return type == boolean.class || type == Boolean.class;
    }

    /**
     * Process token String as an Option
     * @param token the token String
     * @return true if the next raw token is the value
     */
    private boolean parseTokenAsOption(final String token) {
        processToken(token, kv -> this.processOption(token, kv));
        final boolean isShort = isTokenShortKey(token);
        final String[] kvParts = stripOptionPrefix(token).split("=");
        return kvParts.length != 0 && (isShort && token.contains("=")) && !isBooleanShortKey(kvParts[0]);
    }

    /**
     * Process an Option token
     * @param optionToken the raw option token String
     * @param kv the option key-value token
     */
    private void processOption(String optionToken, String kv) {
        final String[] kvParts = kv.split("=");
        final String key = kvParts[0];
        final String value = kv.contains("=") ? kv.split("=")[1] : null;
        final OptionParser optionParser = isTokenShortKey(optionToken) ? optionParserByShortKey(key) : optionParserByLongKey(key);
        if (optionParser == null) throw new UnknownOptionException("Unknown option: '" + key + "'");
        optionParser.process(this.data, value);
    }

    /**
     * Checks to see if the token is a Sub Command
     * @param token the token
     * @return true if the token is a registered Sub Command
     */
    private boolean isSubCommand(String token) {
        return this.subCommands.containsKey(token);
    }

    /**
     * Appends a parameter
     * @param param the parameter to append
     */
    private void appendParameter(String param) {
        this.parameters.add(param);
    }

    private boolean isUsage(String kv) {
        final String[] kvParts = stripOptionPrefix(kv).split("=");
        return kvParts.length!=0 && (kvParts[0].equals(this.usageKeyShort) || kvParts[0].equals(this.getUsageKeyLong));
    }

    /**
     * Parse a key-value arg
     * @param kv the arg, in key-value format
     * @return true if the next token should be skipped (typically if the key is a boolean key)
     */
    private boolean parse(String kv) {
        this.selected = true;
        if(this.isUsage(kv)) this.usage.accept(this.toUsage());
        else if (this.isSubCommand(kv)) this.selectedSubCommand = subCommand(kv);
        else if (this.selectedSubCommand != null) return this.selectedSubCommand.parse(kv);
        else if (isOption(kv)) return this.parseTokenAsOption(kv);
        else this.appendParameter(kv);
        return false;
    }

    /**
     * Gets the selected Sub Command
     * @return the selected Sub Command
     */
    public CommandLineParser selectedSubCommand() {
        return this.selectedSubCommand;
    }

    /**
     * Checks whether a Sub Command is being used.
     *
     * @return true id a subCommand is selected
     */
    public boolean usingSubCommand() {
        return this.selectedSubCommand != null;
    }

    /**
     * converts args into key/value pairs.
     *
     * For long keys, this is pretty straightforward, as they are already in key=value form.
     * For short keys, we take the next arg if it isn't a key, and append it as a value, resulting in a key=value string
     *
     * @param args      the command line args
     * @param kvHandler a function which processes a key/value string, and returns true if the next arg should be skipped (typically this is the case if it's a non-boolean short form key)
     */
    private void processArgs(String[] args, Function<String, Boolean> kvHandler) {
        final int length = args.length;
        String curr, next;
        String kv;
        for (int i = 0; i < length; i++) {
            curr = args[i];
            next = i >= (length - 1) ? null : args[i + 1];
            kv = (curr.startsWith("-") && (next != null && !next.startsWith("-"))) ? (curr + '=' + next) : curr;
            if (kvHandler.apply(kv)) i++;
        }
    }

    /**
     * parse a command line
     * @param args the String[] containing the commandline
     * @return the {@link CommandLineParser}
     */
    private CommandLineParser parseArgs(String[] args) {
        this.processArgs(args, this::parse);
        return this;
    }

    /**
     * Strip the option prefix ("-" or "--") from the token
     * @param token the raw token
     * @return the stripped token
     */
    private static String stripOptionPrefix(String token) {
        return OPTION_PREFIX.matcher(token).replaceFirst("");
    }

    /**
     * Formats options
     * @param shortKey the short-format option key
     * @param longKey the long-format option key
     * @return the formatted string
     */
    public static String formatOptions(String shortKey, String longKey) {
        final String rval = longKey.isEmpty() ? String.format("  %s", shortKey) : shortKey.isEmpty() ? String.format("      %s", longKey) : String.format("  %s, %s", shortKey, longKey);
        return rval.length() > 32 ? String.format("%s\n%32s", rval, "") : String.format("%-32s", rval);
    }

    /**
     * Check if the token starts with a short-format key
     * @param token the token String
     * @return true if the token starts with a short-format key
     */
    private static boolean isTokenShortKey(String token) {
        return token.length() > 1 && token.charAt(0) == '-' && token.charAt(1) != '-';
    }

    /**
     * Process a token as a short-format key
     *
     * if it is a compound key ( for example -abc ), process as multiple short-format keys
     * @param token the raw token
     * @param kv the key-value consumer
     */
    private static void processShortKey(String token, Consumer<String> kv) {
        final String stripped = stripOptionPrefix(token);
        if(stripped.isEmpty())return;
        final String[] kvParts = stripped.split("=");
        if(kvParts.length==0)
            return;
        final boolean isCompoundKey = kvParts[0].length()>1;
        if(isCompoundKey) for (char c : stripped.toCharArray()) kv.accept("" + c);
        else kv.accept(stripped);
    }

    /**
     * Process a token as a long-format key
     *
     * @param token the raw token
     * @param kv the key-value consumer
     */
    private static void processLongKey(String token, Consumer<String> kv) {
        kv.accept(token);
    }

    /**
     * Process a token as a key
     * @param token the raw token
     * @param kv the key-value consumer
     */
    private static void processToken(String token, Consumer<String> kv) {
        if (isTokenShortKey(token)) processShortKey(token, kv);
        else processLongKey(stripOptionPrefix(token), kv);
    }

    /**
     * Checks if token matches the Option pattern (prefixed with either "-" or "--")
     * @param token the token
     * @return true if the token matches the Option prefix pattern
     */
    private static boolean isOption(String token) {
        return OPTION_PREFIX.matcher(token).find();
    }


    /**
     * Assigns a value to a field.
     *
     * This is a convenience wrapper, to prevent exception handling from cluttering the code.
     * @param data the target data object
     * @param field the target field
     * @param value the value to be assigned to the field
     */
    private static void setObjectValue(Object data, Field field, Object value) {
        try {
            field.set(data,value);
        } catch (Exception e) {
            System.err.println("Unable to set field(name='" + field.getName() + "', type='" + field.getType().getName() + "') with value: '" + value + "'");
        }
    }

    /**
     * format an array of Strings containing sentences into a single string
     * @param sentences the sentence array
     * @param embedded true newline should appear before a sentence, false if it should appear after
     * @return the formatted string
     */
    private static String formatSentences(final String[] sentences, final boolean embedded) {
        if (sentences.length == 0) return "";
        final StringBuilder rval = new StringBuilder(embedded ? "\n" : "");
        Arrays.stream(sentences).forEach(sentence -> appendSentence(rval,embedded,sentence));
        if (embedded) rval.deleteCharAt(rval.length() - 1);
        return rval.toString();
    }

    /**
     * An utility method to append a sentence to a {@link StringBuilder}
     * @param rval the StringBuilder
     * @param embedded true if the sentence is embedded into a larger text
     * @param sentence the sentence
     */
    private static void appendSentence(StringBuilder rval, boolean embedded, String sentence) {
        rval.append(embedded ? "\n" : "").append(formatSentence("\t" + sentence, false)).append(embedded ? "" : "\n");
    }

    /**
     * Format a sentence
     * @param sentence the sentence to format
     * @param indented true if wrapped lines should be indented
     * @return the formatted sentence
     */
    static String formatSentence(String sentence, boolean indented) {
        final StringBuilder rval = new StringBuilder();
        final String padding = indented ? PADDING : "";
        final int length = indented ? 44 : 80;
        StringBuilder line = new StringBuilder();
        for (String word : tokenizeSentence(sentence)) {
            if (line.length() + word.length() <= length) {
                line.append(word).append(' ');
            } else {
                rval.append(line.deleteCharAt(line.length() - 1)).append("\n\t").append(padding);
                line = new StringBuilder().append(word).append(' ');
            }
        }
        return rval.append(line.deleteCharAt(line.length() - 1)).toString();
    }

    /**
     * Tokenize a sentence into an List of words strings
     * @param sentence the sentence
     * @return the words
     */
    private static List<String> tokenizeSentence(String sentence) { // split to words
        return Arrays.asList(WORD_SPLIT_REGEX.split(sentence));
    }

    /**
     * A convenience method which calls a method to process all occurrences of a field Annotation
     *
     * @param classToScan the class to scan
     * @param annotationClass the Annotation we are scanning for
     * @param fieldAnnotationConsumer the Consumer which processes each occurrence
     * @param <T> the Type of the Annotation we are scanning for
     */
    private static <T extends Annotation> void fieldAnnotations(Class classToScan, Class<T> annotationClass, Consumer<Pair<Field, T>> fieldAnnotationConsumer) {
        for (Field field : classToScan.getDeclaredFields()) {
            if (!field.isSynthetic()) {
                final T annotation = field.getAnnotation(annotationClass);
                if (annotation != null) fieldAnnotationConsumer.accept(new Pair<>(field, annotation));
            }
        }
    }

    /**
     * A convenience method to get an Annotation from a class
     * @param classToScan the class to scan
     * @param annotationClass the Annotation we are scanning for
     * @param <T> the Type of the Annotation we are scanning for
     * @return the annotation
     */
    private static <T extends Annotation> T getAnnotation(Class classToScan, Class<T> annotationClass) {
        //noinspection unchecked
        return (T) classToScan.getAnnotation(annotationClass);
    }

    /**
     * Simple utility method which handles exceptions and returns null if it's unable to find or instantiate a class
     *
     * @param clazz the class to instantiate
     * @return the new instance of clazz
     */
    private static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * parse commandline arguments into a Command Data Args object of the commandDataClass type
     * @param commandDataClass the type of the Command Data Args object
     * @param args the commandline arguments
     * @return the root-level {@link CommandLineParser} object
     */
    public static CommandLineParser parse(final Class<?> commandDataClass, String[] args) {
        return new CommandLineParser(commandDataClass).parseArgs(args);
    }

    /**
     * parse commandline arguments into the Command Data Args object
     * @param commandDataObject the Command Data Args object
     * @param args the commandline arguments
     * @return the root-level {@link CommandLineParser} object
     */
    public static CommandLineParser parse(final Object commandDataObject, String[] args) {
        return new CommandLineParser(commandDataObject).parseArgs(args);
    }

    public <T> CommandLineParser then(Consumer<T> action){
        action.accept(this.data());
        return this;
    }
}
