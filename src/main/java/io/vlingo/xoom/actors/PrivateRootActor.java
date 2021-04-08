// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

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

    @Override
    protected void beforeStart() {
        super.beforeStart();
        stage().world().setPrivateRoot(selfAs(Stoppable.class));

        stage().actorProtocolFor(
                NoProtocol.class,
                Definition.has(PublicRootActor.class, PublicRootActor::new, World.PUBLIC_ROOT_NAME),
                this,
                stage().world().addressFactory().from(World.PUBLIC_ROOT_ID, World.PUBLIC_ROOT_NAME),
                null,
                null,
                logger());

        stage().actorProtocolFor(
                DeadLetters.class,
                Definition.has(DeadLettersActor.class, DeadLettersActor::new, World.DEADLETTERS_NAME),
                this,
                stage().world().addressFactory().from(World.DEADLETTERS_ID, World.DEADLETTERS_NAME),
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
    logger().error("PrivateRootActor: Failure of: " + supervised.address() + " because: " + throwable.getMessage() + " Action: Stopping.", throwable);
    supervised.stop(strategy.scope());
  }

  @Override
  public SupervisionStrategy supervisionStrategy() {
    return strategy;
  }
}
