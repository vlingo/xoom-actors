// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import io.vlingo.common.Completes;
import io.vlingo.common.PentaConsumer;
import io.vlingo.common.PentaFunction;
import io.vlingo.common.QuadConsumer;
import io.vlingo.common.QuadFunction;
import io.vlingo.common.TriConsumer;
import io.vlingo.common.TriFunction;
/**
 * Routee represents a potential target for for a routed message.
 */
public class Routee<P> {

  private final Addressable addressable;
  private P delegate;
  private long messageCount;

  static <T> Routee<T> of(final T actor, final Addressable addressable) {
    return new Routee<T>(actor, addressable);
  }

  static <T> Routee<T> of(final T actor) {
    return new Routee<T>(actor, null);
  }

  Routee(final P actor, final Addressable addressable) {
    super();
    this.delegate = actor;
    this.addressable = addressable;
    this.messageCount = 0;
  }

  public P delegate() {
    return delegate;
  }

  public LifeCycle delegateLifeCycle() {
    return addressable.lifeCycle();
  }

  public Address address() {
    return addressable.address();
  }

  public int pendingMessages() {
    return delegateLifeCycle().environment.mailbox.pendingMessages();
  }

  public long messageCount() {
    return messageCount;
  }

  protected <T1> void receiveCommand(final BiConsumer<P, T1> consumer, final T1 routable1) {
    messageCount++;
    consumer.accept(delegate, routable1);
  }

  protected <T1, T2> void receiveCommand(final TriConsumer<P, T1, T2> consumer, final T1 routable1, final T2 routable2) {
    messageCount++;
    consumer.accept(delegate, routable1, routable2);
  }

  protected <T1, T2, T3> void receiveCommand(final QuadConsumer<P, T1, T2, T3> consumer, final T1 routable1, final T2 routable2, final T3 routable3) {
    messageCount++;
    consumer.accept(delegate, routable1, routable2, routable3);
  }

  protected <T1, T2, T3, T4> void receiveCommand(final PentaConsumer<P, T1, T2, T3, T4> consumer, final T1 routable1, final T2 routable2, final T3 routable3, final T4 routable4) {
    messageCount++;
    consumer.accept(delegate, routable1, routable2, routable3, routable4);
  }

  public <T1, R extends Completes<?>> R receiveQuery(final BiFunction<P, T1, R> query, final T1 routable1) {
    messageCount++;
    return query.apply(delegate, routable1);
  }

  public <T1, T2, R extends Completes<?>> R receiveQuery(final TriFunction<P, T1, T2, R> query, final T1 routable1, final T2 routable2) {
    messageCount++;
    return query.apply(delegate, routable1, routable2);
  }

  public <T1, T2, T3, R extends Completes<?>> R receiveQuery(final QuadFunction<P, T1, T2, T3, R> query, final T1 routable1, final T2 routable2, final T3 routable3) {
    messageCount++;
    return query.apply(delegate, routable1, routable2, routable3);
  }

  public <T1, T2, T3, T4, R extends Completes<?>> R receiveQuery(final PentaFunction<P, T1, T2, T3, T4, R> query, final T1 routable1, final T2 routable2, final T3 routable3, final T4 routable4) {
    messageCount++;
    return query.apply(delegate, routable1, routable2, routable3, routable4);
  }

  /* @see java.lang.Object#hashCode() */
  @Override
  public int hashCode() {
    return (delegate == null) ? 0 : delegate.hashCode();
  }

  /* @see java.lang.Object#equals(java.lang.Object) */
  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Routee other = (Routee) obj;
    if (delegate == null) {
      if (other.delegate != null)
        return false;
    } else if (!delegate.equals(other.delegate))
      return false;
    return true;
  }

  /* @see java.lang.Object#toString() */
  @Override
  public String toString() {
    return new StringBuilder()
      .append("Routee(")
      .append("actor=").append(delegate)
      .append(")")
      .toString();
  }
}
