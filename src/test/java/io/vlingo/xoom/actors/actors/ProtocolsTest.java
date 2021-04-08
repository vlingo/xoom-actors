// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.xoom.actors.testkit.TestActor;

public class ProtocolsTest extends ActorsTest {

  @Test
  public void testTwoProtocols() {
    final Protocols protocols =
            testWorld.actorFor(
                    new Class<?>[] { P1.class, P2.class },
                    Definition.has(TwoProtocolsActor.class, Definition.NoParameters));
    
    final Protocols.Two<TestActor<P1>, TestActor<P2>> two = Protocols.two(protocols);
    
    two._1.actor().do1();
    assertEquals(1, TwoProtocolsActor.instance.get().do1Count);
    
    two._2.actor().do2();
    two._2.actor().do2();
    assertEquals(2, TwoProtocolsActor.instance.get().do2Count);
  }

  @Test
  public void testThreeProtocols() {
    final Protocols protocols =
            testWorld.actorFor(
                    new Class<?>[] { P1.class, P2.class, P3.class },
                    Definition.has(ThreeProtocolsActor.class, Definition.NoParameters));
    
    final Protocols.Three<TestActor<P1>, TestActor<P2>, TestActor<P3>> three = Protocols.three(protocols);
    
    three._1.actor().do1();
    assertEquals(1, ThreeProtocolsActor.instance.get().do1Count);
    
    three._2.actor().do2();
    three._2.actor().do2();
    assertEquals(2, ThreeProtocolsActor.instance.get().do2Count);
    
    three._3.actor().do3();
    three._3.actor().do3();
    three._3.actor().do3();
    assertEquals(3, ThreeProtocolsActor.instance.get().do3Count);
  }

  @Test
  public void testFourProtocols() {
    final Protocols protocols =
            testWorld.actorFor(
                    new Class<?>[] { P1.class, P2.class, P3.class, P4.class },
                    Definition.has(FourProtocolsActor.class, Definition.NoParameters));
    
    final Protocols.Four<TestActor<P1>, TestActor<P2>, TestActor<P3>, TestActor<P4>> four = Protocols.four(protocols);
    
    four._1.actor().do1();
    assertEquals(1, FourProtocolsActor.instance.get().do1Count);
    
    four._2.actor().do2();
    four._2.actor().do2();
    assertEquals(2, FourProtocolsActor.instance.get().do2Count);
    
    four._3.actor().do3();
    four._3.actor().do3();
    four._3.actor().do3();
    assertEquals(3, FourProtocolsActor.instance.get().do3Count);
    
    four._4.actor().do4();
    four._4.actor().do4();
    four._4.actor().do4();
    four._4.actor().do4();
    assertEquals(4, FourProtocolsActor.instance.get().do4Count);
  }

  @Test
  public void testFiveProtocols() {
    final Protocols protocols =
            testWorld.actorFor(
                    new Class<?>[] { P1.class, P2.class, P3.class, P4.class, P5.class },
                    Definition.has(FiveProtocolsActor.class, Definition.NoParameters));
    
    final Protocols.Five<TestActor<P1>, TestActor<P2>, TestActor<P3>, TestActor<P4>, TestActor<P5>> five = Protocols.five(protocols);
    
    five._1.actor().do1();
    assertEquals(1, FiveProtocolsActor.instance.get().do1Count);
    
    five._2.actor().do2();
    five._2.actor().do2();
    assertEquals(2, FiveProtocolsActor.instance.get().do2Count);
    
    five._3.actor().do3();
    five._3.actor().do3();
    five._3.actor().do3();
    assertEquals(3, FiveProtocolsActor.instance.get().do3Count);
    
    five._4.actor().do4();
    five._4.actor().do4();
    five._4.actor().do4();
    five._4.actor().do4();
    assertEquals(4, FiveProtocolsActor.instance.get().do4Count);
    
    five._5.actor().do5();
    five._5.actor().do5();
    five._5.actor().do5();
    five._5.actor().do5();
    five._5.actor().do5();
    assertEquals(5, FiveProtocolsActor.instance.get().do5Count);
  }

  public static interface P1 {
    void do1();
  }

  public static interface P2 {
    void do2();
  }

  public static interface P3 {
    void do3();
  }

  public static interface P4 {
    void do4();
  }

  public static interface P5 {
    void do5();
  }

  public static class TwoProtocolsActor extends Actor implements P1, P2 {
    public static ThreadLocal<TwoProtocolsActor> instance = new ThreadLocal<>();
    
    public int do1Count;
    public int do2Count;
    
    public TwoProtocolsActor() {
      instance.set(this);
    }
    
    @Override
    public void do1() {
      ++do1Count;
    }

    @Override
    public void do2() {
      ++do2Count;
    }
  }

  public static class ThreeProtocolsActor extends Actor implements P1, P2, P3 {
    public static final ThreadLocal<ThreeProtocolsActor> instance = new ThreadLocal<>();
    
    public int do1Count;
    public int do2Count;
    public int do3Count;
    
    public ThreeProtocolsActor() {
      instance.set(this);
    }
    
    @Override
    public void do1() {
      ++do1Count;
    }

    @Override
    public void do2() {
      ++do2Count;
    }

    @Override
    public void do3() {
      ++do3Count;
    }
  }

  public static class FourProtocolsActor extends Actor implements P1, P2, P3, P4 {
    public static ThreadLocal<FourProtocolsActor> instance = new ThreadLocal<>();
    
    public int do1Count;
    public int do2Count;
    public int do3Count;
    public int do4Count;
    
    public FourProtocolsActor() {
      instance.set(this);
    }
    
    @Override
    public void do1() {
      ++do1Count;
    }

    @Override
    public void do2() {
      ++do2Count;
    }

    @Override
    public void do3() {
      ++do3Count;
    }

    @Override
    public void do4() {
      ++do4Count;
    }
  }

  public static class FiveProtocolsActor extends Actor implements P1, P2, P3, P4, P5 {
    public static ThreadLocal<FiveProtocolsActor> instance = new ThreadLocal<>();
    
    public int do1Count;
    public int do2Count;
    public int do3Count;
    public int do4Count;
    public int do5Count;
    
    public FiveProtocolsActor() {
      instance.set(this);
    }
    
    @Override
    public void do1() {
      ++do1Count;
    }

    @Override
    public void do2() {
      ++do2Count;
    }

    @Override
    public void do3() {
      ++do3Count;
    }

    @Override
    public void do4() {
      ++do4Count;
    }

    @Override
    public void do5() {
      ++do5Count;
    }
  }
}
