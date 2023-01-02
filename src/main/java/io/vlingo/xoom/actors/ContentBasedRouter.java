// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.actors;
/**
 * ContentBasedRouter is a kind of {@link Router} that considers the
 * content of messages in computing a {@link Routing}
 */
public abstract class ContentBasedRouter<P> extends Router<P> {
  
  protected ContentBasedRouter(final RouterSpecification<P> specification) {
    super(specification);
  }

  /* @see io.vlingo.xoom.actors.Router#computeRouting() */
  @Override
  protected Routing<P> computeRouting() {
    throw new UnsupportedOperationException("This router does not have a default routing. Please re-implement the routingFor method(s)");
  }
}
