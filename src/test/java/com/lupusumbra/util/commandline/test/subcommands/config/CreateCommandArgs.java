package com.lupusumbra.util.commandline.test.subcommands.config;

import com.lupusumbra.util.commandline.annotation.Command;
import com.lupusumbra.util.commandline.annotation.Option;
import com.lupusumbra.util.commandline.annotation.SubCommand;

@SuppressWarnings("WeakerAccess")
@Command(name="create")
public class CreateCommandArgs {
    @Option(name="username",shortKey = "-u", longKey = "--username", description = "Username")
    public String username;
    @Option(name="first-name",shortKey = "-F", longKey = "--first-name", description = "First Name")
    public String firstName;
    @Option(name="last-name",shortKey = "-L", longKey = "--last-name", description = "Last Name")
    public String lastName;
    @Option(name="email",shortKey = "-E", longKey = "--email", description = "Email Address")
    public String email;
    @SubCommand(name = "groups")
    public GroupsCommandArgs groups;

    @Override
    public String toString() {
        return "CreateCommandArgs{" +
                "username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", groups=" + groups +
                '}';
    }
}
