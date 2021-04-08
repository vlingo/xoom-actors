// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import static io.vlingo.xoom.common.compiler.DynaNaming.fullyQualifiedClassnameFor;

import java.lang.reflect.Constructor;
import java.util.concurrent.locks.Lock;

import io.vlingo.xoom.actors.ProxyGenerator.Result;
import io.vlingo.xoom.common.compiler.DynaClassLoader;
import io.vlingo.xoom.common.compiler.DynaCompiler;
import io.vlingo.xoom.common.compiler.DynaCompiler.Input;

public final class ActorProxy {

  public static <T> T createFor(final Class<T> protocol, final Actor actor, final Mailbox mailbox) {
    final T maybeCachedProxy = actor.lifeCycle.environment.lookUpProxy(protocol);

    if (maybeCachedProxy != null) {
      return maybeCachedProxy;
    }

    T newProxy = null;

    final String proxyClassname = fullyQualifiedClassnameFor(protocol, "__Proxy");
    Lock lock = ArgumentLock.acquire(protocol);
    lock.lock();
    try {
      newProxy = tryCreate(protocol, actor, mailbox, proxyClassname);
    } catch (Exception e) {
      newProxy = tryGenerateCreate(protocol, actor, mailbox, proxyClassname);
    } finally {
      if (newProxy != null) {
        actor.lifeCycle.environment.cacheProxy(newProxy);
      }
      lock.unlock();
    }

    return newProxy;
  }

  private static DynaClassLoader classLoaderFor(final Actor actor) {
    DynaClassLoader classLoader = actor.lifeCycle.environment.stage.world().classLoader();
    if (classLoader == null) {
      classLoader = new DynaClassLoader(ActorProxy.class.getClassLoader());
      actor.stage().world().classLoader(classLoader);
    }
    return classLoader;
  }

  private static Class<?> loadProxyClassFor(
          final String targetClassname,
          final Actor actor)
  throws ClassNotFoundException {
    final Class<?> proxyClass = Class.forName(targetClassname, true, classLoaderFor(actor));
    return proxyClass;
  }

  @SuppressWarnings("unchecked")
  private static <T> T tryCreate(
          final Class<T> protocol,
          final Actor actor,
          final Mailbox mailbox,
          final String targetClassname)
  throws Exception {
    final Class<?> proxyClass = loadProxyClassFor(targetClassname, actor);
    return (T) tryCreateWithProxyClass(proxyClass, actor, mailbox);
  }

  @SuppressWarnings("unchecked")
  private static <T> T tryCreateWithProxyClass(final Class<T> proxyClass, final Actor actor, final Mailbox mailbox) throws Exception {
    Constructor<?> ctor = proxyClass.getConstructor(new Class<?>[] {Actor.class, Mailbox.class});
    return (T) ctor.newInstance(actor, mailbox);
  }

  private static <T> T tryGenerateCreate(
          final Class<T> protocol,
          final Actor actor,
          final Mailbox mailbox,
          final String targetClassname) {

    final ClassLoader classLoader = classLoaderFor(actor);
    try (final ProxyGenerator generator = ProxyGenerator.forMain(classLoader, true, actor.logger())) {
      return tryGenerateCreate(protocol, actor, mailbox, generator, targetClassname);
    } catch (Exception emain) {
      try (final ProxyGenerator generator = ProxyGenerator.forTest(classLoader, true, actor.logger())) {
        return tryGenerateCreate(protocol, actor, mailbox, generator, targetClassname);
      } catch (Exception etest) {
        throw new IllegalArgumentException("Actor proxy " + protocol.getName() + " not created for main or test: " + etest.getMessage(), etest);
      }
    }
  }

  private static <T> T tryGenerateCreate(
          final Class<T> protocol,
          final Actor actor,
          final Mailbox mailbox,
          final ProxyGenerator generator,
          final String targetClassname) {
    try {
      final Result result = generator.generateFor(protocol.getName());
      final Input input = new Input(protocol, targetClassname, result.source, result.sourceFile, classLoaderFor(actor), generator.type(), true);
      final DynaCompiler proxyCompiler = new DynaCompiler();
      final Class<T> proxyClass = proxyCompiler.compile(input);
      return tryCreateWithProxyClass(proxyClass, actor, mailbox);
    } catch (Exception e) {
      throw new IllegalArgumentException("Actor proxy " + protocol.getName() + " not created because: " + e.getMessage(), e);
    }
  }
}
