/* Copyright (c) 2005-2018 - Blue River Systems Group, LLC - All Rights Reserved */
package io.vlingo.actors;
/**
 * RouterSpecification
 *
 * @author davem
 * @since Oct 26, 2018
 */
public class RouterSpecification {
  
  private final int poolSize; //TODO: refactor towards resizable pool
  private final Definition routerDefinition;
  private final Class<?> routerProtocol;
  
  public RouterSpecification(final int poolSize, final Definition routerDefinition, final Class<?> routerProtocol) {
    this.poolSize = poolSize;
    this.routerDefinition = routerDefinition;
    this.routerProtocol = routerProtocol;
  }

  public int poolSize() {
    return poolSize;
  }
  
  public Definition routerDefinition() {
    return routerDefinition;
  }
  
  public Class<?> routerProtocol() {
    return routerProtocol;
  }
}
