package dev.velix.command.parameters;

import java.util.Collection;
import java.util.LinkedList;

public final class StrictParameterList extends LinkedList<CommandParameter> {
    
    @Override
    public void addFirst(CommandParameter parameter) {
        if (containsSimilar(parameter))
            return;
        
        super.addFirst(parameter);
    }
    
    @Override
    public boolean add(CommandParameter parameter) {
        
        if (containsSimilar(parameter))
            return false;
        
        return super.add(parameter);
    }
    
    @Override
    public boolean addAll(Collection<? extends CommandParameter> c) {
        for (var e : c) {
            add(e);
        }
        return true;
    }
    
    
    @Override
    public boolean contains(Object o) {
        if (!(o instanceof CommandParameter parameter)) return false;
        return super.contains(parameter) || containsSimilar(parameter);
    }
    
    public boolean containsSimilar(CommandParameter parameter) {
        for (var p : this) {
            if (p.similarTo(parameter))
                return true;
        }
        return false;
    }
}
