// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.common.Completes;

public class CompletesActorProtocolTest extends ActorsTest {
  private static final String Hello = "Hello, Completes!";
  private static final String HelloNot = "Bye!";
  private static final String Prefix = "*** ";

  private String greeting;
  private int value;
  private TestUntil untilHello = TestUntil.happenings(1);
  private TestUntil untilOne = TestUntil.happenings(1);

  @Test
  public void testReturnsCompletesForSideEffects() {
    final UsesCompletes uc = world.actorFor(Definition.has(UsesCompletesActor.class, Definition.NoParameters), UsesCompletes.class);

    uc.getHello().andThenConsume((hello) -> setHello(hello.greeting));
    untilHello.completes();
    assertEquals(Hello, greeting);
    uc.getOne().andThenConsume((value) -> setValue(value));
    untilOne.completes();
    assertEquals(1, value);
  }

  @Test
  public void testAfterAndThenCompletesForSideEffects() {
    final UsesCompletes uc = world.actorFor(Definition.has(UsesCompletesActor.class, Definition.NoParameters), UsesCompletes.class);
    final Completes<Hello> helloCompletes = uc.getHello();
    helloCompletes.andThen((hello) -> new Hello(Prefix + helloCompletes.outcome().greeting))
         .andThenConsume((hello) -> setHello(hello.greeting));
    untilHello.completes();
    assertNotEquals(Hello, helloCompletes.outcome().greeting);
    assertNotEquals(Hello, this.greeting);
    assertEquals(Prefix + Hello, helloCompletes.outcome().greeting);
    assertEquals(Prefix + Hello, this.greeting);

    final Completes<Integer> one = uc.getOne();
    one.andThen((value) -> one.outcome() + 1).andThenConsume((value) -> setValue(value));
    untilOne.completes();
    assertNotEquals(new Integer(1), one.outcome());
    assertNotEquals(1, this.value);
    assertEquals(new Integer(2), one.outcome());
    assertEquals(2, this.value);
  }

  @Test
  public void testThatTimeOutOccursForSideEffects() {
    final UsesCompletes uc = world.actorFor(Definition.has(UsesCompletesCausesTimeoutActor.class, Definition.NoParameters), UsesCompletes.class);

    final Completes<Hello> helloCompletes =
            uc.getHello()
              .andThenConsume(2, new Hello(HelloNot), (hello) -> setHello(hello.greeting))
              .otherwise((failedHello) -> { setHello(failedHello.greeting); return failedHello; });
    untilHello.completes();
    assertNotEquals(Hello, greeting);
    assertEquals(HelloNot, helloCompletes.outcome().greeting);

    final Completes<Integer> oneCompletes =
            uc.getOne()
              .andThenConsume(2, 0, (Integer value) -> setValue(value))
              .otherwise((Integer value) -> { untilOne.happened(); return 0; });
    try { Thread.sleep(100); } catch (Exception e) { }
    oneCompletes.with(1);
    untilOne.completes();
    assertNotEquals(1, value);
    assertEquals(new Integer(0), oneCompletes.outcome());
  }

  private void setHello(final String hello) {
    this.greeting = hello;
    untilHello.happened();
  }

  private void setValue(final int value) {
    this.value = value;
    untilOne.happened();
  }

  public static class Hello {
    public final String greeting;

    public Hello(final String greeting) {
      this.greeting = greeting;
    }

    @Override
    public String toString() {
      return "Hello[" + greeting + "]";
    }
  }

  public static interface UsesCompletes {
    Completes<Hello> getHello();
    Completes<Integer> getOne();
  }

  public static class UsesCompletesActor extends Actor implements UsesCompletes {
    public UsesCompletesActor() { }

    @Override
    public Completes<Hello> getHello() {
      return completes().with(new Hello(Hello));
    }

    @Override
    public Completes<Integer> getOne() {
      return completes().with(new Integer(1));
    }
  }

  public static class UsesCompletesCausesTimeoutActor extends Actor implements UsesCompletes {
    public UsesCompletesCausesTimeoutActor() { }

    @Override
    public Completes<Hello> getHello() {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // ignore
      }
      return completes().with(new Hello(Hello));
    }

    @Override
    public Completes<Integer> getOne() {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // ignore
      }
      return completes().with(new Integer(1));
    }
  }
}
