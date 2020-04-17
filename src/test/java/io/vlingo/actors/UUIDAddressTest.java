// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.common.identity.IdentityGeneratorType;

public class UUIDAddressTest {
  private static int MaxPings = 5000;

  private World world;

  @Test
  public void testNameGiven() throws Exception {
    final Address address = world.addressFactory().uniqueWith("test-address");

    assertNotNull(address);
    assertEquals("test-address", address.name());

    final Address another = world.addressFactory().uniqueWith("another-address");

    assertNotEquals(another, address);
    assertEquals("another-address", another.name());
  }

  @Test
  public void testIdType() {
    final Address address = world.addressFactory().uniqueWith("test-address");

    assertNotNull(address);
    assertEquals(0, address.compareTo(address));
    assertFalse(address.isDistributable());

    final UUID addressId = address.idTyped(); // asserts cast
    UUID.fromString(address.idString());      // asserts UUID compatibility

    final Address another = world.addressFactory().uniqueWith("another-address");

    assertNotEquals(another, address);
    assertNotEquals(0, address.compareTo(another));
    assertFalse(address.isDistributable());

    final UUID anotherId = another.idTyped(); // asserts cast
    UUID.fromString(another.idString());      // asserts UUID compatibility

    assertNotEquals(addressId, anotherId);
  }

  @Test
  public void testNameAndIdGiven() throws Exception {
    final String id = UUID.randomUUID().toString();
    final Address address = world.addressFactory().from(id, "test-address");

    assertNotNull(address);
    assertEquals(id, address.idString());
    assertEquals("test-address", address.name());
    assertEquals(address, world.addressFactory().from(id, "test-address"));

    final String anotherId = UUID.randomUUID().toString();
    final Address another = world.addressFactory().from(anotherId, "test-address-1");

    assertNotEquals(address.name(), another.name());
    assertNotEquals(0, address.compareTo(another));
    assertEquals(0, another.compareTo(world.addressFactory().from(anotherId, "test-address-1")));

    assertNotEquals(address.idString(), another.idString());
    assertNotEquals(address, another);
  }

  @Test
  public void testThatActorsAreOperational() {
    final TestResults testResults = new TestResults();

    final AccessSafely access = testResults.afterCompleting(MaxPings * 2);

    final Ping ping = world.actorFor(Ping.class, PingActor.class, testResults);

    ping.ping();

    final int pingCount = access.readFrom("pingCount");
    final int pongCount = access.readFrom("pongCount");

    assertEquals(pingCount, pongCount);
  }

  @Before
  public void setUp() {
    final AddressFactory addressFactory =
            new UUIDAddressFactory(IdentityGeneratorType.RANDOM);

    world =
            World.start(
                    "test-address",
                    Configuration.define().with(addressFactory));
  }

  public static interface Ping extends Stoppable {
    void ping();
  }

  public static class PingActor extends Actor implements Ping {
    private final Ping ping;
    private int pings;
    private final Pong pong;
    private final TestResults testResults;

    public PingActor(final TestResults testResults) {
      this.testResults = testResults;
      this.pong = childActorFor(Pong.class, Definition.has(PongActor.class, Definition.parameters(testResults)));
      this.pings = 0;
      this.ping = selfAs(Ping.class);
    }

    @Override
    public void ping() {
      testResults.access.writeUsing("pingCount", 1);

      final boolean stop = (++pings >= MaxPings);

      if (stop) ping.stop();

      pong.pong(ping);

      if (stop) pong.stop();
    }
  }

  public static interface Pong extends Stoppable {
    void pong(final Ping ping);
  }

  public  static class PongActor extends Actor implements Pong {
    private final TestResults testResults;

    public PongActor(final TestResults testResults) {
      this.testResults = testResults;
    }

    @Override
    public void pong(final Ping ping) {
      testResults.access.writeUsing("pongCount", 1);
      ping.ping();
    }
  }

  private static class TestResults {
    public AccessSafely access = AccessSafely.afterCompleting(0);

    public final AtomicInteger pingCount = new AtomicInteger(0);
    public final AtomicInteger pongCount = new AtomicInteger(0);

    public AccessSafely afterCompleting(final int times) {
      access =
        AccessSafely.afterCompleting(times)
        .writingWith("pingCount", (Integer increment) -> pingCount.incrementAndGet())
        .readingWith("pingCount", () -> pingCount.get())

        .writingWith("pongCount", (Integer increment) -> pongCount.incrementAndGet())
        .readingWith("pongCount", () -> pongCount.get());

      return access;
    }
  }
}
