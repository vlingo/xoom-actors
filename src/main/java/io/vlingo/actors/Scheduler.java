// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
  private final Timer timer;
  
  public Cancellable schedule(final Scheduled scheduled, final Object data, final long delayBefore, final long interval) {
    final SchedulerTask schedulerTask = new SchedulerTask(scheduled, data, true);
    timer.schedule(schedulerTask, delayBefore, interval);
    return schedulerTask;
  }

  public Cancellable scheduleOnce(final Scheduled scheduled, final Object data, final long delayBefore, final long interval) {
    final SchedulerTask schedulerTask = new SchedulerTask(scheduled, data, false);
    timer.schedule(schedulerTask, delayBefore + interval);
    return schedulerTask;
  }

  Scheduler() {
    this.timer = new Timer();
  }

  void close() {
    timer.cancel();
  }

  private class SchedulerTask extends TimerTask implements Cancellable {
    private final Scheduled scheduled;
    private final Object data;
    private final boolean repeats;
    
    SchedulerTask(final Scheduled scheduled, final Object data, final boolean repeats) {
      this.scheduled = scheduled;
      this.data = data;
      this.repeats = repeats;
    }
    
    @Override
    public void run() {
      scheduled.intervalSignal(scheduled, data);
      
      if (!repeats) {
        cancel();
      }
    }
  }
}
