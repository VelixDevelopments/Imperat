package dev.velix.imperat.commands;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.commands.annotations.examples.Group;
import java.util.LinkedList;
import java.util.Map;

@Command("test2")
public class Test2Command {

    @Usage
    public void def(TestSource source) {
        source.reply("Enter an input");
        // /test2
    }

    @SubCommand("group")
    public void def(TestSource source, @Named("group") Group group) {
        source.reply("group=" + group.name());
    }

    @SubCommand("array")
    public void def(TestSource source, @Named("myArray") Group[] array) {
        source.reply("SIZE= " + array.length);
        for(var entry : array) {
            source.reply("-> " + entry.name());
        }
        // /test2 array hi hello how are you
    }

    @SubCommand("collection")
    public void def(TestSource source, @Named("myCollection") LinkedList<String> collection) {
        source.reply("SIZE= " + collection.size());
        for(var entry : collection) {
            source.reply("-> " + entry);
        }
    }

    @SubCommand("map")
    public void def(TestSource source, @Named("myMap") Map<String, String> map) {
        source.reply("SIZE= " + map.size());
        for(var entry : map.entrySet()) {
            source.reply("-> " + entry.getKey() + ":" + entry.getValue());
        }
    }

}
