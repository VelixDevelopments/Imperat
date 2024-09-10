package dev.velix.imperat.commands.annotations;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.types.*;

@Command("test")
@Inherit(FirstSub.class)
public class TestCommand {
    //TODO test command methods
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
    
    @Command("embedded")
    public void embeddedCmd(TestSource source, @Named("value") String arg) {
        source.reply("Embedded command value=" + arg);
    }
    
    
    @SubCommand("sub1")
    public static class Sub1 {
        @Usage
        public void defaultUsage(TestSource source) {
            source.reply("Default exec for Inner");
        }
        
        @Usage
        public void sub1Main(TestSource source, @Named("a") String a) {
            source.reply("a=" + a);
        }
        
        @SubCommand("sub2")
        public static class Sub2 {
            
            
            @Usage
            public void defaultUsage(TestSource source) {
                source.reply("Default exec for Inner");
            }
            
            @Usage
            public void sub2Main(TestSource source, @Named("b") String b) {
                source.reply("a=" + b);
            }
            
            @SubCommand("sub3")
            public static class Sub3 {
                
                @Usage
                public void defaultUsage(TestSource source) {
                    source.reply("Default exec for Inner");
                }
                
                @Usage
                public void sub3Main(TestSource source, @Named("c") String c) {
                    source.reply("a=" + c);
                }
                
            }
            
        }
    }
    
    
    @SubCommand("sub4")
    public static class Sub4 {
        
        @Usage
        public void defaultUsage(TestSource source) {
            source.reply("Default exec for Inner");
        }
        
        @Usage
        public void sub4Main(TestSource source, @Named("a") String a) {
            source.reply("a=" + a);
        }
        
        @SubCommand("sub5")
        public static class Sub5 {
            
            @Usage
            public void defaultUsage(TestSource source) {
                source.reply("Default exec for Inner");
            }
            
            @Usage
            public void sub5Main(TestSource source, @Named("b") String b) {
                source.reply("a=" + b);
            }
            
            @SubCommand("sub6")
            public static class Sub6 {
                @Usage
                public void defaultUsage(TestSource source) {
                    source.reply("Default exec for Inner");
                }
                
                @Usage
                public void sub6Main(TestSource source, @Named("c") String c) {
                    source.reply("c=" + c);
                }
                
            }
            
        }
        
    }
    
}
