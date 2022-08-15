// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.Properties;

import io.vlingo.xoom.actors.plugin.Plugin;
import io.vlingo.xoom.actors.plugin.PluginConfiguration;
import io.vlingo.xoom.actors.plugin.PluginProperties;
import io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin;
import io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin.ManyToOneConcurrentArrayQueuePluginConfiguration;
import io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin;
import io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin.ConcurrentQueueMailboxPluginConfiguration;
import io.vlingo.xoom.actors.plugin.mailbox.sharedringbuffer.SharedRingBufferMailboxPlugin;
import io.vlingo.xoom.actors.plugin.mailbox.sharedringbuffer.SharedRingBufferMailboxPlugin.SharedRingBufferMailboxPluginConfiguration;

/**
 * Basic Mailbox configuration.
 * 
 * <p>See the <a href="https://docs.vlingo.io/xoom-actors#plugins">XOOM Actors Plugins</a> documentation.
 * 
 * <p>Usage:
 * <ul>
 *   <li>{@code ArrayQueueConfiguration configuration = MailboxConfiguration.arrayQueueConfiguration();}</li>
 *   <li>{@code ConcurrentQueueConfiguration configuration = MailboxConfiguration.concurrentQueueConfiguration();}</li>
 *   <li>{@code SharedRingBufferConfiguration configuration = MailboxConfiguration.sharedRingBufferConfiguration();}</li>
 * </ul>
 * 
 * @param <T> the T typed full configuration type
 */
public interface MailboxConfiguration<T> {

  /**
   * Answer a new instance of {@code ArrayQueueConfiguration}.
   * @return ArrayQueueConfiguration
   */
  static ArrayQueueConfiguration arrayQueueConfiguration() {
    return new BasicArrayQueueConfiguration();
  }

  /**
   * Answer a new instance of {@code ConcurrentQueueConfiguration}.
   * @return ConcurrentQueueConfiguration
   */
  static ConcurrentQueueConfiguration concurrentQueueConfiguration() {
    return new BasicConcurrentQueueConfiguration();
  }

  /**
   * Answer a new instance of {@code SharedRingBufferConfiguration}.
   * @return SharedRingBufferConfiguration
   */
  static SharedRingBufferConfiguration sharedRingBufferConfiguration() {
    return new BasicSharedRingBufferConfiguration();
  }

  /**
   * Answer my {@code Configuration}, which is independent
   * of the {@code World} registered {@code Configuration}.
   * @return Configuration
   */
  Configuration configuration();

  /** Answer my {@code mailboxName}.
   * @return String
   */
  String mailboxName();

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
   * Answer my {@code defaultMailbox}.
   * @return boolean
   */
  boolean isDefaultMailbox();

  /**
   * Answer myself after setting my defaultMailbox.
   * @param defaultMailbox the boolean indicating a default or non-default mailbox
   * @return T
   */
  T defaultMailbox(final boolean defaultMailbox);

  /**
   * Answer my {@code MailboxProvider}.
   * @return MailboxProvider
   */
  MailboxProvider mailboxProvider();

  /**
   * Answer my {@code Plugin}.
   * @return Plugin
   */
  Plugin plugin();

  /**
   * Answer my {@code PluginConfiguration}.
   * @return PluginConfiguration
   */
  PluginConfiguration pluginConfiguration();

  /**
   * Answer my {@code PluginConfiguration}.
   * @return PluginConfiguration
   */
  PluginProperties pluginProperties();

  /**
   * Answer my configuration as a Properties.
   * @return Properties
   */
  Properties toProperties();


  /**
   * Configuration for a kind of ManyToOneConcurrentArrayQueueMailbox.
   * 
   * <p>See the <a href="https://docs.vlingo.io/xoom-actors#plugins">XOOM Actors Plugins</a> documentation.
   */
  static interface ArrayQueueConfiguration extends MailboxConfiguration<ArrayQueueConfiguration> {
    /**
     * Answer myself after setting my size.
     * @param size the int size of my internal array
     * @return ArrayQueue
     */
    ArrayQueueConfiguration size(final int size);

    /**
     * Answer myself after setting fixedBackoff.
     * @param fixedBackoff the int count of fixed back-off
     * @return ArrayQueue
     */
    ArrayQueueConfiguration fixedBackoff(final int fixedBackoff);

    /**
     * Answer myself after setting my notifyOnSend. If not set the value is false.
     * @param notifyOnSend the boolean on or off
     * @return ArrayQueue
     */
    ArrayQueueConfiguration notifyOnSend(final boolean notifyOnSend);

    /**
     * Answer myself after setting my dispatcherThrottlingCount.
     * @param dispatcherThrottlingCount the int to set as my dispatcherThrottlingCount
     * @return ArrayQueue
     */
    ArrayQueueConfiguration dispatcherThrottlingCount(final int dispatcherThrottlingCount);

