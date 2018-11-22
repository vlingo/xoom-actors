package io.vlingo.actors.resequencer;

public interface ResequencedMessageConsumer {

    void receive(final SequencedMessage message);
}
