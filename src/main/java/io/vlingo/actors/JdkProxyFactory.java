// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JdkProxyFactory implements ProxyFactory {

  @Override
  @SuppressWarnings("unchecked")
  public <T> T createFor(final Class<T> protocol, final Actor actor, final Mailbox mailbox) {
    final InvocationHandler handler = new ActorProxyInvocationHandler(actor, mailbox);
    return (T) Proxy.newProxyInstance(protocol.getClassLoader(), new Class[] { protocol }, handler);
  }

  @Override
  public Object createFor(final Class<?>[] protocol, final Actor actor, final Mailbox mailbox) {
    final InvocationHandler handler = new ActorProxyInvocationHandler(actor, mailbox);
    return Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(), protocol, handler);
  }
}