    /**
     * Answer myself after setting my sendRetires.
     * @param sendRetires the int number of retries on send
     * @return ArrayQueue
     */
    ArrayQueueConfiguration sendRetires(final int sendRetires);
  }

  /**
   * Configuration for a kind of ConcurrentQueueMailbox.
   * 
   * <p>See the <a href="https://docs.vlingo.io/xoom-actors#plugins">XOOM Actors Plugins</a> documentation.
   */
  static interface ConcurrentQueueConfiguration extends MailboxConfiguration<ConcurrentQueueConfiguration> {
    /**
     * Answer myself after setting my numberOfDispatchersFactor.
     * @param numberOfDispatchersFactor the double number of dispatchers factor
     * @return ConcurrentQueue
     */
    ConcurrentQueueConfiguration numberOfDispatchersFactor(final double numberOfDispatchersFactor);

    /**
     * Answer myself after setting my numberOfDispatchers.
     * @param numberOfDispatchers the int number of dispatchers
     * @return ConcurrentQueue
     */
    ConcurrentQueueConfiguration numberOfDispatchers(final int numberOfDispatchers);

    /**
     * Answer myself after setting my dispatcherThrottlingCount.
     * @param dispatcherThrottlingCount the int dispatcher throttling count
     * @return ConcurrentQueue
     */
    ConcurrentQueueConfiguration dispatcherThrottlingCount(final int dispatcherThrottlingCount);
  }

  /**
   * Configuration for a kind of SharedRingBufferMailbox.
   * 
   * <p>See the <a href="https://docs.vlingo.io/xoom-actors#plugins">XOOM Actors Plugins</a> documentation.
   */
  static interface SharedRingBufferConfiguration extends MailboxConfiguration<SharedRingBufferConfiguration> {
    /**
     * Answer myself after setting my size.
     * @param size the int size of my internal array
     * @return SharedRingBuffer
     */
    SharedRingBufferConfiguration size(final int size);

    /**
     * Answer myself after setting my fixedBackoff.
     * @param fixedBackoff the int fixedBackoff
     * @return SharedRingBuffer
     */
    SharedRingBufferConfiguration fixedBackoff(final int fixedBackoff);

    /**
     * Answer myself after setting my notifyOnSend. If not set the value is false.
     * @param notifyOnSend the boolean on or off
     * @return SharedRingBuffer
     */
    SharedRingBufferConfiguration notifyOnSend(final boolean notifyOnSend);

    /**
     * Answer myself after setting my dispatcherThrottlingCount.
     * @param dispatcherThrottlingCount the int dispatcherThrottlingCount
     * @return SharedRingBuffer
     */
    SharedRingBufferConfiguration dispatcherThrottlingCount(final int dispatcherThrottlingCount);
  }

  //=========================================
  // Implementations
  //=========================================

  static abstract class BaseMailboxConfiguration<T> implements MailboxConfiguration<T> {
    private Configuration configuration;
    private boolean defaultMailbox;
    private String mailboxImplementationClassname;
    private String pluginName;

    protected String mailboxName;
    protected Plugin plugin;
    protected PluginConfiguration pluginConfiguration;
    protected PluginProperties pluginProperties;

    protected BaseMailboxConfiguration() {
      this.configuration = Configuration.define();
    }

    @Override
    public Configuration configuration() {
      return configuration;
    }

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
    public boolean isDefaultMailbox() {
      return defaultMailbox;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T defaultMailbox(final boolean defaultMailbox) {
      this.defaultMailbox = defaultMailbox;

      return (T) this;
    }

    @Override
    public String mailboxName() {
      return mailboxName;
    }

    @Override
    public MailboxProvider mailboxProvider() {
      return (MailboxProvider) plugin();
    }

    @Override
    public PluginProperties pluginProperties() {
      if (pluginProperties == null) {
        pluginProperties = new PluginProperties(mailboxName, toProperties());
      }

      return pluginProperties;
    }

    @Override
    public Properties toProperties() {
      final Properties properties = new Properties();

      properties.setProperty(pluginDeclaration(), "true");
      properties.setProperty(mailboxName, mailboxImplementationClassname);
      properties.setProperty(pluginName(), "true");
      properties.setProperty(pluginName() + ".classname", mailboxImplementationClassname);
      properties.setProperty(pluginName() + ".defaultMailbox", Boolean.toString(defaultMailbox));

      return properties;
    }

    protected String pluginDeclaration() {
      return "plugin.name." + this.mailboxName;
    }

    protected String pluginName() {
      if (pluginName == null) {
        pluginName = "plugin." + this.mailboxName;
      }

      return pluginName;
    }

    @SuppressWarnings("unchecked")
    protected <PC> PC typedPluginConfiguration() {
      return (PC) pluginConfiguration();
    }
  }

