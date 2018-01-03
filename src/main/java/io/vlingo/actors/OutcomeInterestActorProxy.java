// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

class OutcomeInterestActorProxy<O, R> implements OutcomeInterest<O> {
  private final OutcomeAware<O, R> outcomeAware;
  private final R reference;
  
  OutcomeInterestActorProxy(final OutcomeAware<O, R> outcomeAware, final R reference) {
    this.outcomeAware = outcomeAware;
    this.reference = reference;
  }

  @Override
  public void failureOutcome(final Outcome<O> outcome) {
    outcomeAware.failureOutcome(outcome, reference);
  }

  @Override
  public void successfulOutcome(final Outcome<O> outcome) {
    outcomeAware.successfulOutcome(outcome, reference);
  }
}
