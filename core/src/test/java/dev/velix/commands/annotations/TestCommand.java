package dev.velix.commands.annotations;

import dev.velix.TestRun;
import dev.velix.TestSource;
import dev.velix.annotations.*;
import dev.velix.help.CommandHelp;

@Command("test")
@Inherit(FirstSub.class)
public class TestCommand {
    
    @Usage
    public void defaultExec(TestSource source) {
        source.reply("Default execution of test(root) command");
    }
    
    @Usage
    public void cmdUsage(TestSource source, @Named("otherText") @Suggest({"hi", "bye"}) String otherText) {
        source.reply("Executing usage in test's main usage, num= " + otherText);
    }
    
    @SubCommand("othersub")
    public void doOtherSub(TestSource source, @Named("text") @Suggest({"hi", "bye"}) String text) {
        source.reply("Other-text= " + text);
    }
    
    @SubCommand(value = "help", attachDirectly = true)
    public void help(TestSource source, CommandHelp help) {
        help.display(source);
        source.reply("executed /test help");
        TestRun.USAGE_EXECUTED = true;
    }
    
    @Command("embedded")
    public void embeddedCmd(TestSource source, @Named("value") String arg) {
        source.reply("Embedded command value=" + arg);
    }
    
    
    @SubCommand("sub1")
    public static class Sub1 {
        @Usage
        public void defaultUsage(TestSource source) {
            source.reply("default sub1");
        }
        
        @Usage
        public void sub1Main(TestSource source, @Named("a") String a) {
            source.reply("sub1-main a=" + a);
        }
        
        @SubCommand("sub2")
        public static class Sub2 {
            
            
            @Usage
            public void defaultUsage(TestSource source) {
                source.reply("default sub2");
            }
            
            @Usage
            public void sub2Main(TestSource source, @Named("b") String b) {
                source.reply("sub2-main b=" + b);
            }
            
            @SubCommand("sub3")
            public static class Sub3 {
                
                @Usage
                public void defaultUsage(TestSource source) {
                    source.reply("default sub3");
                }
                
                @Usage
                public void sub3Main(TestSource source, @Named("c") String c) {
                    source.reply("sub3 c=" + c);
                }
                
            }
            
        }
    }
    
    
    @SubCommand("sub4")
    public static class Sub4 {
        
        @Usage
        public void defaultUsage(TestSource source) {
            source.reply("default sub4");
        }
        
        @Usage
        public void sub4Main(TestSource source, @Named("othertext") String otherText, @Named("a") String a) {
            source.reply("sub4 a=" + a);
        }
        
        @SubCommand("sub5")
        public static class Sub5 {
            
            @Usage
            public void defaultUsage(TestSource source) {
                source.reply("default sub5");
            }
            
            @Usage
            public void sub5Main(TestSource source, @Named("othertext") String otherText, @Named("a") String a, @Named("b") String b) {
                source.reply("sub4 a= " + a + ", sub5 b=" + b);
            }
            
            @SubCommand("sub6")
            public static class Sub6 {
                @Usage
                public void defaultUsage(TestSource source) {
                    source.reply("default sub6");
                }
                
                @Usage
                public void sub6Main(TestSource source, @Named("othertext") String otherText, @Named("a") String a, @Named("b") String b, @Named("c") String c) {
                    source.reply("sub4 a= " + a + ", sub5b= " + b + ", sub6 c=" + c);
                }
                
            }
            
        }
        
    }
    
}
