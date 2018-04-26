package com.lupusumbra.util.commandline.test.basic.config;

import com.lupusumbra.util.commandline.annotation.Command;
import com.lupusumbra.util.commandline.annotation.Option;

import java.io.File;
import java.nio.file.Path;

@Command(name="root",descriptions = {"USAGE: testexec <options> <parameters>", "This is a typical test program,", "with a dummy description,","And an intentionally very, very, very, very, very, very, very, very, very, very, very, very, very, very, very, very, very, very, very, very, very, very long line"})
public class BasicConfigObject {
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
    public Boolean alpha;
    @Option(name="beta",shortKey = "-b", longKey = "--beta", description = "Beta select")
    public Boolean beta;
    @Option(name="charlie",shortKey = "-c", longKey = "--charlie", description = "Charlie select")
    public Boolean charlie;
    @Option(name="aByte",shortKey = "", longKey = "--byte", description = "byte test")
    public byte aByte;
    @Option(name="aShort",shortKey = "", longKey = "--short", description = "short test")
    public short aShort;
    @Option(name="aChar",shortKey = "", longKey = "--char", description = "char test")
    public char aChar;
    @Option(name="file",shortKey = "", longKey = "--file", description = "file test")
    public File file;
    @Option(name="path",shortKey = "", longKey = "--path", description = "path test")
    public Path path;
    @Option(name="object",shortKey = "", longKey = "--object", description = "object test -- this should fail, using to test a long description")
    public Object anObject;


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
