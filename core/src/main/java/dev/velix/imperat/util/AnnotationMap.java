package dev.velix.imperat.util;

import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.methods.*;
import dev.velix.imperat.annotations.types.parameters.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Set;

public final class AnnotationMap
		  extends HashMap<Class<? extends Annotation>, Annotation> {


	static final Set<Class<? extends Annotation>> COMMAND_ANNOTATIONS =
			  Set.of(
						 Command.class, Description.class, Usage.class, SubCommand.class,
					    DefaultUsage.class, Permission.class, Named.class, Optional.class,
					    Flag.class, Greedy.class, Range.class, DefaultValue.class, Help.class, Cooldown.class
			  );

	public static AnnotationMap loadFrom(AnnotatedElement element) {
		AnnotationMap map = new AnnotationMap();
		for(var type : COMMAND_ANNOTATIONS) {
			if(element.isAnnotationPresent(type)) {
				map.put(type, element.getAnnotation(type));
			}
		}
		return map;
	}

}
