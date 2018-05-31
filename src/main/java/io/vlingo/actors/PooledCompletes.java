// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public class PooledCompletes implements CompletesEventually {
  public final Completes<Object> clientCompletes;
  public final CompletesEventually completesEventually;
  public final long id;
  private Object outcome;

  @SuppressWarnings("unchecked")
  public PooledCompletes(
          final long id,
          final Completes<?> clientCompletes,
          final CompletesEventually completesEventually) {
    this.id = id;
    this.clientCompletes = (Completes<Object>) clientCompletes;
    this.completesEventually = completesEventually;
  }

  public Object outcome() {
    return outcome;
  }

  @Override
  public void with(final Object outcome) {
    this.outcome = outcome;
    completesEventually.with(this);
  }

  @Override
  public boolean isStopped() {
    return completesEventually.isStopped();
  }

  @Override
  public void stop() { }
}
