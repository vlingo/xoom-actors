package io.vlingo.actors;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvalidProtocolException extends IllegalStateException {
    private final String protocolName;
    private final List<Failure> failures;

    public InvalidProtocolException(String protocolName, List<Failure> failures) {
        super(toReadableMessage(protocolName, failures));

        this.protocolName = protocolName;
        this.failures = failures;
    }

    public static class Failure {
        public final String method;
        public final String cause;

        public Failure(String method, String cause) {
            this.method = method;
            this.cause = cause;
        }

        @Override
        public String toString() {
            return "In method `" + method.trim() + "`: " + "\n\t\t" + cause;
        }
    }

    private static String toReadableMessage(String protocolName, List<Failure> failures) {
        Stream<String> failureMessages = failures.stream().map(Failure::toString).map(msg -> "\t" + msg);

        return Stream.concat(Stream.of("For protocol " + protocolName), failureMessages)
                .collect(Collectors.joining("\n"));
    }

}
