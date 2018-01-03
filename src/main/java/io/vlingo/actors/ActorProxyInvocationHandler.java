// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ActorProxyInvocationHandler implements InvocationHandler {
  private final Actor actor;
  private final Mailbox mailbox;

  ActorProxyInvocationHandler(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    if (!actor.isStopped()) {
      mailbox.send(new Message(actor, method, args));
    } else if (!actor.__internal__Environment().stage.world().isTerminated()) {
      actor.__internal__Environment().stage.world().deadLetters().failedDelivery(new DeadLetter(actor, method, args));
    }

    return null;
  }
}
