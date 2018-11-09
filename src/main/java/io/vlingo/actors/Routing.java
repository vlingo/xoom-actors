/* Copyright (c) 2005-2018 - Blue River Systems Group, LLC - All Rights Reserved */
package io.vlingo.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 * Routing
 *
 * @author davem
 * @since Oct 28, 2018
 */
public class Routing {
  
  public static Routing empty() {
    return new Routing();
  }

  public static Routing with(Routee routee) {
    return new Routing(Arrays.asList(routee));
  }
  
  public static Routing with(Optional<Routee> routeeOrNull) {
    return routeeOrNull.isPresent()
            ? Routing.with(routeeOrNull.get())
            : Routing.empty();
  }
  
  public static Routing with(List<Routee> routees) {
    return new Routing(routees);
  }
  
  private final List<Routee> routees;
  
  protected Routing() {
    super();
    this.routees = new ArrayList<>();
  }
  
  protected Routing(List<Routee> routees) {
    super();
    this.routees = routees;
  }
    
  public List<Routee> routees() {
    return Collections.unmodifiableList(routees);
  }
  
  public <T> List<T> routeesAs(Class<T> protocol) {
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
}
