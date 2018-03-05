// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class Supervisor__Proxy implements Supervisor {
  private static final String representationInform1 = "inform(Throwable, Supervised)";

  private final Actor actor;
  private final Mailbox mailbox;

  public Supervisor__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public void inform(final Throwable throwable, final Supervised supervised) {
    if (!actor.isStopped()) {
      final Consumer<Supervisor> consumer = (actor) -> actor.inform(throwable, supervised);
      mailbox.send(new LocalMessage<Supervisor>(actor, Supervisor.class, consumer, representationInform1));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, representationInform1));
    }
  }

  public SupervisionStrategy supervisionStrategy() {
    return null;
  }
}
