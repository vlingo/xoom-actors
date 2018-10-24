// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.event;

public interface EventBus<E, S, C> {

    boolean subscribe(final S subscriber, final C to);

    boolean unsubscribe(final S subscriber, final C from);

    void unsubscribe(final S subscriber);

    void publish(final E event);

}