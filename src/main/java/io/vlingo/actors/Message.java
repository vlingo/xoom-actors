// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.common.SerializableConsumer;

public interface Message {
  Actor actor();
  void deliver();
  Class<?> protocol();
  String representation();
  boolean isStowed();
  void set(final Actor actor, final Class<?> protocol, final SerializableConsumer<?> consumer, final Returns<?> returns, final String representation);
}
