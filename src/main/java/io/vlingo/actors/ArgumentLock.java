package io.vlingo.actors;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.vlingo.common.Tuple2;

/**
 * based on the solution found on https://stackoverflow.com/a/28347825
 */
public final class ArgumentLock implements Lock {

  private static final Map<Object, Tuple2<WeakReference<Object>, Lock>> locks =
      new WeakHashMap<>();

  public static Lock acquire(Object param) {
    Object intern = null;
    synchronized (locks) {
      Tuple2<WeakReference<Object>, Lock> pair = locks.get(param);
      if (pair != null) {
        intern = pair._1.get();
      }
      if (intern == null) {
        intern = param;
        pair = Tuple2.from(new WeakReference<>(intern), new ReentrantLock());
        locks.put(intern, pair);
      }
    }

    return new ArgumentLock(intern, locks.get(intern)._2);
  }

  /**
   * We need a strong reference of the object here to make sure that the entry
   * in locks stays in place for as long as the ParameterLock instance
   * is referenced by clients.
   */
  @SuppressWarnings("unused")
  private final Object object;
  private final Lock lock;


  public ArgumentLock(Object object, Lock lock) {
    this.object = object;
    this.lock = lock;
  }


  @Override
  public void lock() {
    lock.lock();
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
    lock.lockInterruptibly();
  }

  @Override
  public boolean tryLock() {
    return lock.tryLock();
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    return lock.tryLock(time, unit);
  }

  @Override
  public void unlock() {
    lock.unlock();
  }

  @Override
  public Condition newCondition() {
    return lock.newCondition();
  }
}
