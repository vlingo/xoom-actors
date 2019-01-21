// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;
/**
 * RouterSpecification specifies the definition and protocol of
 * the {@link Actor actors} to which a {@link Router} will route,
 * as well as other details such as pool size.
 */
public class RouterSpecification<P> {
  
  private final int initialPoolSize;
  private final Definition routerDefinition;
  private final Class<P> routerProtocol;
  
  public RouterSpecification(final int poolSize, final Definition routerDefinition, final Class<P> routerProtocol) {
    if (poolSize < 0)
      throw new IllegalArgumentException("poolSize must be 0 or greater");
    this.initialPoolSize = poolSize;
    this.routerDefinition = routerDefinition;
    this.routerProtocol = routerProtocol;
  }

  public int initialPoolSize() {
    return initialPoolSize;
  }
  
  public Definition routerDefinition() {
    return routerDefinition;
  }
  
  public Class<P> routerProtocol() {
    return routerProtocol;
  }
}
