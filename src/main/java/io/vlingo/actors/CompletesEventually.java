// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public interface CompletesEventually extends Stoppable {
  static ActorInstantiator<CompletesEventuallyActor> Instantiator = new CompletesEventuallyInstantiator();

  Address address();
  void with(final Object outcome);
  @Override
  default void conclude() { }
  @Override
  default boolean isStopped() { return false; }
  @Override
  default void stop() { }

  static class CompletesEventuallyInstantiator implements ActorInstantiator<CompletesEventuallyActor> {
    @Override
    public CompletesEventuallyActor instantiate() {
      return new CompletesEventuallyActor();
    }

    @Override
    public Class<CompletesEventuallyActor> type() {
      return CompletesEventuallyActor.class;
    }
  }
}
