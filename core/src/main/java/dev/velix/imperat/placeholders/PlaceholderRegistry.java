package dev.velix.imperat.placeholders;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.Registry;

public final class PlaceholderRegistry<S extends Source> extends Registry<String, Placeholder<S>> {

    private final Imperat<S> imperat;

    PlaceholderRegistry(Imperat<S> imperat) {
        this.imperat = imperat;
    }

    public static <S extends Source> PlaceholderRegistry<S> createDefault(Imperat<S> imperat) {
        return new PlaceholderRegistry<>(imperat);
    }

    public String resolvedString(String input) {

        String result = input;
        for (var placeHolder : getAll()) {

            if (placeHolder.isUsedIn(result)) {
                System.out.println("FOUNd" + placeHolder.id());
                String id = placeHolder.id();
                result = placeHolder.replaceResolved(imperat, id, result);
            }

        }
        return result;
    }

    public String[] resolvedArray(String[] array) {
        String[] arr = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            arr[i] = resolvedString(array[i]);
        }
        return arr;
    }


}
