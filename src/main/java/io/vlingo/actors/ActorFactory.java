// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.lang.reflect.Constructor;

public class ActorFactory {
  protected static final ThreadLocal<Environment> threadLocalEnvironment = new ThreadLocal<Environment>();

  protected static Actor actorFor(
          final Stage stage,
          final Actor parent,
          final Definition definition,
          final Address address,
          final Mailbox mailbox) throws Exception {
    
    final Environment environment =
            new Environment(
                    stage,
                    address,
                    definition,
                    parent,
                    mailbox);
    
    threadLocalEnvironment.set(environment);
    
    Actor actor = null;
    
    if (definition.internalParameters().isEmpty()) {
      actor = definition.type().newInstance();
    } else {
      for (final Constructor<?> ctor : definition.type().getConstructors()) {
        if (ctor.getParameterCount() == definition.internalParameters().size()) {
          actor = (Actor) ctor.newInstance(definition.internalParameters().toArray());
          break;
        }
      }
    }
    
    if (actor == null) {
      throw new IllegalArgumentException("No constructor matches the given number of parameters.");
    }
    
    if (parent != null) {
      parent.__internal__Environment().addChild(actor);
    }
    
    return actor;
  }

  protected static Mailbox actorMailbox(final Stage stage, final Address address, final Definition definition) {
    final String mailboxName = stage.world().mailboxNameFrom(definition.mailboxName());
    final Mailbox mailbox = stage.world().assignMailbox(mailboxName, address.hashCode());
    
    return mailbox;
  }
}
