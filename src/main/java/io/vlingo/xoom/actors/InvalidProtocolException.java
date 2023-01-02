// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvalidProtocolException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    private final String protocolName;
    private final List<Failure> failures;

    public InvalidProtocolException(String protocolName, List<Failure> failures) {
        super(toReadableMessage(protocolName, failures));

        this.protocolName = protocolName;
        this.failures = failures;
    }

    @Override
    public String toString() {
      return "Protocol '" + protocolName + "' " + " with failures " + failures;
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
