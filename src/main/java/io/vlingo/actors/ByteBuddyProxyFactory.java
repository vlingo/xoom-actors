// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

public class ByteBuddyProxyFactory implements ProxyFactory {

  @SuppressWarnings("unchecked")
  @Override
  public <T> T createFor(final Class<T> protocol, final Actor actor, final Mailbox mailbox) {
    try {
      final ActorProxyInvocationHandler invocationHandler = new ActorProxyInvocationHandler(actor, mailbox);

      return (T) new ByteBuddy()
              .subclass(actor.getClass())
              .implement(protocol)
              .method(ElementMatchers.any())
              .intercept(InvocationHandlerAdapter.of(invocationHandler))
              .make()
              .load(actor.getClass().getClassLoader())
              .getLoaded()
              .getDeclaredConstructor()
              .newInstance();
//    } catch (InstantiationException | IllegalAccessException e) {
//      // TODO: Log
//      System.out.println("vlingo/actors: ByteBuddyProxyFactory createFor(Class<?>[], Actor, Mailbox) failed: " + e.getMessage());
//      throw new RuntimeException(e);
    } catch (Exception e) {
      System.out.println("vlingo/actors: ByteBuddyProxyFactory createFor(Class<?>[], Actor, Mailbox) failed: " + e.getMessage());
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public Object createFor(final Class<?>[] protocol, final Actor actor, final Mailbox mailbox) {
    try {
      final ActorProxyInvocationHandler invocationHandler = new ActorProxyInvocationHandler(actor, mailbox);

      return new ByteBuddy()
              .subclass(actor.getClass())
              .implement(protocol)
              .method(ElementMatchers.any())
              .intercept(InvocationHandlerAdapter.of(invocationHandler))
              .make()
              .load(actor.getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
              .getLoaded()
              .getDeclaredConstructor()
              .newInstance();
//    } catch (InstantiationException | IllegalAccessException e) {
//      // TODO: Log
//      System.out.println("vlingo/actors: ByteBuddyProxyFactory createFor(Class<?>[], Actor, Mailbox) failed: " + e.getMessage());
//      throw new RuntimeException(e);
    } catch (Exception e) {
      System.out.println("vlingo/actors: ByteBuddyProxyFactory createFor(Class<?>[], Actor, Mailbox) failed: " + e.getMessage());
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
