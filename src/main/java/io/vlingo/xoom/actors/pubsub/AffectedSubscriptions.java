// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.pubsub;

import java.util.HashMap;
import java.util.Map;

public class AffectedSubscriptions {

    private final Map<Topic, Subscriber<?>> registry = new HashMap<>();

    public void add(final Topic topic, final Subscriber<?> subscriber) {
        registry.put(topic, subscriber);
    }

    public boolean hasAny() {
        return !registry.isEmpty();
    }
}