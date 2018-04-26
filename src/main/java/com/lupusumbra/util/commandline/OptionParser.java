package com.lupusumbra.util.commandline;


import com.lupusumbra.util.commandline.annotation.Option;
import com.lupusumbra.util.commandline.exception.CommandLineParserException;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;


public class OptionParser {
    public final Field field;
    public final Class fieldType;
    public final Option annotation;
    private boolean provided;

    OptionParser(Field field, Option annotation) {
        this.field = field;
        this.fieldType = field.getType();
        this.annotation = annotation;
        this.provided = false;
    }

    public boolean provided() {
        return this.provided;
    }

    public String toString() {
        final String optsText = CommandLineParser.formatOptions(this.annotation.shortKey(), this.annotation.longKey());
        final String descText = CommandLineParser.formatSentence(this.annotation.description(), true);
        return String.format("%s  %s", optsText, descText);
    }

    public void process(final Object data, String value) {
        if (this.fieldType.equals(Boolean.class) || this.fieldType.equals(boolean.class)) value = "true";
        if (value == null || value.isEmpty()) value = this.annotation.defaultValue();
        setValue(data, this.field, value);
        this.provided = true;
    }

    private static void setValue(Object data, Field field, String value) {
        final Class<?> type = field.getType();
        try {
            if (type == String.class) {
                field.set(data, value);
            } else if (type == boolean.class || type == Boolean.class) {
                field.set(data, Boolean.parseBoolean(value));
            } else if (type == int.class || type == Integer.class) {
                field.set(data, Integer.decode(value));
            } else if (type == long.class || type == Long.class) {
                field.set(data, Long.decode(value));
            } else if (type == byte.class || type == Byte.class) {
                field.set(data, Byte.decode(value));
            } else if (type == short.class || type == Short.class) {
                field.set(data, Short.decode(value));
            } else if (type == double.class || type == Double.class) {
                field.set(data, Double.parseDouble(value));
            } else if (type == float.class || type == Float.class) {
                field.set(data, Float.parseFloat(value));
            } else if (type == char.class || type == Character.class) {
                field.set(data, value.startsWith("0x")?((char)Integer.parseInt(value.substring(2),16)):value.charAt(0));
            } else if (type == File.class) {
                field.set(data, new File(value));
            } else if (type == Path.class) {
                field.set(data, Paths.get(value));
            } else throw new CommandLineParserException("Unsupported object type");
        } catch (Exception e) {
            System.err.println("Unable to set field(name='" + field.getName() + "', type='" + type.getName() + "') with value: '" + value + "'");
            e.printStackTrace();
        }
    }
}
