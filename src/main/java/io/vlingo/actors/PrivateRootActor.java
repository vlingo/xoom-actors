// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public class PrivateRootActor extends Actor implements Stoppable, Supervisor {
  private final SupervisionStrategy strategy =
          new SupervisionStrategy() {
            @Override
            public int intensity() {
              return 0;
            }

            @Override
            public long period() {
              return 0;
            }

            @Override
            public Scope scope() {
              return Scope.One;
            }
          };
  
  public PrivateRootActor() {
    stage().world().setPrivateRoot(selfAs(Stoppable.class));
    
    stage().actorProtocolFor(
              Definition.has(PublicRootActor.class, Definition.NoParameters, World.PUBLIC_ROOT_NAME),
              NoProtocol.class,
              this,
              new Address(World.PUBLIC_ROOT_ID, World.PUBLIC_ROOT_NAME),
              null,
              null,
              logger());

    stage().actorProtocolFor(
              Definition.has(DeadLettersActor.class, Definition.NoParameters, World.DEADLETTERS_NAME),
              DeadLetters.class,
              this,
              new Address(World.DEADLETTERS_ID, World.DEADLETTERS_NAME),
              null,
              null,
              logger());
  }
  
  @Override
  protected void afterStop() {
    stage().world().setPrivateRoot(null);
    super.afterStop();
  }

  //=========================================
  // Supervisor
  //=========================================

  @Override
  public void inform(Throwable throwable, Supervised supervised) {
    logger().log("PrivateRootActor: Failure of: " + supervised.address() + ": Stopping.", throwable);
    supervised.stop(strategy.scope());
  }

  @Override
  public SupervisionStrategy supervisionStrategy() {
    return strategy;
  }
}
