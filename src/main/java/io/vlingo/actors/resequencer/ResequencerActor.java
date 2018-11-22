// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.resequencer;

import io.vlingo.actors.Actor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResequencerActor extends Actor implements Resequencer {

    private final ResequencedMessageConsumer resequencedMessageConsumer;
    private final Map<String, ResequencedMessages> messageHolder = new HashMap<>();

    public ResequencerActor(final ResequencedMessageConsumer resequencedMessageConsumer) {
        this.resequencedMessageConsumer = resequencedMessageConsumer;
    }

    @Override
    public void resequence(final SequencedMessage message) {
        hold(message);
        dispatchAllSequenced(message.correlationId());
        removeCompleted(message.correlationId());
    }

    private void hold(final SequencedMessage message) {

        final String correlationId = message.correlationId();

        if(!messageHolder.containsKey(correlationId)) {
            messageHolder.put(correlationId, ResequencedMessages.withNonIndexedMessages(correlationId, message.total()));
        }

        messageHolder.get(correlationId)
                .sequencedMessages()
                .add(message.index() - 1, message);
    }

    private void dispatchAllSequenced(final String correlationId) {
        final ResequencedMessages resequencedMessages = messageHolder.get(correlationId);
        int dispatchableIndex = resequencedMessages.dispatchableIndex();

        for(final SequencedMessage sequencedMessage : resequencedMessages.onlyIndexed()) {
            if(sequencedMessage.index() == dispatchableIndex) {
                dispatchableIndex++;
                resequencedMessageConsumer.receive(sequencedMessage);
            }
        }

        messageHolder.replace(correlationId, resequencedMessages.advancedTo(dispatchableIndex));
    }

    private void removeCompleted(final String correlationId) {
        if(messageHolder.get(correlationId).isCompleted()) {
            messageHolder.remove(correlationId);
        }
    }
}
