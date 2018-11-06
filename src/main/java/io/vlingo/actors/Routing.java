/* Copyright (c) 2005-2018 - Blue River Systems Group, LLC - All Rights Reserved */
package io.vlingo.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
 * Routing
 *
 * @author davem
 * @since Oct 28, 2018
 */
public class Routing<T> {
  
  public static <T> Routing<T> empty() {
    return new Routing<T>();
  }

  public static <T> Routing<T> with(T routee) {
    return new Routing<T>(Arrays.asList(routee));
  }
  
  public static <T> Routing<T> with(List<T> routees) {
    return new Routing<T>(routees);
  }
  
  private final List<T> routees;
  
  protected Routing() {
    super();
    this.routees = new ArrayList<>();
  }
  
  protected Routing(List<T> routees) {
    super();
    this.routees = routees;
  }
    
  public List<T> routees() {
    return Collections.unmodifiableList(routees);
  }

  public boolean isEmpty() {
    return routees.isEmpty();
  }

  @Override
  public String toString() {
    return "Routing[routees=" + routees + "]";
  }
}
