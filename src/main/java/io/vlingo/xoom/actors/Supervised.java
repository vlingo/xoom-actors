// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import io.vlingo.xoom.actors.SupervisionStrategy.Scope;

public interface Supervised {
  Address address();
  void escalate();
  void restartWithin(final long period, final int intensity, final Scope scope);
  void resume();
  void stop(final Scope scope);
  Supervisor supervisor();
  void suspend();
  Throwable throwable();
}
