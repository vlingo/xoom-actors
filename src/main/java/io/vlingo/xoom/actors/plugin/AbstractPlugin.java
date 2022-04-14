// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin;

public abstract class AbstractPlugin implements Plugin {

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != getClass()) {
      return false;
    }
    return (name().equals(((Plugin) other).name()));
  }
}
