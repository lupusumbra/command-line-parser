package com.lupusumbra.util.commandline.test.basic;

import com.lupusumbra.util.commandline.CommandLineParser;
import com.lupusumbra.util.commandline.exception.CommandLineParserException;
import com.lupusumbra.util.commandline.exception.UnknownOptionException;
import com.lupusumbra.util.commandline.test.basic.config.*;
import org.junit.Test;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

@SuppressWarnings("Duplicates")
public class BasicTests {
    private static final PrintStream stderr = System.err;

    @Test
    public void testNoArgs() {
        final String[] args = {};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertEquals(bco.name, null);
        assertFalse(root.optionParserByName("name").provided());
        assertFalse(root.optionParserByName("verbose").provided());
        assertFalse(root.optionParserByName("depth").provided());
        assertFalse(root.optionParserByName("value").provided());
        assertFalse(root.optionParserByName("balance").provided());
        assertFalse(root.optionParserByName("long-value").provided());
        assertFalse(root.optionParserByName("alpha").provided());
        assertFalse(root.optionParserByName("beta").provided());
        assertFalse(root.optionParserByName("charlie").provided());
    }

    @Test
    public void testNoArgsWithObject() {
        final String[] args = {};
        BasicConfigObject bco = new BasicConfigObject();
        CommandLineParser root = CommandLineParser.parse(bco, args);
        assertEquals(root.data(), bco);
        assertTrue(bco == root.data());
        assertEquals(bco.name, null);
        assertFalse(root.optionParserByName("name").provided());
        assertFalse(root.optionParserByName("verbose").provided());
        assertFalse(root.optionParserByName("depth").provided());
        assertFalse(root.optionParserByName("value").provided());
        assertFalse(root.optionParserByName("balance").provided());
        assertFalse(root.optionParserByName("long-value").provided());
        assertFalse(root.optionParserByName("alpha").provided());
        assertFalse(root.optionParserByName("beta").provided());
        assertFalse(root.optionParserByName("charlie").provided());
    }

