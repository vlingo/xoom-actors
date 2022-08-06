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
   * Answer myself after setting my name of the mailbox.
   * @param name the String name of the mailbox
   * @return T
   */
  T mailboxName(final String name);

  /**
   * Answer myself after setting my {@code mailboxImplementationClassname}.
   * @param classname the String fully-qualified class name
   * @return T
   */
  T mailboxImplementationClassname(final String classname);

  /**
   * Answer myself after setting my defaultMailbox.
   * @param defaultMailbox the boolean indicating a default or non-default mailbox
   * @return T
   */
  T defaultMailbox(final boolean defaultMailbox);

  /**
   * Answer the configuration as a Properties.
   * @return Properties
   */
  Properties toProperties();


  /**
   * Configuration for a kind of ManyToOneConcurrentArrayQueueMailbox.
   * 
   * <p>See the <a href="https://docs.vlingo.io/xoom-actors#plugins">XOOM Actors Plugins</a> documentation.
   */
  static interface ArrayQueue extends MailboxConfiguration<ArrayQueue> {
    /**
     * Answer myself after setting my size.
     * @param size the int size of my internal array
     * @return ArrayQueue
     */
    ArrayQueue size(final int size);

    /**
     * Answer myself after setting fixedBackoff.
     * @param fixedBackoff the int count of fixed back-off
     * @return ArrayQueue
     */
    ArrayQueue fixedBackoff(final int fixedBackoff);

    /**
     * Answer myself after setting my notifyOnSend. If not set the value is false.
     * @param notifyOnSend the boolean on or off
     * @return ArrayQueue
     */
    ArrayQueue notifyOnSend(final boolean notifyOnSend);

    /**
     * Answer myself after setting my dispatcherThrottlingCount.
     * @param dispatcherThrottlingCount the int to set as my dispatcherThrottlingCount
     * @return ArrayQueue
     */
    ArrayQueue dispatcherThrottlingCount(final int dispatcherThrottlingCount);

    /**
     * Answer myself after setting my sendRetires.
     * @param sendRetires the int number of retries on send
     * @return ArrayQueue
     */
    ArrayQueue sendRetires(final int sendRetires);
  }

  /**
   * Configuration for a kind of ConcurrentQueueMailbox.
   * 
   * <p>See the <a href="https://docs.vlingo.io/xoom-actors#plugins">XOOM Actors Plugins</a> documentation.
   */
  static interface ConcurrentQueue extends MailboxConfiguration<ConcurrentQueue> {
    /**
     * Answer myself after setting my numberOfDispatchersFactor.
     * @param numberOfDispatchersFactor the double number of dispatchers factor
     * @return ConcurrentQueue
     */
    ConcurrentQueue numberOfDispatchersFactor(final double numberOfDispatchersFactor);

    /**
     * Answer myself after setting my numberOfDispatchers.
     * @param numberOfDispatchers the int number of dispatchers
     * @return ConcurrentQueue
     */
    ConcurrentQueue numberOfDispatchers(final int numberOfDispatchers);

    /**
     * Answer myself after setting my dispatcherThrottlingCount.
     * @param dispatcherThrottlingCount the int dispatcher throttling count
     * @return ConcurrentQueue
     */
    ConcurrentQueue dispatcherThrottlingCount(final int dispatcherThrottlingCount);
  }

  /**
   * Configuration for a kind of SharedRingBufferMailbox.
   * 
   * <p>See the <a href="https://docs.vlingo.io/xoom-actors#plugins">XOOM Actors Plugins</a> documentation.
   */
  static interface SharedRingBuffer extends MailboxConfiguration<SharedRingBuffer> {
    /**
     * Answer myself after setting my size.
     * @param size the int size of my internal array
     * @return SharedRingBuffer
     */
    SharedRingBuffer size(final int size);

    /**
     * Answer myself after setting my fixedBackoff.
     * @param fixedBackoff the int fixedBackoff
     * @return SharedRingBuffer
     */
    SharedRingBuffer fixedBackoff(final int fixedBackoff);

    /**
     * Answer myself after setting my notifyOnSend. If not set the value is false.
     * @param notifyOnSend the boolean on or off
     * @return SharedRingBuffer
     */
    SharedRingBuffer notifyOnSend(final boolean notifyOnSend);

    /**
     * Answer myself after setting my dispatcherThrottlingCount.
     * @param dispatcherThrottlingCount the int dispatcherThrottlingCount
     * @return SharedRingBuffer
     */
    SharedRingBuffer dispatcherThrottlingCount(final int dispatcherThrottlingCount);
  }

  //=========================================
  // Implementations
  //=========================================

  static abstract class BaseMailboxConfiguration<T> implements MailboxConfiguration<T> {
	private boolean defaultMailbox;
    private String mailboxImplementationClassname;
    private String pluginName;

	protected String mailboxName;

    @Override
    @SuppressWarnings("unchecked")
    public T mailboxName(final String mailboxName) {
      this.mailboxName = mailboxName;
      
      return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T mailboxImplementationClassname(final String classname) {
    	this.mailboxImplementationClassname = classname;

      return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T defaultMailbox(final boolean defaultMailbox) {
      this.defaultMailbox = defaultMailbox;

      return (T) this;
    }

    @Override
    public Properties toProperties() {
      final Properties properties = new Properties();

      properties.setProperty(pluginName(), "true");
      properties.setProperty(pluginName() + ".classname", mailboxImplementationClassname);
      properties.setProperty(pluginName() + ".defaultMailbox", Boolean.toString(defaultMailbox));

      return properties;
    }

    protected String pluginName() {
      if (pluginName == null) {
        pluginName = "plugin.name." + this.mailboxName;
      }

      return pluginName;
    }
  }

  static final class ArrayQueueConfiguration extends BaseMailboxConfiguration<ArrayQueue> implements ArrayQueue {
    private int fixedBackoff;
    private boolean notifyOnSend;
    private int sendRetires;
    private int size;
    private int dispatcherThrottlingCount;

    @Override
    public ArrayQueue size(final int size) {
      this.size = size;

      return this;
    }

    @Override
    public ArrayQueue fixedBackoff(final int fixedBackoff) {
      this.fixedBackoff = fixedBackoff;

      return this;
    }

    @Override
    public ArrayQueue notifyOnSend(final boolean notifyOnSend) {
      this.notifyOnSend = notifyOnSend;

      return this;
    }

    @Override
    public ArrayQueue dispatcherThrottlingCount(final int dispatcherThrottlingCount) {
      this.dispatcherThrottlingCount = dispatcherThrottlingCount;

      return this;
    }

    @Override
    public ArrayQueue sendRetires(final int sendRetires) {
      this.sendRetires = sendRetires;

      return this;
    }

    @Override
    public Properties toProperties() {
      final Properties properties = super.toProperties();

      properties.setProperty(pluginName() + ".size", Integer.toString(size));
      properties.setProperty(pluginName() + ".fixedBackoff", Integer.toString(fixedBackoff));
      properties.setProperty(pluginName() + ".notifyOnSend", Boolean.toString(notifyOnSend));
      properties.setProperty(pluginName() + ".dispatcherThrottlingCount", Integer.toString(dispatcherThrottlingCount));
      properties.setProperty(pluginName() + ".sendRetires", Integer.toString(sendRetires));

      return properties;
    }
  }
  
  static final class ConcurrentQueueConfiguration extends BaseMailboxConfiguration<ConcurrentQueue> implements ConcurrentQueue {
    private int dispatcherThrottlingCount;
    private int numberOfDispatchers;
    private double numberOfDispatchersFactor;

    @Override
    public ConcurrentQueue numberOfDispatchersFactor(final double numberOfDispatchersFactor) {
      this.numberOfDispatchersFactor = numberOfDispatchersFactor;

      return this;
    }

    @Override
    public ConcurrentQueue numberOfDispatchers(final int numberOfDispatchers) {
      this.numberOfDispatchers = numberOfDispatchers;

      return this;
    }

    @Override
    public ConcurrentQueue dispatcherThrottlingCount(final int dispatcherThrottlingCount) {
      this.dispatcherThrottlingCount = dispatcherThrottlingCount;

      return this;
    }

    @Override
    public Properties toProperties() {
      final Properties properties = super.toProperties();

      properties.setProperty(pluginName() + ".numberOfDispatchersFactor", Double.toString(numberOfDispatchersFactor));
      properties.setProperty(pluginName() + ".numberOfDispatchers", Integer.toString(numberOfDispatchers));
      properties.setProperty(pluginName() + ".dispatcherThrottlingCount", Integer.toString(dispatcherThrottlingCount));

      return properties;
    }
  }

  static final class SharedRingBufferConfiguration extends BaseMailboxConfiguration<SharedRingBuffer> implements SharedRingBuffer {
    private int fixedBackoff;
    private boolean notifyOnSend;
    private int size;
    private int dispatcherThrottlingCount;

    @Override
    public SharedRingBuffer size(final int size) {
      this.size = size;

      return this;
    }

    @Override
    public SharedRingBuffer fixedBackoff(final int fixedBackoff) {
      this.fixedBackoff = fixedBackoff;

      return this;
    }

    @Override
    public SharedRingBuffer notifyOnSend(final boolean notifyOnSend) {
      this.notifyOnSend = notifyOnSend;

      return this;
    }

    @Override
    public SharedRingBuffer dispatcherThrottlingCount(final int dispatcherThrottlingCount) {
      this.dispatcherThrottlingCount = dispatcherThrottlingCount;

      return this;
    }

    @Override
    public Properties toProperties() {
      final Properties properties = super.toProperties();

      properties.setProperty(pluginName() + ".size", Integer.toString(size));
      properties.setProperty(pluginName() + ".fixedBackoff", Integer.toString(fixedBackoff));
      properties.setProperty(pluginName() + ".notifyOnSend", Boolean.toString(notifyOnSend));
      properties.setProperty(pluginName() + ".dispatcherThrottlingCount", Integer.toString(dispatcherThrottlingCount));

      return properties;
    }
  }
}
