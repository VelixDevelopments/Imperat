package dev.velix.imperat.util.jeflect;

import java.util.Objects;

abstract class AbstractMember extends AbstractAnnotated implements ByteMember {
    private final ByteClass parent;
    private final String name;
    private final int modifiers;

    protected AbstractMember(ByteClass parent, String name, int modifiers) {
        this.parent = parent;
        this.name = name;
        this.modifiers = modifiers;
    }

    @Override
    public ByteClass getDeclaringClass() {
        return parent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractMember)) return false;
        var that = (AbstractMember) o;
        return modifiers == that.modifiers && Objects.equals(parent, that.parent) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, name, modifiers);
    }

    @Override
    public String toString() {
        return name;
    }
}
