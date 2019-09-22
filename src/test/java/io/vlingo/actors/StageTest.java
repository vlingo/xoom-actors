// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.vlingo.actors.WorldTest.Simple;
import io.vlingo.actors.WorldTest.SimpleActor;
import io.vlingo.actors.WorldTest.TestResults;
import io.vlingo.actors.plugin.mailbox.testkit.TestMailbox;
import io.vlingo.actors.testkit.AccessSafely;

public class StageTest extends ActorsTest {
  @Test
  public void testActorForDefinitionAndProtocol() {
    final NoProtocol test = world.stage().actorFor(NoProtocol.class, TestInterfaceActor.class);

    assertNotNull(test);
    assertNotNull(TestInterfaceActor.instance.get());
    assertEquals(world.defaultParent(), TestInterfaceActor.instance.get().lifeCycle.environment.parent);
  }

  @Test
  public void testActorForNoDefinitionAndProtocol() {
    final TestResults testResults = new TestResults(1);
    final Simple simple = world.stage().actorFor(Simple.class, SimpleActor.class, testResults);
    simple.simpleSay();
    assertTrue(testResults.getInvoked());

    // another

    final NoProtocol test = world.stage().actorFor(NoProtocol.class, TestInterfaceActor.class);
    assertNotNull(test);
    assertNotNull(TestInterfaceActor.instance.get());
    assertEquals(world.defaultParent(), TestInterfaceActor.instance.get().lifeCycle.environment.parent);
  }

  @Test
  public void testActorForAll() {
    world.actorFor(NoProtocol.class, ParentInterfaceActor.class);

    final Definition definition =
            Definition.has(
                    TestInterfaceActor.class,
                    Definition.NoParameters,
                    ParentInterfaceActor.parent.get(),
                    TestMailbox.Name,
                    "test-actor");

    final NoProtocol test = world.stage().actorFor(NoProtocol.class, definition);

    assertNotNull(test);
    assertNotNull(TestInterfaceActor.instance.get());
  }

  @Test
  public void testDirectoryScan() {
    final Address address1 = world.addressFactory().uniqueWith("test-actor1");
    final Address address2 = world.addressFactory().uniqueWith("test-actor2");
    final Address address3 = world.addressFactory().uniqueWith("test-actor3");
    final Address address4 = world.addressFactory().uniqueWith("test-actor4");
    final Address address5 = world.addressFactory().uniqueWith("test-actor5");

    final Address address6 = world.addressFactory().uniqueWith("test-actor6");
    final Address address7 = world.addressFactory().uniqueWith("test-actor7");

    world.stage().directory().register(address1, new TestInterfaceActor());
    world.stage().directory().register(address2, new TestInterfaceActor());
    world.stage().directory().register(address3, new TestInterfaceActor());
    world.stage().directory().register(address4, new TestInterfaceActor());
    world.stage().directory().register(address5, new TestInterfaceActor());

    final ScanResult scanResult = new ScanResult(7);

    world.stage().actorOf(NoProtocol.class, address5).andFinallyConsume(actor -> {
      assertNotNull(actor);
      scanResult.found();
    });
    world.stage().actorOf(NoProtocol.class, address4).andFinallyConsume(actor -> {
      assertNotNull(actor);
      scanResult.found();
    });
    world.stage().actorOf(NoProtocol.class, address3).andFinallyConsume(actor -> {
      assertNotNull(actor);
      scanResult.found();
    });
    world.stage().actorOf(NoProtocol.class, address2).andFinallyConsume(actor -> {
      assertNotNull(actor);
      scanResult.found();
    });
    world.stage().actorOf(NoProtocol.class, address1).andFinallyConsume(actor -> {
      assertNotNull(actor);
      scanResult.found();
    });

    world.stage().maybeActorOf(NoProtocol.class, address6)
      .andFinallyConsume((maybe) -> {
        if (maybe.isPresent()) scanResult.found();
        else scanResult.notFound();
      });
    world.stage().maybeActorOf(NoProtocol.class, address7)
      .andFinallyConsume((maybe) -> {
        if (maybe.isPresent()) scanResult.found();
        else scanResult.notFound();
    });

    assertEquals(5, scanResult.getFoundCount());
    assertEquals(2, scanResult.getNotFoundCount());
  }