    @Test
    public void testLongKeyString() {
        final String[] args = {"--name=test"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertEquals(bco.name, "test");
        assertTrue(root.optionParserByName("name").provided());
        assertFalse(root.optionParserByName("verbose").provided());
        assertFalse(root.optionParserByName("depth").provided());
        assertFalse(root.optionParserByName("value").provided());
        assertFalse(root.optionParserByName("balance").provided());
        assertFalse(root.optionParserByName("long-value").provided());
        assertFalse(root.optionParserByName("alpha").provided());
        assertFalse(root.optionParserByName("beta").provided());
        assertFalse(root.optionParserByName("charlie").provided());
    }

    @Test
    public void testShortKeyBoolean() {
        final String[] args = {"-v"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertEquals(bco.verbose, true);
        assertFalse(root.optionParserByName("name").provided());
        assertTrue(root.optionParserByName("verbose").provided());
        assertFalse(root.optionParserByName("depth").provided());
        assertFalse(root.optionParserByName("value").provided());
        assertFalse(root.optionParserByName("balance").provided());
        assertFalse(root.optionParserByName("long-value").provided());
        assertFalse(root.optionParserByName("alpha").provided());
        assertFalse(root.optionParserByName("beta").provided());
        assertFalse(root.optionParserByName("charlie").provided());
    }

    @Test
    public void testCompoundShortKeyBoolean() {
        final String[] args = {"-abc"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertEquals(bco.alpha, true);
        assertEquals(bco.beta, true);
        assertEquals(bco.charlie, true);
        assertFalse(root.optionParserByName("name").provided());
        assertFalse(root.optionParserByName("verbose").provided());
        assertFalse(root.optionParserByName("depth").provided());
        assertFalse(root.optionParserByName("value").provided());
        assertFalse(root.optionParserByName("balance").provided());
        assertFalse(root.optionParserByName("long-value").provided());
        assertTrue(root.optionParserByName("alpha").provided());
        assertTrue(root.optionParserByName("beta").provided());
        assertTrue(root.optionParserByName("charlie").provided());
    }

    @Test
    public void testShortKeyInt() {
        final String[] args = {"-d", "1"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertEquals(bco.depth, 1);
        assertFalse(root.optionParserByName("name").provided());
        assertFalse(root.optionParserByName("verbose").provided());
        assertTrue(root.optionParserByName("depth").provided());
        assertFalse(root.optionParserByName("value").provided());
        assertFalse(root.optionParserByName("balance").provided());
        assertFalse(root.optionParserByName("long-value").provided());
        assertFalse(root.optionParserByName("alpha").provided());
        assertFalse(root.optionParserByName("beta").provided());
        assertFalse(root.optionParserByName("charlie").provided());
    }

    @Test
    public void testShortKeyLong() {
        final String[] args = {"-l", "1"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertTrue(bco.longValue == 1L);
        assertFalse(root.optionParserByName("name").provided());
        assertFalse(root.optionParserByName("verbose").provided());
        assertFalse(root.optionParserByName("depth").provided());
        assertFalse(root.optionParserByName("value").provided());
        assertFalse(root.optionParserByName("balance").provided());
        assertTrue(root.optionParserByName("long-value").provided());
        assertFalse(root.optionParserByName("alpha").provided());
        assertFalse(root.optionParserByName("beta").provided());
        assertFalse(root.optionParserByName("charlie").provided());
    }

    @Test
    public void testLongKeyFloat() {
        final String[] args = {"--value=0.93"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertEquals(Float.toString(bco.value), Float.toString(0.93f));
        assertFalse(root.optionParserByName("name").provided());
        assertFalse(root.optionParserByName("verbose").provided());
        assertFalse(root.optionParserByName("depth").provided());
        assertTrue(root.optionParserByName("value").provided());
        assertFalse(root.optionParserByName("balance").provided());
        assertFalse(root.optionParserByName("long-value").provided());
        assertFalse(root.optionParserByName("alpha").provided());
        assertFalse(root.optionParserByName("beta").provided());
        assertFalse(root.optionParserByName("charlie").provided());
    }

    @Test
    public void testLongKeyDouble() {
        final String[] args = {"--balance=0.93"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertTrue(bco.balance > 0);
        assertFalse(root.optionParserByName("name").provided());
        assertFalse(root.optionParserByName("verbose").provided());
        assertFalse(root.optionParserByName("depth").provided());
        assertFalse(root.optionParserByName("value").provided());
        assertTrue(root.optionParserByName("balance").provided());
        assertFalse(root.optionParserByName("long-value").provided());
        assertFalse(root.optionParserByName("alpha").provided());
        assertFalse(root.optionParserByName("beta").provided());
        assertFalse(root.optionParserByName("charlie").provided());
    }

    @Test
    public void testLongKeyByte() {
        final String[] args = {"--byte=0x34"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertTrue(root.optionParserByName("aByte").provided());
        assertEquals(bco.aByte, 0x34);
    }

    @Test
    public void testLongKeyShort() {
        final String[] args = {"--short=0x34"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertTrue(root.optionParserByName("aShort").provided());
        assertEquals(bco.aShort, 0x34);
    }

    @Test
    public void testLongKeyChar() {
        final String[] args = {"--char=d"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertTrue(root.optionParserByName("aChar").provided());
        assertEquals(bco.aChar, 'd');
    }

    @Test
    public void testLongKeyCharHex() {
        final String[] args = {"--char=0xa1"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertTrue(root.optionParserByName("aChar").provided());
        assertEquals(bco.aChar, (char) 0xa1);
    }

    @Test
    public void testLongKeyCharUnicode() {
        final String[] args = {"--char=\u3443"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertTrue(root.optionParserByName("aChar").provided());
        assertEquals(bco.aChar, '\u3443');
    }

    @Test
    public void testLongKeyFile() {
        final String[] args = {"--file=./src/test/test.txt"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertTrue(root.optionParserByName("file").provided());
        assertEquals(bco.file, new File("./src/test/test.txt"));
    }

    @Test
    public void testLongKeyPath() {
        final String[] args = {"--path=./src/test/test.txt"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertTrue(root.optionParserByName("path").provided());
        assertEquals(bco.path, new File("./src/test/test.txt").toPath());
    }

    @Test
    public void testLongKeyObject() {
        final String[] args = {"--object=this should fail"};
        null_stderr();
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertTrue(root.optionParserByName("object").provided());
        assertNull(bco.anObject);
        reset_stderr();
    }

    @Test
    public void testArgWithValueContainingWhitespace() {
        final String[] args = {"--name=This is a Test"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        BasicConfigObject bco = root.data();
        assertEquals(bco.name, "This is a Test");
        assertTrue(root.optionParserByName("name").provided());
        assertFalse(root.optionParserByName("verbose").provided());
        assertFalse(root.optionParserByName("depth").provided());
        assertFalse(root.optionParserByName("value").provided());
        assertFalse(root.optionParserByName("balance").provided());
        assertFalse(root.optionParserByName("long-value").provided());
        assertFalse(root.optionParserByName("alpha").provided());
        assertFalse(root.optionParserByName("beta").provided());
        assertFalse(root.optionParserByName("charlie").provided());
    }

    @Test(expected = UnknownOptionException.class)
    public void testUnknownOption() {
        final String[] args = {"--name=This is a Test", "--non-existing"};
        CommandLineParser.parse(BasicConfigObject.class, args);
    }

    @Test
    public void testToString() {
        final String[] args = {"--name=This is a Test"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args);
        assertNotNull(root.toString());
    }

    @Test
    public void testThen() {
        final String[] args = {"--name=This is a Test"};
        CommandLineParser root = CommandLineParser.parse(BasicConfigObject.class, args).then(this::processBCO);
        assertNotNull(root.toString());

    }

    private void processBCO(BasicConfigObject bco) {
        assertNotNull(bco.toString());
    }

    @Test
    public void testStrayEqualSign() {
        final String[] args = {"-="};
        CommandLineParser.parse(BasicConfigObject.class, args);
    }

    @Test
    public void testNewInstanceException() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method m = CommandLineParser.class.getDeclaredMethod("newInstance", Class.class);
        m.setAccessible(true);
        final Object rval = m.invoke(null, (Class) null);
        assertEquals(rval, null);
    }

    @Test
    public void testSetObjectValuePermissionIssue() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        final Method m = CommandLineParser.class.getDeclaredMethod("setObjectValue", Object.class, Field.class, Object.class);
        m.setAccessible(true);
        final Object bco = new BasicConfigObjectWithFinalField();
        final Field f = bco.getClass().getField("alpha");
        null_stderr();
        m.invoke(null, bco, f, true);
        reset_stderr();
        assertTrue(true);
    }

    @Test(expected = CommandLineParserException.class)
    public void testNoNameOption() {
        final String[] args = {};
        CommandLineParser.parse(BasicConfigObjectWithNoNameOption.class, args);
    }

    @Test(expected = CommandLineParserException.class)
    public void testFinalOptionField() {
        final String[] args = {};
        final SecurityManager original = System.getSecurityManager();
        System.setProperty("java.security.policy", new File("./src/test/change_security_manager_only.policy").getAbsolutePath());
        null_stderr();
        System.setSecurityManager(new SecurityManager());
        try {
            CommandLineParser.parse(BasicConfigObjectWithFinalField.class, args);
        } finally {
            System.setSecurityManager(original);
            reset_stderr();
        }
    }

    @Test(expected = CommandLineParserException.class)
    public void testNoNoArgsDataObject() {
        final String[] args = {};
        CommandLineParser.parse(BasicConfigObjectWithInvalidConstructor.class, args);
    }

    @Test(expected = CommandLineParserException.class)
    public void testInvalidDataObject() {
        final String[] args = {};
        CommandLineParser.parse(Object.class, args);
    }

    @Test(expected = CommandLineParserException.class)
    public void testDuplicateOptions() {
        final String[] args = {};
        CommandLineParser.parse(BasicConfigObjectWithDuplicateOptions.class, args);
    }

    @Test
    public void testDefaultUsage() {
        System.out.println(); //ensure we are on a new line
        final String[] args = {"-h"};
        //dump to null for now, once the test is finalized, we'll check against a file
        null_stderr();
        CommandLineParser.parse(BasicConfigObject.class, args);
        reset_stderr();
    }


    private static void null_stderr() {
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
    }

    private static void reset_stderr() {
        System.setErr(stderr);
    }
}
