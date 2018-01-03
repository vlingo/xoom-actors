// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.plugin.mailbox.testkit.TestMailbox;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class ByteBuddyProxyFactoryTest {

  private final ByteBuddyProxyFactory proxyFactory = new ByteBuddyProxyFactory();

  private final Mailbox mailbox = new TestMailbox();

  @Test
  public void createForSingleClassProtocol() {
    final TellSomethingActor actor = new TellSomethingActor();
    final Object proxy = proxyFactory.createFor(TellSomething.class, actor, mailbox);
    Assert.assertNotNull(proxy);
    Assert.assertThat(proxy, CoreMatchers.instanceOf(TellSomething.class));
  }

  @Test
  public void createForMultipleClassProtocol() {
    final TellSomethingElseActor actor = new TellSomethingElseActor();
    final Class<?>[] protocol = { TellSomethingElse.class };
    final Object proxy = proxyFactory.createFor(protocol, actor, mailbox);
    Assert.assertNotNull(proxy);
    Assert.assertThat(proxy, CoreMatchers.instanceOf(TellSomething.class));
    Assert.assertThat(proxy, CoreMatchers.instanceOf(TellSomethingElse.class));
  }

  public interface TellSomething {
    void tellMeSomething(final String something, final int value);
  }

  public interface TellSomethingElse {
    void tellMeSomethingElse(final String something, final int value);
  }

  public static class TellSomethingActor extends Actor implements TellSomething {
    public void tellMeSomething(final String something, final int value) {
    }
  }

  public static class TellSomethingElseActor extends TellSomethingActor implements TellSomethingElse {
    public void tellMeSomethingElse(final String something, final int value) {
    }
  }
}