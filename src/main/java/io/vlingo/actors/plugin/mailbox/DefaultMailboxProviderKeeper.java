// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox;

import io.vlingo.actors.Mailbox;
import io.vlingo.actors.MailboxProvider;
import io.vlingo.actors.MailboxProviderKeeper;

import java.util.HashMap;
import java.util.Map;

public final class DefaultMailboxProviderKeeper implements MailboxProviderKeeper {
  private final Map<String, MailboxProviderInfo> mailboxProviderInfos;
  private MailboxProviderInfo defaultProvider;

  public DefaultMailboxProviderKeeper() {
    this.mailboxProviderInfos = new HashMap<>();
    this.defaultProvider = null;
  }

  public Mailbox assignMailbox(final String name, final int hashCode) {
    MailboxProviderInfo info = mailboxProviderInfos.get(name);

    if (info == null) {
      throw new IllegalStateException("No registered MailboxProvider named " + name);
    }

    return info.mailboxProvider.provideMailboxFor(hashCode);
  }

  public void close() {
    mailboxProviderInfos.values().forEach(info -> info.mailboxProvider.close());
  }

  public String findDefault() {
    if (defaultProvider == null) {
      throw new IllegalStateException("No registered default MailboxProvider.");
    }

    return defaultProvider.name;
  }

  public void keep(final String name, boolean isDefault, final MailboxProvider mailboxProvider) {
    MailboxProviderInfo providerInfo = new MailboxProviderInfo(name, mailboxProvider);

    mailboxProviderInfos.put(name, providerInfo);
    if (defaultProvider == null || isDefault) {
      defaultProvider = providerInfo;
    }
  }

  public boolean isValidMailboxName(final String candidateMailboxName) {
    return mailboxProviderInfos.containsKey(candidateMailboxName);
  }

  private static final class MailboxProviderInfo {
    final MailboxProvider mailboxProvider;
    final String name;

    MailboxProviderInfo(final String name, final MailboxProvider mailboxProvider) {
      this.name = name;
      this.mailboxProvider = mailboxProvider;
    }
  }
}
