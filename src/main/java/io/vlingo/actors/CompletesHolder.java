// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public class CompletesHolder implements Completes<Object> {
  public final Completes<Object> clientCompletes;
  public final CompletesEventually completesEventually;
  public final long id;
  private Object outcome;

  public CompletesHolder(final long id, final Completes<Object> clientCompletes, final CompletesEventually completesEventually) {
    this.id = id;
    this.clientCompletes = clientCompletes;
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
}
