// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.Properties;

/**
 * Basic Mailbox configuration.
 * 
 * <p>See the <a href="https://docs.vlingo.io/xoom-actors#plugins">XOOM Actors Plugins</a> documentation.
 * 
 * @param <T> the T typed full configuration type
 */
public interface MailboxConfiguration<T> {
  /**
   * Sets the name of the mailbox.
   * @param name the String name of the mailbox
   * @return T
   */
  T name(final String name);

  /**
   * Sets the class name of the mailbox.
   * @param classname the String fully-qualified class name
   * @return T
   */
  T classname(final String classname);

  /**
   * Sets whether this is the default mailbox.
   * @param flag the boolean indicating a default or non-default mailbox
   * @return T
   */
  T defaultMailbox(final boolean flag);



  /**
   * Configuration for a kind of ManyToOneConcurrentArrayQueueMailbox.
   * 
   * <p>See the <a href="https://docs.vlingo.io/xoom-actors#plugins">XOOM Actors Plugins</a> documentation.
   */
  static interface ArrayQueue extends MailboxConfiguration<ArrayQueue> {
    /**
     * Sets the size of the array.
     * @param size the int size of the array
     * @return ArrayQueue
     */
    ArrayQueue size(final int size);

    /**
     * Sets the fixed back-off value.
     * @param amount the int amount of fixed back-off
     * @return ArrayQueue
     */
    ArrayQueue fixedBackoff(final int amount);

    /**
     * Sets the notify-on-send on or off. If not set the value is false.
     * @param flag the boolean on or off
     * @return ArrayQueue
     */
    ArrayQueue notifyOnSend(final boolean flag);

    /**
     * Sets the dispatcher throttling count.
     * @param count the int count
     * @return ArrayQueue
     */
    ArrayQueue dispatcherThrottlingCount(final int count);

    /**
     * Sets the send retries count.
     * @param retries the int number of retries on send
     * @return ArrayQueue
     */
    ArrayQueue sendRetires(final int retries);
  }

  /**
   * Configuration for a kind of ConcurrentQueueMailbox.
   * 
   * <p>See the <a href="https://docs.vlingo.io/xoom-actors#plugins">XOOM Actors Plugins</a> documentation.
   */
  static interface ConcurrentQueue extends MailboxConfiguration<ConcurrentQueue> {
    /**
     * Sets the number of dispatchers factor.
     * @param factor the double number of dispatchers factor
     * @return ConcurrentQueue
     */
    ConcurrentQueue numberOfDispatchersFactor(final double factor);

    /**
     * Sets my number of dispatchers.
     * @param dispatchers the int number of dispatchers
     * @return ConcurrentQueue
     */
    ConcurrentQueue numberOfDispatchers(final int dispatchers);

    /**
     * Sets the dispatcher throttling count.
     * @param count the int dispatcher throttling count
     * @return ConcurrentQueue
     */
    ConcurrentQueue dispatcherThrottlingCount(final int count);
  }

  /**
   * Configuration for a kind of SharedRingBufferMailbox.
   * 
   * <p>See the <a href="https://docs.vlingo.io/xoom-actors#plugins">XOOM Actors Plugins</a> documentation.
   */
  static interface SharedRingBuffer extends MailboxConfiguration<SharedRingBuffer> {
    /**
     * Sets the size of the array.
     * @param size the int size of the array
     * @return SharedRingBuffer
     */
    SharedRingBuffer size(final int size);

    /**
     * Sets the fixed back-off value.
     * @param amount the int amount of fixed back-off
     * @return SharedRingBuffer
     */
    SharedRingBuffer fixedBackoff(final int amount);

    /**
     * Sets the notify-on-send on or off. If not set the value is false.
     * @param flag the boolean on or off
     * @return SharedRingBuffer
     */
    SharedRingBuffer notifyOnSend(final boolean flag);

    /**
     * Sets the dispatcher throttling count.
     * @param count the int count
     * @return SharedRingBuffer
     */
    SharedRingBuffer dispatcherThrottlingCount(final int count);
  }

  //=========================================
  // Implementations
  //=========================================

  static abstract class BaseMailboxConfiguration<T> implements MailboxConfiguration<T> {
    protected String name;
    protected Properties properties;

    @Override
    @SuppressWarnings("unchecked")
    public T name(final String name) {
      this.properties = new Properties();
      this.name = "plugin.name." + name;
      properties.setProperty(this.name, "true");

      return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T classname(final String classname) {
      properties.setProperty(this.name + ".classname", "");

      return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T defaultMailbox(final boolean flag) {
      properties.setProperty(this.name + ".defaultMailbox", Boolean.toString(flag));

      return (T) this;
    }
  }

  static final class ArrayQueueConfiguration extends BaseMailboxConfiguration<ArrayQueue> implements ArrayQueue {

    @Override
    public ArrayQueue size(final int size) {
      properties.setProperty(this.name + ".size", Integer.toString(size));

      return this;
    }

    @Override
    public ArrayQueue fixedBackoff(final int amount) {
      properties.setProperty(this.name + ".fixedBackoff", Integer.toString(amount));

      return this;
    }

    @Override
    public ArrayQueue notifyOnSend(final boolean flag) {
      properties.setProperty(this.name + ".notifyOnSend", Boolean.toString(flag));

      return this;
    }

    @Override
    public ArrayQueue dispatcherThrottlingCount(final int count) {
      properties.setProperty(this.name + ".dispatcherThrottlingCount", Integer.toString(count));

      return this;
    }

    @Override
    public ArrayQueue sendRetires(final int retries) {
      properties.setProperty(this.name + ".sendRetires", Integer.toString(retries));

      return this;
    }
  }
  
  static final class ConcurrentQueueConfiguration extends BaseMailboxConfiguration<ConcurrentQueue> implements ConcurrentQueue {

    @Override
    public ConcurrentQueue numberOfDispatchersFactor(final double factor) {
      properties.setProperty(this.name + ".numberOfDispatchersFactor", Double.toString(factor));

      return this;
    }

    @Override
    public ConcurrentQueue numberOfDispatchers(final int dispatchers) {
      properties.setProperty(this.name + ".numberOfDispatchers", Integer.toString(dispatchers));

      return this;
    }

    @Override
    public ConcurrentQueue dispatcherThrottlingCount(final int count) {
      properties.setProperty(this.name + ".dispatcherThrottlingCount", Integer.toString(count));

      return this;
    }
  }

  static final class SharedRingBufferConfiguration extends BaseMailboxConfiguration<SharedRingBuffer> implements SharedRingBuffer {

    @Override
    public SharedRingBuffer size(final int size) {
      properties.setProperty(this.name + ".size", Integer.toString(size));

      return this;
    }

    @Override
    public SharedRingBuffer fixedBackoff(final int amount) {
      properties.setProperty(this.name + ".fixedBackoff", Integer.toString(amount));

      return this;
    }

    @Override
    public SharedRingBuffer notifyOnSend(final boolean flag) {
      properties.setProperty(this.name + ".notifyOnSend", Boolean.toString(flag));

      return this;
    }

    @Override
    public SharedRingBuffer dispatcherThrottlingCount(final int count) {
      properties.setProperty(this.name + ".dispatcherThrottlingCount", Integer.toString(count));

      return this;
    }
  }
}
