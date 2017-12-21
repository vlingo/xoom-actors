// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OutcomeInterestTest {
  private World world;
  
  @Test
  public void testOutcomeInterestSuccess() throws Exception {
    final Definition def = Definition.has(DoSomethingOutcomeAwareActor.class, Definition.NoParameters);
    final TestsOutcomeAware test = world.actorFor(def, TestsOutcomeAware.class);
    test.doSomething();
    
    Thread.sleep(500);
  }
  
  @Before
  public void setUp() {
    world = World.start("test");
  }
  
  @After
  public void tearDown() {
    world.terminate();
  }

  public static class DoSomethingOutcomeAwareActor extends Actor implements TestsOutcomeAware, OutcomeAware<String, Integer> {
    
    private TestsOutcomeInterest testsInterest;
    
    public DoSomethingOutcomeAwareActor() {
      this.testsInterest =
              stage().actorFor(
                      Definition.has(DoSomethingWithOutcomeInterestActor.class, Definition.NoParameters),
                      TestsOutcomeInterest.class);
    }

    @Override
    public void doSomething() {
      @SuppressWarnings("unchecked")
      final OutcomeInterest<String> interest = selfAsOutcomeInterest(new Integer(1));
      testsInterest.doSomethingWith("something", interest);
    }

    @Override
    public void failureOutcome(Outcome<String> outcome, Integer reference) {
      System.out.println("SUCCESS: outcome=" + outcome + " reference=" + reference);
    }

    @Override
    public void successfulOutcome(Outcome<String> outcome, Integer reference) {
      System.out.println("SUCCESS: outcome=" + outcome.value() + " reference=" + reference);
    }
  }

  public static class DoSomethingWithOutcomeInterestActor extends Actor implements TestsOutcomeInterest {

    @Override
    public void doSomethingWith(String text, OutcomeInterest<String> interest) {
      interest.successfulOutcome(new SuccessfulOutcome<String>(text + "-something-else"));
    }
  }

  public static interface TestsOutcomeAware {
    void doSomething();
  }

  public static interface TestsOutcomeInterest {
    void doSomethingWith(final String text, final OutcomeInterest<String> interest);
  }
}
