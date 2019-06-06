// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Ignore;
import org.junit.Test;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import io.vlingo.common.Completes;
import io.vlingo.actors.testkit.AccessSafely;

public class CompletesActorProtocolTest extends ActorsTest {
  private static final String Hello = "Hello, Completes!";
  private static final String HelloNot = "Bye!";
  private static final String Prefix = "*** ";
  
  @Test
  public void testReturnsCompletesForSideEffects() {
    final TestResults testResults = TestResults.afterCompleting(2);

    final UsesCompletes uc = world.actorFor(UsesCompletes.class, UsesCompletesActor.class);

    uc.getHello().andThenConsume((hello) -> testResults.setGreeting(hello.greeting));
    uc.getOne().andThenConsume((value) -> testResults.setValue(value));

    assertEquals(Hello, testResults.getGreeting());
    assertEquals(1, testResults.getValue().intValue());
  }

  @Test
  public void testAfterAndThenCompletesForSideEffects() {
    final TestResults greetingsTestResult = TestResults.afterCompleting(1);

    final UsesCompletes uc = world.actorFor(UsesCompletes.class, UsesCompletesActor.class);
    final Completes<Hello> helloCompletes = uc.getHello();
    helloCompletes.andThen((hello) -> new Hello(Prefix + hello.greeting))
         .andThenConsume((hello) -> greetingsTestResult.setGreeting(hello.greeting));

    assertNotEquals(Hello, greetingsTestResult.getGreeting());
    assertNotEquals(Hello, helloCompletes.outcome().greeting);
    assertEquals(Prefix + Hello, greetingsTestResult.getGreeting());
    assertEquals(Prefix + Hello, helloCompletes.outcome().greeting);

    final TestResults valueTestResult = TestResults.afterCompleting(1);

    final Completes<Integer> one = uc.getOne();
    one.andThen((value) -> value + 1).andThenConsume((value) -> valueTestResult.setValue(value));
    
    assertNotEquals(1, valueTestResult.getValue().intValue());
    assertNotEquals(new Integer(1), one.outcome());
    assertEquals(2, valueTestResult.getValue().intValue());
    assertEquals(new Integer(2), one.outcome());
  }

  @Test
  @Ignore("Need explanation of why it should timeout")
  public void testThatTimeOutOccursForSideEffects() {
    final UsesCompletes uc = world.actorFor(UsesCompletes.class, UsesCompletesCausesTimeoutActor.class);
    final TestResults greetingsTestResult = TestResults.afterCompleting(1);

    final Completes<Hello> helloCompletes =
            uc.getHello()
              .andThenConsume(2, new Hello(HelloNot), (hello) -> greetingsTestResult.setGreeting(hello.greeting))
              .otherwise((failedHello) -> { greetingsTestResult.setGreeting(failedHello.greeting); return failedHello; });

    assertNotEquals(Hello, greetingsTestResult.getGreeting());
    assertEquals(HelloNot, helloCompletes.outcome().greeting);

    final TestResults valueTestResult = TestResults.afterCompleting(1);

    final Completes<Integer> oneCompletes =
            uc.getOne()
              .andThenConsume(2, 0, (Integer value) -> valueTestResult.setValue(value))
              .otherwise((Integer value) -> { valueTestResult.setValue(value); return 0; });
    try { Thread.sleep(100); } catch (Exception e) { }
    oneCompletes.with(1);
    assertNotEquals(1, valueTestResult.getValue().intValue());
    assertEquals(new Integer(0), oneCompletes.outcome());
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


  private static class TestResults{
    private final AtomicReference<String> receivedGreeting = new AtomicReference<>(null);
    private final AtomicInteger receivedValue = new AtomicInteger(0);
    private final AccessSafely received;

    private TestResults(AccessSafely received) {
      this.received = received;
    }

    private static TestResults afterCompleting(final int times) {
      final TestResults testResults = new TestResults(AccessSafely.afterCompleting(times));
      testResults.received.writingWith("receivedGreeting", testResults.receivedGreeting::set);
      testResults.received.readingWith("receivedGreeting", testResults.receivedGreeting::get);
      testResults.received.writingWith("receivedValue", testResults.receivedValue::set);
      testResults.received.readingWith("receivedValue", testResults.receivedValue::get);
      return testResults;
    }

    private void setGreeting(String greeting){
        this.received.writeUsing("receivedGreeting", greeting);
    }

    private void setValue(Integer value){
        this.received.writeUsing("receivedValue", value);
    }

    private String getGreeting(){
      return this.received.readFrom("receivedGreeting");
    }

    private Integer getValue(){
      return this.received.readFrom("receivedValue");
    }
  }
}
