// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.Collection;

import io.vlingo.actors.SupervisionStrategy.Scope;

public class StageSupervisedActor implements Supervised {
  private final Actor actor;
  private final Class<?> protocol;
  private final Throwable throwable;

  @Override
  public Address address() {
    return actor.address();
  }

  @Override
  public void escalate() {
    supervisor().supervisor().inform(throwable, this);
  }

  @Override
  public void restartWithin(final long period, final int intensity, final Scope scope) {
    if (failureThresholdReached(period, intensity)) {
      stop(scope);
    } else {
      if (scope == Scope.One) {
        restartWithin(actor, period, intensity);
      } else {
        for (final Actor actor : selfWithSiblings()) {
          restartWithin(actor, period, intensity);
        }
      }
    }
  }

  @Override
  public void resume() {
    actor.__internal__Resume();
  }

  @Override
  public void stop(final Scope scope) {
    if (scope == Scope.One) {
      actor.stop();
    } else {
      for (final Actor actor : selfWithSiblings()) {
        actor.stop();
      }
    }
  }

  @Override
  public void suspend() {
    actor.__internal__Suspend();
  }

  @Override
  public Supervisor supervisor() {
    return actor.__internal_Supervisor(protocol);
  }

  @Override
  public Throwable throwable() {
    return throwable;
  }

  protected StageSupervisedActor(final Class<?> protocol, final Actor actor, final Throwable throwable) {
    this.protocol = protocol;
    this.actor = actor;
    this.throwable = throwable;
  }

  private Collection<Actor> selfWithSiblings() {
    return environmentOf(environmentOf(actor).parent).children;
  }

  private Environment environmentOf(final Actor actor) {
    return actor.__internal__Environment();
  }

  private boolean failureThresholdReached(final long period, final int intensity) {
    return environmentOf(actor).failureMark.failedWithExcessiveFailures(period, intensity);
  }

  private void restartWithin(final Actor actor, final long period, final int intensity) {
    actor.__internal__BeforeRestart(throwable, protocol);
    // TODO: Actually restart actor here? I am not
    // yet convinced that it is necessary or practical.
    // Please convince me.
    actor.__internal__AfterRestart(throwable, protocol);
    resume();
  }
}
