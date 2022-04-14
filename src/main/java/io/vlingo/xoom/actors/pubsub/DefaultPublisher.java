// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.pubsub;

public class DefaultPublisher implements Publisher {

    private final Subscriptions subscriptions = new Subscriptions();

    @Override
    public void publish(final Topic topic, final Message message) {
        subscriptions.forTopic(topic).forEach(subscriber -> subscriber.receive(message));
    }

    @Override
    public boolean subscribe(final Topic topic, final Subscriber<?> subscriber) {
        final AffectedSubscriptions affectedSubscriptions = subscriptions.create(topic, subscriber);
        return affectedSubscriptions.hasAny();
    }

    @Override
    public boolean unsubscribe(final Topic topic, final Subscriber<?> subscriber) {
        final AffectedSubscriptions affectedSubscriptions = subscriptions.cancel(topic, subscriber);
        return affectedSubscriptions.hasAny();
    }

    @Override
    public void unsubscribeAllTopics(final Subscriber<?> subscriber) {
        subscriptions.cancelAll(subscriber);
    }
}