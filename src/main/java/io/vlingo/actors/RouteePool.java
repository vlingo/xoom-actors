// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * RouteePool is responsible for maintaining an ordered,
 * resizeable list of {@link Routee}
 */
public class RouteePool<P> {
  
  private final List<Routee<P>> routees;

  public static <T> RouteePool<T> empty() {
    return new RouteePool<T>();
  }

  public static <T> RouteePool<T> with(Routee<T> routee) {
    return new RouteePool<T>().subscribe(routee);
  }

  public static <T> RouteePool<T> withAll(List<Routee<T>> routees) {
    return new RouteePool<T>().subscribe(routees);
  }
  
  RouteePool() {
    this.routees = new ArrayList<Routee<P>>();
  }
  
  public List<Routee<P>> routees() {
    return Collections.unmodifiableList(routees);
  }
  
  public Routee<P> routeeAt(int index) {
    return (index < 0 || index >= routees.size()) ? null : routees.get(index);
  }
  
  public int size() {
    return routees.size();
  }
  
  public RouteePool<P> subscribe(Routee<P> routee) {
    if (!routees.contains(routee))
      routees.add(routee);
    return this;
  }
  
  public RouteePool<P> subscribe(List<Routee<P>> toSubscribe) {
    toSubscribe.forEach(routee -> subscribe(routee));
    return this;
  }
  
  public RouteePool<P> unsubscribe(Routee<P> routee) {
    routees.remove(routee);
    return this;
  }
  
  public RouteePool<P> unsubscribe(List<Routee<P>> toUnsubscribe) {
    toUnsubscribe.forEach(routee -> unsubscribe(routee));
    return this;
  }
}
