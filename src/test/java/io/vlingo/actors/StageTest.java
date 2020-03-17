// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.WorldTest.Simple;
import io.vlingo.actors.WorldTest.SimpleActor;
import io.vlingo.actors.WorldTest.TestResults;
import io.vlingo.actors.plugin.mailbox.testkit.TestMailbox;
import io.vlingo.actors.testkit.AccessSafely;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

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

  @Test
  public void testSingleThreadRawLookupOrStartFindsActorPreviouslyStartedWithActorFor() {
    Address address = world.addressFactory().unique();
    final Definition definition = Definition.has(ParentInterfaceActor.class,
        ParentInterfaceActor::new);
    world.stage().actorFor(NoProtocol.class, definition, address);
    Actor existing = world.stage().rawLookupOrStart(definition, address);
    assertSame(address, existing.address());
  }

  @Test
  public void testSingleThreadRawLookupOrStartFindsActorPreviouslyStartedWithRawLookupOrStart() {
    Address address = world.addressFactory().unique();
    final Definition definition = Definition.has(ParentInterfaceActor.class,
        ParentInterfaceActor::new);
    Actor started = world.stage().rawLookupOrStart(definition, address);
    Actor found = world.stage().rawLookupOrStart(definition, address);
    assertSame(started, found);
  }

  @Test
  public void testSingleThreadActorLookupOrStartFindsActorPreviouslyStartedWithActorFor() {
    Address address = world.addressFactory().unique();
    final Definition definition = Definition.has(ParentInterfaceActor.class,
        ParentInterfaceActor::new);
    world.stage().actorFor(NoProtocol.class, definition, address);
    Actor existing = world.stage().actorLookupOrStart(definition, address);
    assertSame(address, existing.address());
  }

  @Test
  public void testSingleThreadActorLookupOrStartFindsActorPreviouslyStartedWithActorLookupOrStart() {
    Address address = world.addressFactory().unique();
    final Definition definition = Definition.has(ParentInterfaceActor.class,
        ParentInterfaceActor::new);
    Actor started = world.stage().actorLookupOrStart(definition, address);
    Actor found = world.stage().actorLookupOrStart(definition, address);
    assertSame(started, found);
  }

  @Test
  public void testSingleThreadLookupOrStartFindsActorPreviouslyStartedWithActorFor() {
    Address address = world.addressFactory().unique();
    final Definition definition = Definition.has(ParentInterfaceActor.class,
        ParentInterfaceActor::new);
    world.stage().actorFor(NoProtocol.class, definition, address);
    assertNotNull(world.stage().lookupOrStart(NoProtocol.class, definition, address));
  }

  @Test
  public void testSingleThreadLookupOrStartFindsActorPreviouslyStartedWithLookupOrStart() {
    Address address = world.addressFactory().unique();
    final Definition definition = Definition.has(ParentInterfaceActor.class,
        ParentInterfaceActor::new);
    assertNotNull(world.stage().lookupOrStart(NoProtocol.class, definition, address));
    assertNotNull(world.stage().lookupOrStart(NoProtocol.class, definition, address));
  }

  private static final ExecutorService exec = Executors.newFixedThreadPool(32);

  @Test
  public void testMultiThreadRawLookupOrStartFindsActorPreviouslyStartedWIthRawLookupOrStart() {
    final int size = 1000;

    List<Address> addresses = IntStream.range(0, size)
        .mapToObj((ignored) -> world.addressFactory().unique())
        .collect(Collectors.toList());

    CompletionService<Actor> completionService =
        new ExecutorCompletionService<>(exec);

    final Definition definition = Definition.has(ParentInterfaceActor.class,
        ParentInterfaceActor::new);

    multithreadedLookupOrStartTest(index ->
            completionService.submit(() ->
                world.stage()
                    .rawLookupOrStart(definition, addresses.get(index)))
        , size);
  }

  @Test
  public void testMultiThreadActorLookupOrStartFindsActorPreviouslyStartedWIthActorLookupOrStart() {
    final int size = 1000;

    List<Address> addresses = IntStream.range(0, size)
        .mapToObj((ignored) -> world.addressFactory().unique())
        .collect(Collectors.toList());

    CompletionService<Actor> completionService =
        new ExecutorCompletionService<>(exec);

    final Definition definition = Definition.has(ParentInterfaceActor.class,
        ParentInterfaceActor::new);

    multithreadedLookupOrStartTest(index ->
            completionService.submit(() ->
                world.stage()
                    .actorLookupOrStart(definition, addresses.get(index)))
        , size);
  }

  private void multithreadedLookupOrStartTest(final Function<Integer, Future<Actor>> work, final int size) {
    List<Future<Actor>> futures = IntStream.range(0, size)
        .flatMap(i -> IntStream.of(i, i))
        .mapToObj(work::apply)
        .collect(Collectors.toList());

    List<Actor> results = new ArrayList<>(futures.size());
    for (Future<Actor> future : futures) {
      try {
        final Actor actor = future.get();
        if (!results.isEmpty() && results.size() % 2 != 0) {
          final Actor expected = results.get(results.size() - 1);
          assertSame(expected.address(), actor.address());
        }
        results.add(actor);
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
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
