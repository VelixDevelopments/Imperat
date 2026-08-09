package studio.mevera.imperat.responses;

import studio.mevera.imperat.util.Keyed;

public interface ResponseKey extends Keyed<String> {

    // Parse exceptions
    ResponseKey INVALID_BOOLEAN = () -> "args.parsing.invalid-boolean";
    ResponseKey INVALID_ENUM = () -> "args.parsing.invalid-enum";
    ResponseKey INVALID_NUMBER_FORMAT = () -> "args.parsing.invalid-number-format";
    ResponseKey INVALID_CHARACTER = () -> "args.parsing.invalid-character";
    ResponseKey INVALID_MAP_ENTRY_FORMAT = () -> "args.parsing.invalid-map-entry-format";
    ResponseKey INVALID_UUID = () -> "args.parsing.invalid-uuid";
    ResponseKey VALUE_OUT_OF_CONSTRAINT = () -> "args.parsing.value-out-of-constraint";
    ResponseKey INVALID_DECIMAL = () -> "args.parsing.invalid-decimal";
    ResponseKey INVALID_INTEGER = () -> "args.parsing.invalid-integer";
    ResponseKey INVALID_LITERAL = () -> "args.parsing.invalid-literal";
    ResponseKey INVALID_DURATION = () -> "args.parsing.invalid-duration";
    ResponseKey INVALID_INSTANT = () -> "args.parsing.invalid-instant";
    ResponseKey INVALID_DATE = () -> "args.parsing.invalid-date";
    ResponseKey INVALID_DATE_TIME = () -> "args.parsing.invalid-date-time";
    ResponseKey INVALID_PATH = () -> "args.parsing.invalid-path";
    ResponseKey INVALID_REGEX = () -> "args.parsing.invalid-regex";
    ResponseKey INVALID_URI = () -> "args.parsing.invalid-uri";
    ResponseKey INVALID_INPUT_NATIVE = () -> "args.parsing.invalid-input-native";

    // Flag-related exceptions
    ResponseKey UNKNOWN_FLAG = () -> "flag.unknown";
    ResponseKey MISSING_FLAG_INPUT = () -> "flag.missing-input";
    ResponseKey FLAG_OUTSIDE_SCOPE = () -> "flag.outside-scope";

    // Validation exceptions
    ResponseKey NUMBER_OUT_OF_RANGE = () -> "args.validation.number-out-of-range";

    // Command exceptions
    ResponseKey COOLDOWN = () -> "command.cooldown";

    // Help exceptions
    ResponseKey NO_HELP = () -> "help.not-available";
    ResponseKey NO_HELP_PAGE = () -> "help.page-not-found";

}
