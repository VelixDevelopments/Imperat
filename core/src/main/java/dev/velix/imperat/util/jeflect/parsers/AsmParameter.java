package dev.velix.imperat.util.jeflect.parsers;


import dev.velix.imperat.util.jeflect.ByteAnnotation;
import dev.velix.imperat.util.jeflect.ByteParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class AsmParameter extends ByteParameter {
	final List<ByteAnnotation> annotations;

	AsmParameter(String descriptor, int modifiers) {
		super(descriptor, modifiers);
		this.annotations = new ArrayList<>();
	}

	@Override
	public List<ByteAnnotation> getAnnotations() {
		return Collections.unmodifiableList(annotations);
	}
}
