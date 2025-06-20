package dev.velix.imperat.paramtypes;

import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Greedy;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.components.TestSource;

import java.util.concurrent.CompletableFuture;

@Command("testcf")
public class TestCompletableFutureParam {

    @Usage
    public void exec(TestSource source, @Named("text") @Greedy CompletableFuture<String> future) {
        future.whenComplete((txt, ex)-> {
            if(ex != null) {
                ex.printStackTrace();
                return;
            }
           source.reply("Text entered= '" + txt + "'" );
        });
    }

}
