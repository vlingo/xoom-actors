// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public class PublicRootActor extends Actor implements Stoppable {
  public PublicRootActor() {
    stage().world().setDefaultParent(this);
    stage().world().setPublicRoot(selfAs(Stoppable.class));
  }

  // TODO: implement top-level supervision
  
  @Override
  protected void afterStop() {
    stage().world().setDefaultParent(null);
    stage().world().setPublicRoot(null);
    super.afterStop();
  }
}
