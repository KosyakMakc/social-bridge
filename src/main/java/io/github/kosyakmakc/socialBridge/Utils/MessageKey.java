package io.github.kosyakmakc.socialBridge.Utils;

public record MessageKey(String key) {
    public static final MessageKey INTERNAL_SERVER_ERROR = new MessageKey("internal_server_error");
    public static final MessageKey INVALID_ARGUMENT = new MessageKey("invalid_argument");
    public static final MessageKey INVALID_ARGUMENT_ARE_EMPTY = new MessageKey("invalid_argument_are_empty");
    public static final MessageKey INVALID_ARGUMENT_NOT_A_BOOLEAN = new MessageKey("invalid_argument_not_a_boolean");
    public static final MessageKey INVALID_ARGUMENT_NOT_A_INTEGER = new MessageKey("invalid_argument_not_a_integer");
    public static final MessageKey INVALID_ARGUMENT_NOT_A_LONG = new MessageKey("invalid_argument_not_a_long");
    public static final MessageKey INVALID_ARGUMENT_NOT_A_FLOAT = new MessageKey("invalid_argument_not_a_float");
    public static final MessageKey INVALID_ARGUMENT_NOT_A_DOUBLE = new MessageKey("invalid_argument_not_a_double");
    
    public static final MessageKey EMPTY = new MessageKey("empty");
}
