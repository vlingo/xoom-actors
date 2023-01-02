// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.testkit;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Environment;
import io.vlingo.xoom.actors.plugin.mailbox.testkit.TestMailbox;

public class TestEnvironment extends Environment {
  public TestEnvironment() {
    super(
            TestWorld.Instance.get().world().stage(),
            TestWorld.Instance.get().world().addressFactory().uniqueWith("test"),
            Definition.has(null, Definition.NoParameters),
            TestWorld.Instance.get().world().defaultParent(),
            new TestMailbox(),
            TestWorld.Instance.get().world().defaultSupervisor(),
            TestWorld.Instance.get().world().defaultLogger());
  }
}
