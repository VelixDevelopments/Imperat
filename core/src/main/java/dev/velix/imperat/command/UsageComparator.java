package dev.velix.imperat.command;

import dev.velix.imperat.command.parameters.CommandParameter;

import java.util.Comparator;
import java.util.List;

final class UsageComparator implements Comparator<List<CommandParameter>> {
    
    private static UsageComparator instance;
    
    static UsageComparator getInstance() {
        if (instance == null) {
            instance = new UsageComparator();
            return instance;
        }
        return instance;
    }
    
    @Override
    public int compare(List<CommandParameter> firstUsage, List<CommandParameter> secondUsage) {
        
        if (firstUsage.size() == secondUsage.size()) {
            
            for (int i = 0; i < firstUsage.size(); i++) {
                CommandParameter p1 = firstUsage.get(i);
                CommandParameter p2 = secondUsage.get(i);
                if (p1 == null || p2 == null) break;
                if (p1.isCommand() && !p2.isCommand()) {
                    return -1;
                } else if (!p1.isCommand() && p2.isCommand()) {
                    return 1;
                }
            }
            
        }
        
        return firstUsage.size() - secondUsage.size();
    }
}
