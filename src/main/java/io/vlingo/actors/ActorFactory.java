// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ActorFactory {
  static final ThreadLocal<Environment> threadLocalEnvironment = new ThreadLocal<Environment>();

  @SuppressWarnings("serial")
  private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_OBJECT_TYPE = new HashMap<Class<?>, Class<?>>(8, 0.75f) {{
    put(byte.class, Byte.class);
    put(short.class, Short.class);
    put(int.class, Integer.class);
    put(long.class, Long.class);
    put(float.class, Float.class);
    put(double.class, Double.class);
    put(boolean.class, Boolean.class);
    put(char.class, Character.class);
  }};

  @SuppressWarnings("unchecked")
  public static Class<? extends Actor> actorClassWithProtocol(final String actorClassname, final Class<?> protocolClass) {
    try {
      final Class<? extends Actor> actorClass = (Class<? extends Actor>) Class.forName(actorClassname);
      assertActorWithProtocol(actorClass, protocolClass);
      return actorClass;
    } catch (Exception e) {
      throw new IllegalArgumentException("The class " + actorClassname + " cannot be loaded because: " + e.getMessage(), e);
    }
  }

  public static void assertActorWithProtocol(final Class<?> candidateActorClass, final Class<?> protocolClass) {
    Class<?> superclass = candidateActorClass.getSuperclass();
    while (superclass != null) {
      if (superclass == Actor.class) {
        break;
      }
      superclass = superclass.getSuperclass();
    }

    if (superclass == null) {
      throw new IllegalStateException("Class must extend io.vlingo.actors.Actor: " + candidateActorClass.getName());
    }

    for (final Class<?> protocolInterfaceClass : candidateActorClass.getInterfaces()) {
      if (protocolClass == protocolInterfaceClass) {
        return;
      }
    }
    throw new IllegalStateException("Actor class " + candidateActorClass.getName() + "must implement: " + protocolClass.getName());
  }

  static Actor actorFor(
          final Stage stage,
          final Actor parent,
          final Definition definition,
          final Address address,
          final Mailbox mailbox,
          final Supervisor supervisor,
          final Logger logger) throws Exception {

    final Environment environment =
            new Environment(
                    stage,
                    address,
                    definition,
                    parent,
                    mailbox,
                    supervisor,
                    new ActorLoggerAdapter(logger, definition.type().getName()));

    threadLocalEnvironment.set(environment);

    Actor actor = null;
    final Object[] parameterTypes = definition.internalParameters()
        .stream().map(Object::getClass).toArray();

    if (definition.hasInstantiator()) {
      actor = definition.instantiator().instantiate();
      actor.lifeCycle.sendStart(actor);
    } else if (definition.internalParameters().isEmpty()) {
      actor = definition.type().newInstance();
      actor.lifeCycle.sendStart(actor);
    } else {
      for (final Constructor<?> ctor : definition.type().getConstructors()) {
        if (ctor.getParameterCount() == definition.internalParameters().size()) {
          boolean cont = true;
          for (int i = 0; cont  && i < parameterTypes.length; i++) {
            final Class<?> ctorParameterType = PRIMITIVE_TO_OBJECT_TYPE
                .getOrDefault(ctor.getParameterTypes()[i], ctor.getParameterTypes()[i]);
            final Class<?> parameterType = (Class<?>) parameterTypes[i];
            cont = ctorParameterType.isAssignableFrom(parameterType);
          }

          if (cont) {
            actor = start(ctor, definition, address, logger);
            if (actor != null) {
              break;
            }
          }
        }
      }
    }

    if (actor == null) {
      throw new IllegalArgumentException(String.format("No constructor matches the given parameters %s.", parameterTypes));
    }

    if (parent != null) {
      parent.lifeCycle.environment.addChild(actor);
    }

    return actor;
  }

  @SuppressWarnings("unused")
  private static boolean implementing(Class<?>[] interfaces, Class<?> type) {
    return Arrays.asList(interfaces).contains(type);
  }

  @SuppressWarnings("unused")
  private static boolean isOrExtending(Class<?> type, Class<?> isOrExtends) {
    return isOrExtends.isAssignableFrom(type);
  }

  static Mailbox actorMailbox(final Stage stage, final Address address, final Definition definition, MailboxWrapper wrapper) {
    final String mailboxName = stage.world().mailboxNameFrom(definition.mailboxName());
    final Mailbox mailbox = stage.world().assignMailbox(mailboxName, address.hashCode());

    return wrapper.wrap(address, mailbox);
  }

  static Mailbox actorMailbox(final Stage stage, final Address address, final Definition definition) {
    return actorMailbox(stage, address, definition, MailboxWrapper.Identity);
  }

  private static Actor start(
          final Constructor<?> ctor,
          final Definition definition,
          final Address address,
          final Logger logger) throws Exception {

    Actor actor = null;
    Object[] args = null;
    Throwable cause = null;

    for (int times = 1; times <= 2; ++times) {
      try {
        if (times == 1) {
          args = definition.internalParameters().toArray();
        }
        actor = (Actor) ctor.newInstance(args);
        actor.lifeCycle.sendStart(actor);
        cause = null;
        return actor;
      } catch (Throwable t) {
        cause = (t.getCause() == null ? t : t.getCause());
        if (times == 1) {
          args = unfold(args);
        }
      }
    }

    if (cause != null) {
      logger.error("ActorFactory: failed actor creation. "
              + "This is sometimes cause be the constructor parameter types not matching "
              + "the types in the Definition.parameters(). Often it is caused by a "
              + "failure in the actor constructor. We have attempted to uncover "
              + "the root cause here, but that may not be available in some cases.\n"
              + "The root cause may be: " + cause + "\n"
              + "See stacktrace for more information. We strongly recommend reviewing your "
              + "constructor for possible failures in dependencies that it creates.",
              cause);

      throw new InstantiationException("ActorFactory failed actor creation for: " + address);
    }

    return actor;
  }

  private static Object[] unfold(final Object[] args) {
    final Object[] unfolded = new Object[args.length];
    for (int idx = 0; idx < args.length; ++idx) {
      Object currentArg = args[idx];
      if (currentArg.getClass().isArray()) {
        unfolded[idx] = ((Object[]) currentArg)[0];
      } else {
        unfolded[idx] = args[idx];
      }
    }
    return unfolded;
  }


  public interface MailboxWrapper {
    MailboxWrapper Identity = (a, m) -> m;
    Mailbox wrap(Address address, Mailbox mailbox);
  }
}
