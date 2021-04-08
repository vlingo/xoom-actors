// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.testkit;

import java.util.HashMap;
import java.util.Map;

public class TestState {
  private final Map<String, Object> state;

  public TestState() {
    this.state = new HashMap<String, Object>();
  }

  public TestState putValue(final String name, final Object value) {
    state.put(name, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T valueOf(final String name) {
    return (T) state.get(name);
  }
}
