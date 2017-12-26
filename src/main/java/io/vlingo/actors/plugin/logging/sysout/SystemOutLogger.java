// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.sysout;

import io.vlingo.actors.Logger;

public class SystemOutLogger implements Logger {

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void log(final String message) {
    System.out.println(message);
  }

  @Override
  public String name() {
    return "no-op";
  }
}
