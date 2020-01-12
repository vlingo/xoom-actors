// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.pubsub;

public interface Publisher {

    void publish(final Topic topic, final Message message);

    boolean subscribe(final Topic topic, final Subscriber<?> subscriber);

    boolean unsubscribe(final Topic topic, final Subscriber<?> subscriber);

    void unsubscribeAllTopics(final Subscriber<?> subscriber);

}