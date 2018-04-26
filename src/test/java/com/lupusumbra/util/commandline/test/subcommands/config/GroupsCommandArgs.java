package com.lupusumbra.util.commandline.test.subcommands.config;

import com.lupusumbra.util.commandline.annotation.Command;
import com.lupusumbra.util.commandline.annotation.Parameters;

import java.util.List;

@SuppressWarnings("WeakerAccess")
@Command(name = "groups")
public class GroupsCommandArgs {
    @Parameters
    public List<String> members;

    @Override
    public String toString() {
        return "GroupsCommandArgs{" +
                "members=" + members +
                '}';
    }
}
