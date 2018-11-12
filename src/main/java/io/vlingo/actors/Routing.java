// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 * Routing is an ordered sequence of {@link Routee routees} that
 * was computed by a {@link RoutingStrategy} and whose elements
 * will be the target of a message forwarded by a {@link Router}.
 */
public class Routing {
  
  public static Routing empty() {
    return new Routing();
  }

  public static Routing with(final Routee routee) {
    return new Routing(Arrays.asList(routee));
  }
  
  public static Routing with(final Optional<Routee> routeeOrNull) {
    return routeeOrNull.isPresent()
            ? Routing.with(routeeOrNull.get())
            : Routing.empty();
  }
  
  public static Routing with(final List<Routee> routees) {
    return new Routing(routees);
  }
  
  private final List<Routee> routees;
  
  Routing() {
    super();
    this.routees = new ArrayList<>();
  }
  
  Routing(final List<Routee> routees) {
    super();
    this.routees = routees;
  }
    
  public List<Routee> routees() {
    return Collections.unmodifiableList(routees);
  }
  
  public <T> List<T> routeesAs(final Class<T> protocol) {
    return routees.stream()
            .map(routee -> routee.as(protocol))
            .collect(Collectors.toList());
  }

  public boolean isEmpty() {
    return routees.isEmpty();
  }

  @Override
  public String toString() {
    return "Routing[routees=" + routees + "]";
  }

  public void validate() {
    if (routees.isEmpty())
      throw new IllegalStateException("routees may not be empty");
  }
}
