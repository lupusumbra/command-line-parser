package com.lupusumbra.util.commandline.test.subcommands.config;

import com.lupusumbra.util.commandline.annotation.Command;
import com.lupusumbra.util.commandline.annotation.Option;
import com.lupusumbra.util.commandline.annotation.SubCommand;

@SuppressWarnings("WeakerAccess")
@Command(name="root")
public class RootConfigObject {
    @SuppressWarnings("WeakerAccess")
    @Option(name="name",shortKey = "", longKey = "--name", description = "The Name")
    public String name;
    @Option(name="verbose",shortKey = "-v", longKey = "--verbose", description = "Verbose Output")
     boolean verbose;
    @SubCommand(name = "create")
    public CreateCommandArgs create;
    @SubCommand(name = "search")
    public SearchConfigArgs search;

    @Override
    public String toString() {
        return "RootConfigObject{" +
                "name='" + name + '\'' +
                ", verbose=" + verbose +
                ", create=" + create +
                ", search=" + search +
                '}';
    }
}
