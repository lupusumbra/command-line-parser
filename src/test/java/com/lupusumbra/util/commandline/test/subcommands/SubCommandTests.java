package com.lupusumbra.util.commandline.test.subcommands;

import com.lupusumbra.util.commandline.CommandLineParser;
import com.lupusumbra.util.commandline.test.subcommands.config.CreateCommandArgs;
import com.lupusumbra.util.commandline.test.subcommands.config.RootConfigObject;
import com.lupusumbra.util.commandline.test.subcommands.config.SearchConfigArgs;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

@SuppressWarnings("Duplicates")
public class SubCommandTests {
    @Test
    public void testNoArgs() {
        final String[] args = {};
        CommandLineParser.parse(RootConfigObject.class,args);
    }

    @Test
    public void testCreateShort() {
        final String[] args = {"--name=test", "-v", "create", "--username=testuser", "--first-name=Test user", "-L", "Dealer", "-E", "test@user.com", "groups", "wheel", "bin", "users"};
        CommandLineParser root = CommandLineParser.parse(RootConfigObject.class,args);
        assertTrue(root.selected());
        assertTrue(root.usingSubCommand());
        assertNotNull(root.selectedSubCommand());
        assertTrue(root.selectedSubCommand().selected());
        assertEquals(root.selectedSubCommand().name(),"create");
        final Set<String> subCommands = root.subCommands();
        assertEquals(subCommands.size(),2);
        assertEquals(root.subCommand("create").data().getClass(), CreateCommandArgs.class);
        assertEquals(root.subCommand("search").data().getClass(), SearchConfigArgs.class);
        assertEquals(root.subCommand("create").parameters().size(),0);
        assertEquals(root.subCommand("create").subCommand("groups").parameters().size(),3);
    }
}
