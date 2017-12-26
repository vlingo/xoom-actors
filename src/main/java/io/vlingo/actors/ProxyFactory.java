// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public interface ProxyFactory {
  <T> T createFor(final Class<T> protocol, final Actor actor, final Mailbox mailbox);
  Object createFor(final Class<?>[] protocol, final Actor actor, final Mailbox mailbox);
}
