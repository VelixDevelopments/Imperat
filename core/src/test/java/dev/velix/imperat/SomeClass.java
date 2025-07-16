package dev.velix.imperat;

import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Dependency;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.components.TestSource;
import org.junit.jupiter.api.Assertions;

@Command("root")
public class SomeClass {
    
    @Dependency
    private SomeInstance dependency;
    
    @SubCommand("i1")
    public class InnerOne {
        
        @Usage
        public void def(TestSource source) {
            Assertions.assertEquals("Hello-world", dependency.toString());
        }
        
        @SubCommand("i1.1")
        public class InnerOne2 {
            
            @Usage
            public void def(TestSource source) {
                Assertions.assertEquals("Hello-world", dependency.toString());
            }
            
            @SubCommand("i1.1.1")
            public class InnerOne3 {
                
                @Usage
                public void def(TestSource source) {
                    Assertions.assertEquals("Hello-world", dependency.toString());
                }
                
            }
            
        }
        
        
    }
    
    @SubCommand("i2")
    public class InnerTwo {
        @Usage
        public void def(TestSource source) {
            Assertions.assertEquals("Hello-world", dependency.toString());
        }
        
        @SubCommand("i2.1")
        public class InnerTwo2 {
            
            @Usage
            public void def(TestSource source) {
                Assertions.assertEquals("Hello-world", dependency.toString());
            }
            
            @SubCommand("i2.2")
            public class InnerTwo3 {
                
                @Usage
                public void def(TestSource source) {
                    Assertions.assertEquals("Hello-world", dependency.toString());
                }
                
            }
            
        }
        
    }
    
}
