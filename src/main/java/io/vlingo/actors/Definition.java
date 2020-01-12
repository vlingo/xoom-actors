// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Definition {
  public static final List<Object> NoParameters = new ArrayList<Object>();

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator) {
    return new Definition(type, instantiator);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters) {
    return new Definition(type, parameters);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Logger logger) {
    return new Definition(type, instantiator, logger);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Logger logger) {
    return new Definition(type, parameters, logger);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final String actorName) {
    return new Definition(type, instantiator, actorName);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final String actorName) {
    return new Definition(type, parameters, actorName);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final String actorName,
          final Logger logger) {
    return new Definition(type, instantiator, actorName, logger);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final String actorName,
          final Logger logger) {
    return new Definition(type, parameters, actorName, logger);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String actorName) {
    return new Definition(type, instantiator, parent, actorName);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String actorName) {
    return new Definition(type, parameters, parent, actorName);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final String mailboxName,
          final String actorName) {
    return new Definition(type, instantiator, null, mailboxName, actorName);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final String mailboxName,
          final String actorName) {
    return new Definition(type, parameters, null, mailboxName, actorName);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String mailboxName,
          final String actorName) {
    return new Definition(type, instantiator, parent, mailboxName, actorName);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String mailboxName,
          final String actorName) {
    return new Definition(type, parameters, parent, mailboxName, actorName);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final Logger logger) {
    return new Definition(type, instantiator, parent, mailboxName, actorName, logger);
  }

  public static Definition has(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final Logger logger) {
    return new Definition(type, parameters, parent, mailboxName, actorName, logger);
  }

  public static List<Object> parameters(Object... parameters) {
    final List<Object> allParameters = new ArrayList<Object>();
    for (final Object param : parameters) {
      allParameters.add(param);
    }
    return allParameters;
  }

  private final String actorName;
  private final ActorInstantiator<? extends Actor> instantiator;
  private final Logger logger;
  private final String mailboxName;
  private final List<Object> parameters;
  private final Actor parent;
  private final Supervisor supervisor;
  private final Class<? extends Actor> type;

  public Definition(final Class<? extends Actor> type, final ActorInstantiator<? extends Actor> instantiator) {
    this(type, instantiator, null, null, null, null);
  }

  public Definition(final Class<? extends Actor> type, final ActorInstantiator<? extends Actor> instantiator, final Logger logger) {
    this(type, instantiator, null, null, null, logger);
  }

  public Definition(final Class<? extends Actor> actor, final List<Object> parameters) {
    this(actor, parameters, null, null, null, null);
  }

  public Definition(final Class<? extends Actor> actor, final List<Object> parameters, final Logger logger) {
    this(actor, parameters, null, null, null, logger);
  }

  public Definition(final Class<? extends Actor> actor, final ActorInstantiator<? extends Actor> instantiator, final String actorName, final Logger logger) {
    this(actor, instantiator, null, null, actorName, logger);
  }

  public Definition(final Class<? extends Actor> actor, final List<Object> parameters, final String actorName, final Logger logger) {
    this(actor, parameters, null, null, actorName, logger);
  }

  public Definition(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final String actorName) {

    this(type, instantiator, null, null, actorName, null);
  }

  public Definition(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final String actorName) {

    this(type, parameters, null, null, actorName, null);
  }

  public Definition(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String actorName) {

    this(type, instantiator, parent, null, actorName, null);
  }

  public Definition(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String actorName) {

    this(type, parameters, parent, null, actorName, null);
  }

  public Definition(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String mailboxName,
          final String actorName) {

    this(type, instantiator, parent, mailboxName, actorName, null);
  }

  public Definition(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String mailboxName,
          final String actorName) {

    this(type, parameters, parent, mailboxName, actorName, null);
  }

  public Definition(
          final Class<? extends Actor> type,
          final List<Object> parameters,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final Logger logger) {

    this.type = type;
    this.instantiator = null;
    this.parameters = parameters;
    this.parent = parent;
    this.mailboxName = mailboxName;
    this.actorName = actorName;
    this.supervisor = assignSupervisor(parent);
    this.logger = logger;
  }

  public Definition(
          final Class<? extends Actor> type,
          final ActorInstantiator<? extends Actor> instantiator,
          final Actor parent,
          final String mailboxName,
          final String actorName,
          final Logger logger) {

    this.type = type;
    this.instantiator = instantiator;
    this.parameters = Collections.emptyList();
    this.parent = parent;
    this.mailboxName = mailboxName;
    this.actorName = actorName;
    this.supervisor = assignSupervisor(parent);
    this.logger = logger;
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

  private Supervisor assignSupervisor(final Actor parent) {
    if (parent != null && parent instanceof Supervisor) {
      return parent.lifeCycle.environment.stage.actorAs(parent, Supervisor.class);
    }
    return null;
  }
}
