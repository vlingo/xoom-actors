// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.io.Serializable;
import java.util.*;

public final class Definition {
  public static final List<Object> NoParameters = Collections.emptyList();


  public static Definition from(final Stage stage,
                                final SerializationProxy proxy,
                                final Logger logger) {

    final Actor parent = Optional.ofNullable(proxy.parent)
        .map(p -> {
          ActorProxyBase.thunk(stage, proxy.parent);
          return stage.directory.actorOf(proxy.parent.address);
        }).orElse(null);

    return new Definition(
        proxy.type,
        proxy.instantiator,
        proxy.parameters,
        parent,
        proxy.mailboxName,
        proxy.actorName,
        logger,
        proxy.evictable
    );
  }



  private static Supervisor assignSupervisor(final Actor parent) {
    if (parent instanceof Supervisor) {
      return parent.lifeCycle.environment.stage.actorAs(parent, Supervisor.class);
    }
    return null;
  }


  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator) {
    return has(type, instantiator, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final boolean evictable) {
    return new Definition(type, instantiator, evictable);
  }

  public static Definition has(
      final Class<? extends Actor> type,
      final List<Object> parameters) {
    return has(type, parameters, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final boolean evictable) {
    return new Definition(type, parameters, evictable);
  }

  public static Definition has(
      final Class<? extends Actor> type,
      final ActorInstantiator<? extends Actor> instantiator,
      final Logger logger) {
    return has(type, instantiator, logger, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Logger logger,
          final boolean evictable) {
    return new Definition(type, instantiator, logger, evictable);
  }

  public static Definition has(
      final Class<? extends Actor> type,
      final List<Object> parameters,
      final Logger logger) {
    return has(type, parameters, logger, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Logger logger,
          final boolean evictable) {
    return new Definition(type, parameters, logger, evictable);
  }

  public static Definition has(
      final Class<? extends Actor> type,
      final ActorInstantiator<? extends Actor> instantiator,
      final String actorName) {
    return has(type, instantiator, actorName, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final String actorName,
          final boolean evictable) {
    return new Definition(type, instantiator, actorName, evictable);
  }


  public static Definition has(
      final Class<? extends Actor> type,
      final List<Object> parameters,
      final String actorName) {
    return has(type, parameters, actorName, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final String actorName,
          final boolean evictable) {
    return new Definition(type, parameters, actorName, evictable);
  }

  public static Definition has(
      final Class<? extends Actor> type,
      final ActorInstantiator<? extends Actor> instantiator,
      final String actorName,
      final Logger logger) {
    return has(type, instantiator, actorName, logger, false);
  }

  public static Definition has(
      final Class<? extends Actor> type,
      final ActorInstantiator<? extends Actor> instantiator,
      final String actorName,
      final Logger logger,
      final boolean evictable) {
    return new Definition(type, instantiator, actorName, logger, evictable);
  }

  public static Definition has(
      final Class<? extends Actor> type,
      final List<Object> parameters,
      final String actorName,
      final Logger logger) {
    return has(type, parameters, actorName, logger, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final String actorName,
          final Logger logger,
          final boolean evictable) {
    return new Definition(type, parameters, actorName, logger, evictable);
  }

  public static Definition has(
      final Class<? extends Actor> type,
      final ActorInstantiator<? extends Actor> instantiator,
      final Actor parent,
      final String actorName) {
    return has(type, instantiator, parent, actorName, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String actorName,
          final boolean evictable) {
    return new Definition(type, instantiator, parent, actorName, evictable);
  }

  public static Definition has(
      final Class<? extends Actor> type,
      final List<Object> parameters,
      final Actor parent,
      final String actorName) {
    return has(type, parameters, parent, actorName, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String actorName,
          final boolean evictable) {
    return new Definition(type, parameters, parent, actorName, evictable);
  }

  public static Definition has(
      final Class<? extends Actor> type,
      final ActorInstantiator<? extends Actor> instantiator,
      final String mailboxName,
      final String actorName) {
    return has(type, instantiator, mailboxName, actorName, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final String mailboxName,
          final String actorName,
          final boolean evictable) {
    return new Definition(type, instantiator, null, mailboxName, actorName, evictable);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final String mailboxName,
          final String actorName) {
    return has(type, parameters,  mailboxName, actorName, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final String mailboxName,
          final String actorName,
          final boolean evictable) {
    return new Definition(type, parameters, null, mailboxName, actorName, evictable);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String mailboxName,
          final String actorName) {
    return has(type, instantiator, parent, mailboxName, actorName, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final boolean evictable) {
    return new Definition(type, instantiator, parent, mailboxName, actorName, evictable);
  }

  public static Definition has(
      final Class<? extends Actor> type,
      final List<Object> parameters,
      final Actor parent,
      final String mailboxName,
      final String actorName) {
    return has(type, parameters, parent, mailboxName, actorName, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final boolean evictable) {
    return new Definition(type, parameters, parent, mailboxName, actorName, evictable);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final Logger logger) {
    return has(type, instantiator, parent, mailboxName, actorName, logger, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final Logger logger,
          final boolean evictable) {
    return new Definition(type, instantiator, parent, mailboxName, actorName, logger, evictable);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final Logger logger) {
    return has(type, parameters, parent, mailboxName, actorName, logger, false);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final Logger logger,
          final boolean evictable) {
    return new Definition(type, parameters, parent, mailboxName, actorName, logger, evictable);
  }

  public static List<Object> parameters(Object... parameters) {
    return Arrays.asList(parameters);
  }

  private final String actorName;
  private final ActorInstantiator<? extends Actor> instantiator;
  private final Logger logger;
  private final String mailboxName;
  private final List<Object> parameters;
  private final Actor parent;
  private final Supervisor supervisor;
  private final Class<? extends Actor> type;
  final boolean evictable;

  public Definition(final Class<? extends Actor> type, final ActorInstantiator<? extends Actor> instantiator) {
    this(type, instantiator, false);
  }

  public Definition(final Class<? extends Actor> type, final ActorInstantiator<? extends Actor> instantiator, final boolean evictable) {
    this(type, instantiator, null, null, null, null, evictable);
  }

  public Definition(final Class<? extends Actor> type, final ActorInstantiator<? extends Actor> instantiator, final Logger logger) {
    this(type, instantiator, logger, false);
  }

  public Definition(final Class<? extends Actor> type, final ActorInstantiator<? extends Actor> instantiator, final Logger logger, final boolean evictable) {
    this(type, instantiator, null, null, null, logger, evictable);
  }

  public Definition(final Class<? extends Actor> actor, final List<Object> parameters) {
    this(actor, parameters, false);
  }

  public Definition(final Class<? extends Actor> actor, final List<Object> parameters, final boolean evictable) {
    this(actor, parameters, null, null, null, null, evictable);
  }

  public Definition(final Class<? extends Actor> actor, final List<Object> parameters, final Logger logger) {
    this(actor, parameters, logger, false);
  }

  public Definition(final Class<? extends Actor> actor, final List<Object> parameters, final Logger logger, final boolean evictable) {
    this(actor, parameters, null, null, null, logger, evictable);
  }

  public Definition(final Class<? extends Actor> actor, final ActorInstantiator<? extends Actor> instantiator, final String actorName, final Logger logger) {
    this(actor, instantiator, actorName, logger, false);
  }

  public Definition(final Class<? extends Actor> actor, final ActorInstantiator<? extends Actor> instantiator, final String actorName, final Logger logger, final boolean evictable) {
    this(actor, instantiator, null, null, actorName, logger, evictable);
  }

  public Definition(final Class<? extends Actor> actor, final List<Object> parameters, final String actorName, final Logger logger) {
    this(actor, parameters, actorName, logger, false);
  }

  public Definition(final Class<? extends Actor> actor, final List<Object> parameters, final String actorName, final Logger logger, final boolean evictable) {
    this(actor, parameters, null, null, actorName, logger, evictable);
  }

  public Definition(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final String actorName) {
    this(type, instantiator, actorName, false);
  }

  public Definition(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final String actorName,
          final boolean evictable) {

    this(type, instantiator, null, null, actorName, null, evictable);
  }

  public Definition(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final String actorName) {
    this(type, parameters, actorName, false);
  }

  public Definition(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final String actorName,
          final boolean evictable) {

    this(type, parameters, null, null, actorName, null, evictable);
  }

  public Definition(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String actorName) {
    this(type, instantiator, parent, actorName, false);
  }

  public Definition(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String actorName,
          final boolean evictable) {

    this(type, instantiator, parent, null, actorName, null, evictable);
  }

  public Definition(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String actorName) {
    this(type, parameters, parent, actorName, false);
  }

  public Definition(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String actorName,
          final boolean evictable) {

    this(type, parameters, parent, null, actorName, null, evictable);
  }

  public Definition(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String mailboxName,
          final String actorName) {
    this(type, instantiator, parent, mailboxName, actorName, false);
  }

  public Definition(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final boolean evictable) {

    this(type, instantiator, parent, mailboxName, actorName, null, evictable);
  }

  public Definition(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String mailboxName,
          final String actorName) {
    this(type, parameters, parent, mailboxName, actorName, false);
  }

  public Definition(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final boolean evictable) {

    this(type, parameters, parent, mailboxName, actorName, null, evictable);
  }

  public Definition(
      final Class<? extends Actor> type,
      final List<Object> parameters,
      final Actor parent,
      final String mailboxName,
      final String actorName,
      final Logger logger) {

    this(type, parameters, parent, mailboxName, actorName, logger, false);
  }

  public Definition(
      final Class<? extends Actor> type,
      final List<Object> parameters,
      final Actor parent,
      final String mailboxName,
      final String actorName,
      final Logger logger,
      boolean evictable) {

    this.type = type;
    this.instantiator = null;
    this.parameters = parameters;
    this.parent = parent;
    this.mailboxName = mailboxName;
    this.actorName = actorName;
    this.supervisor = assignSupervisor(parent);
    this.logger = logger;
    this.evictable = evictable;
  }

  public Definition(
      final Class<? extends Actor> type,
      final ActorInstantiator<? extends Actor> instantiator,
      final Actor parent,
      final String mailboxName,
      final String actorName,
      final Logger logger) {

    this(type, instantiator, parent, mailboxName, actorName, logger, false);
  }

  public Definition(
      final Class<? extends Actor> type,
      final ActorInstantiator<? extends Actor> instantiator,
      final Actor parent,
      final String mailboxName,
      final String actorName,
      final Logger logger,
      boolean evictable) {

    this(type, instantiator, NoParameters, parent, mailboxName, actorName, logger, evictable);

  }

  private Definition(
      final Class<? extends Actor> type,
      final ActorInstantiator<? extends Actor> instantiator,
      final List<Object> parameters,
      final Actor parent,
      final String mailboxName,
      final String actorName,
      final Logger logger,
      boolean evictable) {

    this.type = type;
    this.instantiator = instantiator;

    this.parameters = parameters;
    this.parent = parent;
    this.mailboxName = mailboxName;
    this.actorName = actorName;
    this.supervisor = Definition.assignSupervisor(parent);
    this.logger = logger;
    this.evictable = evictable;
  }


  public String actorName() {
    return actorName;
  }

  public boolean hasInstantiator() {
    return instantiator != null;
  }

  public ActorInstantiator<? extends Actor> instantiator() {
    return instantiator;
  }

  public Logger loggerOr(final Logger defaultLogger) {
    return logger != null ? logger : defaultLogger;
  }

  public String mailboxName() {
    return mailboxName;
  }

  public List<Object> parameters() {
    return new ArrayList<Object>(internalParameters());
  }

  public Actor parent() {
    return parent;
  }

  public Actor parentOr(final Actor defaultParent) {
    return parent != null ? parent : defaultParent;
  }

  public Supervisor supervisor() {
    return supervisor;
  }

  public Class<? extends Actor> type() {
    return type;
  }

  List<Object> internalParameters() {
    return parameters;
  }


  public static final class SerializationProxy implements Serializable {

    private static final long serialVersionUID = 2654451856010534929L;


    public static SerializationProxy from(final Definition definition) {
      return new SerializationProxy(
          definition.actorName,
          definition.instantiator,
          definition.mailboxName,
          definition.parameters,
          Optional.ofNullable(definition.parent)
              .map(ActorProxyStub::new).orElse(null),
          definition.type,
          definition.evictable
      );
    }


    public final String actorName;
    public final ActorInstantiator<? extends Actor> instantiator;
    public final String mailboxName;
    public final List<Object> parameters;
    public final ActorProxyStub<?> parent;
    public final Class<? extends Actor> type;
    public final boolean evictable;
    

    public SerializationProxy(
        String actorName,
        ActorInstantiator<? extends Actor> instantiator,
        String mailboxName,
        List<Object> parameters,
        ActorProxyStub<?> parent,
        Class<? extends Actor> type,
        boolean evictable) {

      this.actorName = actorName;
      this.instantiator = instantiator;
      this.mailboxName = mailboxName;
      this.parameters = parameters;
      this.parent = parent;
      this.type = type;
      this.evictable = evictable;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SerializationProxy that = (SerializationProxy) o;
      return Objects.equals(actorName, that.actorName) &&
          Objects.equals(instantiator, that.instantiator) &&
          Objects.equals(mailboxName, that.mailboxName) &&
          Objects.equals(parameters, that.parameters) &&
          Objects.equals(parent, that.parent) &&
          type.equals(that.type);

    }

    @Override
    public int hashCode() {
      return Objects.hash(
          actorName, instantiator, mailboxName, parameters, parent, type);

    }

    @Override
    public String toString() {
      return String.format(
          "Definition(actorName='%s', instantiator='%s', mailboxName='%s', " +
              "parameters='%s', parent='%s', type='%s')",
          actorName, instantiator, mailboxName,
          parameters, parent, type);
    }
  }
}