  static final class BasicArrayQueueConfiguration extends BaseMailboxConfiguration<ArrayQueueConfiguration> implements ArrayQueueConfiguration {
    private int fixedBackoff;
    private boolean notifyOnSend;
    private int sendRetires;
    private int size;
    private int dispatcherThrottlingCount;

    @Override
    public ArrayQueueConfiguration size(final int size) {
      this.size = size;

      return this;
    }

    @Override
    public ArrayQueueConfiguration fixedBackoff(final int fixedBackoff) {
      this.fixedBackoff = fixedBackoff;

      return this;
    }

    @Override
    public ArrayQueueConfiguration notifyOnSend(final boolean notifyOnSend) {
      this.notifyOnSend = notifyOnSend;

      return this;
    }

    @Override
    public ArrayQueueConfiguration dispatcherThrottlingCount(final int dispatcherThrottlingCount) {
      this.dispatcherThrottlingCount = dispatcherThrottlingCount;

      return this;
    }

    @Override
    public ArrayQueueConfiguration sendRetires(final int sendRetires) {
      this.sendRetires = sendRetires;

      return this;
    }

    @Override
    public Plugin plugin() {
      if (plugin == null) {
        plugin = new ManyToOneConcurrentArrayQueuePlugin(typedPluginConfiguration());
      }

      return plugin;
    }

    @Override
    public PluginConfiguration pluginConfiguration() {
      if (pluginConfiguration == null) {
        pluginConfiguration = ManyToOneConcurrentArrayQueuePluginConfiguration.define();
        pluginConfiguration.buildWith(configuration(), pluginProperties());
      }

      return pluginConfiguration;
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
  
  static final class BasicConcurrentQueueConfiguration extends BaseMailboxConfiguration<ConcurrentQueueConfiguration> implements ConcurrentQueueConfiguration {
    private int dispatcherThrottlingCount;
    private int numberOfDispatchers;
    private double numberOfDispatchersFactor;

    @Override
    public ConcurrentQueueConfiguration numberOfDispatchersFactor(final double numberOfDispatchersFactor) {
      this.numberOfDispatchersFactor = numberOfDispatchersFactor;

      return this;
    }

    @Override
    public ConcurrentQueueConfiguration numberOfDispatchers(final int numberOfDispatchers) {
      this.numberOfDispatchers = numberOfDispatchers;

      return this;
    }

    @Override
    public ConcurrentQueueConfiguration dispatcherThrottlingCount(final int dispatcherThrottlingCount) {
      this.dispatcherThrottlingCount = dispatcherThrottlingCount;

      return this;
    }

    @Override
    public Plugin plugin() {
      if (plugin == null) {
        plugin = new ConcurrentQueueMailboxPlugin(typedPluginConfiguration());
      }

      return plugin;
    }

    @Override
    public PluginConfiguration pluginConfiguration() {
      if (pluginConfiguration == null) {
        pluginConfiguration = ConcurrentQueueMailboxPluginConfiguration.define();
        pluginConfiguration.buildWith(configuration(), pluginProperties());
      }

      return pluginConfiguration;
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

  static final class BasicSharedRingBufferConfiguration extends BaseMailboxConfiguration<SharedRingBufferConfiguration> implements SharedRingBufferConfiguration {
    private int fixedBackoff;
    private boolean notifyOnSend;
    private int size;
    private int dispatcherThrottlingCount;

    @Override
    public SharedRingBufferConfiguration size(final int size) {
      this.size = size;

      return this;
    }

    @Override
    public SharedRingBufferConfiguration fixedBackoff(final int fixedBackoff) {
      this.fixedBackoff = fixedBackoff;

      return this;
    }

    @Override
    public SharedRingBufferConfiguration notifyOnSend(final boolean notifyOnSend) {
      this.notifyOnSend = notifyOnSend;

      return this;
    }

    @Override
    public SharedRingBufferConfiguration dispatcherThrottlingCount(final int dispatcherThrottlingCount) {
      this.dispatcherThrottlingCount = dispatcherThrottlingCount;

      return this;
    }

    @Override
    public Plugin plugin() {
      if (plugin == null) {
        plugin = new SharedRingBufferMailboxPlugin(typedPluginConfiguration());
      }

      return plugin;
    }

    @Override
    public PluginConfiguration pluginConfiguration() {
      if (pluginConfiguration == null) {
        pluginConfiguration = SharedRingBufferMailboxPluginConfiguration.define();
        pluginConfiguration.buildWith(configuration(), pluginProperties());
      }

      return pluginConfiguration;
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