  @Test
  public void testDirectoryScanMaybeActor() {
    final Address address1 = world.addressFactory().uniqueWith("test-actor1");
    final Address address2 = world.addressFactory().uniqueWith("test-actor2");
    final Address address3 = world.addressFactory().uniqueWith("test-actor3");
    final Address address4 = world.addressFactory().uniqueWith("test-actor4");
    final Address address5 = world.addressFactory().uniqueWith("test-actor5");

    final Address address6 = world.addressFactory().uniqueWith("test-actor6");
    final Address address7 = world.addressFactory().uniqueWith("test-actor7");

    world.stage().directory().register(address1, new TestInterfaceActor());
    world.stage().directory().register(address2, new TestInterfaceActor());
    world.stage().directory().register(address3, new TestInterfaceActor());
    world.stage().directory().register(address4, new TestInterfaceActor());
    world.stage().directory().register(address5, new TestInterfaceActor());

    final ScanResult scanResult = new ScanResult(7);

    world.stage().maybeActorOf(NoProtocol.class, address5).andFinallyConsume(maybe -> {
      assertTrue(maybe.isPresent());
      scanResult.found();
    });
    world.stage().maybeActorOf(NoProtocol.class, address4).andFinallyConsume(maybe -> {
      assertTrue(maybe.isPresent());
      scanResult.found();
    });
    world.stage().maybeActorOf(NoProtocol.class, address3).andFinallyConsume(maybe -> {
      assertTrue(maybe.isPresent());
      scanResult.found();
    });
    world.stage().maybeActorOf(NoProtocol.class, address2).andFinallyConsume(maybe -> {
      assertTrue(maybe.isPresent());
      scanResult.found();
    });
    world.stage().maybeActorOf(NoProtocol.class, address1).andFinallyConsume(maybe -> {
      assertTrue(maybe.isPresent());
      scanResult.found();
    });

    world.stage().maybeActorOf(NoProtocol.class, address6)
      .andFinallyConsume(maybe -> {
        assertFalse(maybe.isPresent());
        scanResult.notFound();
      });
    world.stage().maybeActorOf(NoProtocol.class, address7)
      .andFinallyConsume(maybe -> {
        assertFalse(maybe.isPresent());
        scanResult.notFound();
      });

    assertEquals(5, scanResult.getFoundCount());
    assertEquals(2, scanResult.getNotFoundCount());
  }

  @Test
  public void testThatProtocolIsInterface() {
    world.stage().actorFor(NoProtocol.class, ParentInterfaceActor.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThatProtocolIsNotInterface() {
    world.stage().actorFor(ParentInterfaceActor.class, ParentInterfaceActor.class);
  }

  @Test
  public void testThatProtocolsAreInterfaces() {
    world.stage().actorFor(new Class[] { NoProtocol.class, NoProtocol.class }, ParentInterfaceActor.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThatProtocolsAreNotInterfaces() {
    world.stage().actorFor(new Class[] { NoProtocol.class, ParentInterfaceActor.class }, ParentInterfaceActor.class);
  }

  public static class ParentInterfaceActor extends Actor implements NoProtocol {
    public static ThreadLocal<ParentInterfaceActor> parent = new ThreadLocal<>();

    public ParentInterfaceActor() { parent.set(this); }
  }

  public static class TestInterfaceActor extends Actor implements NoProtocol {
    public static ThreadLocal<TestInterfaceActor> instance = new ThreadLocal<>();

    public TestInterfaceActor() {
      instance.set(this);
    }
  }

  private static class ScanResult {
    final AccessSafely scanFound;

    private ScanResult(final int times) {
      final AtomicInteger foundCount = new AtomicInteger(0);
      final AtomicInteger notFoundCount = new AtomicInteger(0);
      this.scanFound = AccessSafely.afterCompleting(times)
              .writingWith("foundCount", (Integer ignored) -> foundCount.incrementAndGet())
              .readingWith("foundCount", foundCount::get)
              .writingWith("notFoundCount", (Integer ignored) -> notFoundCount.incrementAndGet())
              .readingWith("notFoundCount", notFoundCount::get);
    }

    private int getFoundCount(){
      return scanFound.readFrom("foundCount");
    }

    private int getNotFoundCount(){
      return scanFound.readFrom("notFoundCount");
    }

    private void found(){
      this.scanFound.writeUsing("foundCount", 1);
    }

    private void notFound(){
      this.scanFound.writeUsing("notFoundCount", 1);
    }
  }
}
