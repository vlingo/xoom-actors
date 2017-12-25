// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ActorProxy implements InvocationHandler {
  private final Actor actor;
  private final Mailbox mailbox;

  @SuppressWarnings("unchecked")
  protected static <T extends Object> T createFor(
          final Class<T> protocol,
          final Actor actor,
          final Mailbox mailbox) {

    final InvocationHandler handler = new ActorProxy(actor, mailbox);

    final T proxy = (T) Proxy.newProxyInstance(protocol.getClassLoader(), new Class[] { protocol }, handler);

    return proxy;
  }

  protected static Object createFor(
          final Class<?>[] protocol,
          final Actor actor,
          final Mailbox mailbox) {
    
    final InvocationHandler handler = new ActorProxy(actor, mailbox);

    final Object proxy = Proxy.newProxyInstance(ActorProxy.class.getClassLoader(), protocol, handler);

    return proxy;
  }
  
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    if (!actor.isStopped()) {
      mailbox.send(new Message(actor, method, args));
    } else if (!actor.__internal__Environment().stage.world().isTerminated()) {
      actor.__internal__Environment().stage.world().deadLetters().failedDelivery(new DeadLetter(actor, method, args));
    }

    return null;
  }

  private ActorProxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }
}
