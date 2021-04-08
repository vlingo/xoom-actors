// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
/**
 * Routing is an ordered sequence of {@link Routee routees} that
 * was computed by way of some routing strategy and whose elements
 * will be the target of a message forwarded by a {@link Router}.
 */
public class Routing<P> {
  
  public static <T> Routing<T> with(final Routee<T> routee) {
    if (routee == null)
      throw new IllegalArgumentException("routee may not be null");
    return new Routing<T>(Arrays.asList(routee));
  }
  
  public static <T> Routing<T> with(final List<Routee<T>> routees) {
    if (routees == null || routees.isEmpty())
      throw new IllegalArgumentException("routees may not be null or empty");
    return new Routing<T>(routees);
  }
  
  public static <T> Routing<T> with(final Set<Routee<T>> routees) {
    if (routees == null || routees.isEmpty())
      throw new IllegalArgumentException("routees may not be null or empty");
    return new Routing<T>(routees);
  }
  
  private final List<Routee<P>> routees;
  
  Routing() {
    super();
    this.routees = new ArrayList<>();
  }
  
  Routing(final List<Routee<P>> routees) {
    super();
    this.routees = routees;
  }
  
  Routing(final Set<Routee<P>> routees) {
    this(new ArrayList<>(routees));
  }
  
  public Routee<P> first() {
    return routees.get(0);
  }
    
  public List<Routee<P>> routees() {
    return Collections.unmodifiableList(routees);
  }

  public boolean isEmpty() {
    return routees.isEmpty();
  }

  @Override
  public String toString() {
    return "Routing[routees=" + Arrays.toString(routees.toArray()) + "]";
  }

  public void validate() {
    if (routees.isEmpty())
      throw new IllegalStateException("routees may not be empty");
  }
}