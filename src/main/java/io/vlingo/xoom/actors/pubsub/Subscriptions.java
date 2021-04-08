// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.pubsub;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Subscriptions {

    private Map<Topic, Set<Subscriber<?>>> index = new HashMap<>();

    public AffectedSubscriptions create(final Topic topic, final Subscriber<?> subscriber) {

        if(!index.containsKey(topic)) {
            index.put(topic, new HashSet<>());
        }

        return performOperation(topic, subscriber, defaultCondition(), insertOperation());
    }

    public AffectedSubscriptions cancel(final Topic topic, final Subscriber<?> subscriber) {
        return performOperation(topic, subscriber, defaultCondition(), removalOperation());
    }

    public AffectedSubscriptions cancelAll(final Subscriber<?> subscriber) {
        return performOperation(null, subscriber, noCondition(), removalOperation());
    }

    public Set<Subscriber<?>> forTopic(final Topic topic) {

        final Set<Subscriber<?>> subscribers = new HashSet<>();

        index.entrySet().forEach(subscription -> {

            final Topic subscribedTopic = subscription.getKey();

            if(subscribedTopic.equals(topic) || subscribedTopic.isSubTopic(topic)) {
                subscribers.addAll(subscription.getValue());
            }
        });

        return subscribers;
    }

    private Operation insertOperation() {
        return (existingValues, givenValue) -> existingValues.add(givenValue);
    }

    private Operation removalOperation() {
        return (existingValues, givenValue) -> existingValues.remove(givenValue);
    }

    private Condition defaultCondition() {
        return (subscription, topic, subscriber) -> subscription.getKey().equals(topic);
    }

    private Condition noCondition() {
        return (subscription, topic, subscriber) -> true;
    }

    private AffectedSubscriptions performOperation(final Topic topic, final Subscriber<?> subscriber, final Condition condition, final Operation operation) {

        final AffectedSubscriptions affectedSubscriptions = new AffectedSubscriptions();

        index.entrySet().forEach(subscription -> {
            if(condition.should(subscription, topic, subscriber) &&
                    operation.perform(subscription.getValue(), subscriber)) {
                affectedSubscriptions.add(topic, subscriber);
            }
        });

        return affectedSubscriptions;
    }

    @FunctionalInterface
    private interface Operation {
        boolean perform(final Set<Subscriber<?>> existingSubscriber, final Subscriber<?> givenSubscriber);
    }

    @FunctionalInterface
    private interface Condition {
        boolean should(final Entry<Topic, Set<Subscriber<?>>> subscription, final Topic topic, final Subscriber<?> subscriber);
    }
}
