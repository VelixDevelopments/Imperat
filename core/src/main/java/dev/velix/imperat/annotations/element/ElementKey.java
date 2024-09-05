package dev.velix.imperat.annotations.element;

import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Objects;

@ApiStatus.Internal
public record ElementKey(String id, String... signatureParamTypes) {
    public static ElementKey of(String id) {
        return new ElementKey(id);
    }

    @Override
    public String toString() {
        return "ElementKey{" +
                "id='" + id + '\'' +
                ", signatureParams='" + String.join("->", signatureParamTypes) + "'";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ElementKey key = (ElementKey) object;
        return Objects.equals(id, key.id) && (Objects.deepEquals(signatureParamTypes, key.signatureParamTypes));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, Arrays.hashCode(signatureParamTypes));
    }
}