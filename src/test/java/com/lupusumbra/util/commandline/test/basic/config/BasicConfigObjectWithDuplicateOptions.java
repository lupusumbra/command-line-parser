package com.lupusumbra.util.commandline.test.basic.config;

import com.lupusumbra.util.commandline.annotation.Command;
import com.lupusumbra.util.commandline.annotation.Option;

@SuppressWarnings("WeakerAccess")
@Command(name="root")
public class BasicConfigObjectWithDuplicateOptions {
    @Option(name="name",shortKey = "", longKey = "--name", description = "The Name")
    public String name;
    @Option(name="verbose",shortKey = "-v", longKey = "--verbose", description = "Verbose Output")
    public boolean verbose;
    @Option(name="depth",shortKey = "-d", longKey = "--depth", description = "Depth")
    public int depth;
    @Option(name="value",shortKey = "", longKey = "--value", description = "Value")
    public float value;
    @Option(name="balance",shortKey = "", longKey = "--balance", description = "Balance")
    public double balance;
    @Option(name="long-value",shortKey = "-l", longKey = "--long-value", description = "Long Value")
    public Long longValue;
    @Option(name="alpha",shortKey = "-a", longKey = "--alpha", description = "Alpha select")
    public final Boolean alpha = false;
    @Option(name="alpha",shortKey = "-b", longKey = "--beta", description = "Beta select")
    public Boolean beta;
    @Option(name="charlie",shortKey = "-c", longKey = "--charlie", description = "Charlie select")
    public Boolean charlie;

    @Override
    public String toString() {
        return "BasicConfigObject{" +
                "name='" + name + '\'' +
                ", verbose=" + verbose +
                ", depth=" + depth +
                ", value=" + value +
                ", balance=" + balance +
                ", longValue=" + longValue +
                ", alpha=" + alpha +
                ", beta=" + beta +
                ", charlie=" + charlie +
                '}';
    }
}
