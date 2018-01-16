// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import org.junit.Test;

public class ActorMessageSendingSpeedTest {
  private static final int Max = 100_000_000;
  
  private static long StartTime = 0;
  private static long EndTime = 0;
  
  @Test
  public void test100millionSendsOnQueueMailbox() throws Exception {
    runWith("queueMailbox");
  }
  
  @Test
  public void test100millionSendsOnSharedRingBufferMailbox() throws Exception {
    runWith("ringMailbox");
  }
  
  @Test
  public void test100millionSendsOnArrayQueueMailbox() throws Exception {
    runWith("arrayQueueMailbox");
  }
  
  private void runWith(final String mailboxType) throws Exception {
    final World world = World.start("speed-test");
    
    final SingleOperation actor =
            world.actorFor(
                    Definition.has(
                            SingleOperationActor.class,
                            Definition.NoParameters,
                            mailboxType,
                            "single-op"),
                    SingleOperation.class);
    
    System.out.println("======================================");
    System.out.println("WARM UP: STARTING FOR MAILBOX TYPE: " + mailboxType);
    // warm up
    EndTime = 0;
    SingleOperationActor.totalValue = 0;
    for (int idx = 1; idx <= Max; ++idx) {
      actor.keep(idx);
    }
    System.out.println("WARM UP: SENT ALL, WAITING FOR COMPLETION");
    
    while (EndTime == 0) {
      Thread.sleep(100L);
    }
    
    //======================================
    
    System.out.println("SPEED TEST: START FOR MAILBOX TYPE: " + mailboxType);
    
    // speed test
    EndTime = 0;
    SingleOperationActor.totalValue = 0;
    StartTime = System.currentTimeMillis();
    for (int idx = 1; idx <= Max; ++idx) {
      actor.keep(idx);
    }

    while (EndTime == 0) {
      Thread.sleep(500L);
    }
    
    final long totalTime = EndTime - StartTime;
    final long totalSeconds = totalTime / 1000;
    
    System.out.println("SPEED TEST: ENDED FOR MAILBOX TYPE: " + mailboxType);
    System.out.println("          TOTAL TIME: " + totalTime);
    System.out.println(" MESSAGES PER SECOND: " + (Max  / totalSeconds));
  }
  
  public static interface SingleOperation {
    void keep(final int value);
  }
  
  public static class SingleOperationActor extends Actor implements SingleOperation {
    public static int totalValue;
    
    public SingleOperationActor() { }
    
    @Override
    public void keep(final int value) {
      totalValue = value;
      if (value == Max) EndTime = System.currentTimeMillis();
    }
  }
}
