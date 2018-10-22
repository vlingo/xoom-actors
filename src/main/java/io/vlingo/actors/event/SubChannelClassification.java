package io.vlingo.actors.event;

import io.vlingo.common.Changes;
import io.vlingo.common.SubClassification;
import io.vlingo.common.SubClassifiedIndex;

public abstract class SubChannelClassification<E, S, C> implements EventBus<E, S, C> {

    private final SubClassifiedIndex<C, S> subscriptions;

    protected abstract C classify(final E event);

    protected abstract void publish(final E event, final S subscriber);

    public SubChannelClassification(final SubClassification<C> subClassification) {
        this.subscriptions = new SubClassifiedIndex<>(subClassification);
    }

    @Override
    public boolean subscribe(final S subscriber, final C classifier) {

        final Changes<C, S> changes = subscriptions.addValue(classifier, subscriber);

        return changes.hasAny();
    }

    @Override
    public boolean unsubscribe(final S subscriber, final C classifier) {

        final Changes<C, S> changes = subscriptions.removeValue(classifier, subscriber);

        return changes.hasAny();
    }

}