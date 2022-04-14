// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

class ActorProxyStub<T> extends ActorProxyBase<T> {

  private static final long serialVersionUID = -7711746801630270471L;

  public ActorProxyStub(Actor actor) {
    super(null,
        Definition.SerializationProxy.from(actor.definition()),
        actor.address());
  }

  public ActorProxyStub() { }
}
