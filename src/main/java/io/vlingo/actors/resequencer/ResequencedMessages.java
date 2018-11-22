// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.resequencer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ResequencedMessages {

    private final int dispatchableIndex;
    private final List<SequencedMessage> sequencedMessages;

    private static final int INITIAL_DISPATCHABLE_INDEX = 1;

    public static ResequencedMessages of(final int dispatchableIndex, final List<SequencedMessage> sequencedMessages) {
        return new ResequencedMessages(dispatchableIndex, sequencedMessages);
    }

    public static ResequencedMessages withNonIndexedMessages(final String correlationId, final int totalMessages) {
        return of(INITIAL_DISPATCHABLE_INDEX, generateNonIndexedMessages(correlationId, totalMessages));
    }

    private static List<SequencedMessage> generateNonIndexedMessages(final String correlationId, final int totalMessages) {
        return IntStream.range(0, totalMessages)
                .mapToObj(x -> SequencedMessage.asNonIndexedMessage(correlationId, totalMessages))
                .collect(Collectors.toList());
    }

    private ResequencedMessages(final int dispatchableIndex, final List<SequencedMessage> sequencedMessages) {
        this.dispatchableIndex = dispatchableIndex;
        this.sequencedMessages = new ArrayList<>();
        this.sequencedMessages.addAll(sequencedMessages);
    }

    public ResequencedMessages advancedTo(final int dispatchableIndex) {
        return of(dispatchableIndex, sequencedMessages);
    }

    public List<SequencedMessage> sequencedMessages() {
        return Collections.unmodifiableList(sequencedMessages);
    }

    public int dispatchableIndex() {
        return dispatchableIndex;
    }

    public boolean isCompleted() {
        return dispatchableIndex > sequencedMessages.stream().findFirst().get().total();
    }
}