package dev.velix.imperat.annotations;

import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.ElementKey;
import dev.velix.imperat.annotations.element.ElementVisitor;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.AnnotatedElement;

@ApiStatus.Internal
public final class AnnotationContainer extends Registry<ElementKey, AnnotatedElement> {
    
    private final AnnotationLevel level;
    
    AnnotationContainer(AnnotationLevel level) {
        this.level = level;
    }
    
    public void addElement(
            ElementKey key,
            AnnotatedElement element
    ) {
        if (!level.matches(element)) {
            throw new IllegalArgumentException("Failed to add element '"
                    + element.getClass().getName() + "' to level=" + level.name());
        }
        this.setData(key, element);
    }
    
    public void removeElement(ElementKey key) {
        this.setData(key, null);
    }
    
    public <T extends AnnotatedElement> void accept(
            ElementVisitor<T> elementVisitor,
            CommandAnnotatedElement<T> element
    ) {
        elementVisitor.visit(this, element);
    }
    
}
