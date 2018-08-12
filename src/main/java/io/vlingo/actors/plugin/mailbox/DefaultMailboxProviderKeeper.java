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
  private final Map<String,MailboxProviderInfo> mailboxProviderInfos;

  public DefaultMailboxProviderKeeper() {
    this.mailboxProviderInfos = new HashMap<>();
  }

  public Mailbox assignMailbox(final String name, final int hashCode) {
    MailboxProviderInfo info = mailboxProviderInfos.get(name);

    if (info == null) {
      throw new IllegalStateException("No registered MailboxProvider named " + name);
    }

    return info.mailboxProvider.provideMailboxFor(hashCode);
  }

  public void close() {
    for (final MailboxProviderInfo info : mailboxProviderInfos.values()) {
      info.mailboxProvider.close();
    }
  }

  public String findDefault() {
    for (final MailboxProviderInfo info : mailboxProviderInfos.values()) {
      if (info.isDefault) {
        return info.name;
      }
    }

    throw new IllegalStateException("No registered default MailboxProvider.");
  }

  public void keep(final String name, boolean isDefault, final MailboxProvider mailboxProvider) {
    if (mailboxProviderInfos.isEmpty()) {
      isDefault = true;
    }
    
    if (isDefault) {
      undefaultCurrentDefault();
    }

    mailboxProviderInfos.put(name, new MailboxProviderInfo(name, mailboxProvider, isDefault));
  }

  public boolean isValidMailboxName(final String candidateMailboxName) {
    final MailboxProviderInfo info = mailboxProviderInfos.get(candidateMailboxName);

    return info != null;
  }

    private void undefaultCurrentDefault() {
        for (final String key : mailboxProviderInfos.keySet()) {
            final MailboxProviderInfo info = mailboxProviderInfos.get(key);

            if (info.isDefault) {
                mailboxProviderInfos.put(key, new MailboxProviderInfo(info.name, info.mailboxProvider, false));
            }
        }
    }

  private static final class MailboxProviderInfo {
    final boolean isDefault;
    final MailboxProvider mailboxProvider;
    final String name;

    MailboxProviderInfo(final String name, final MailboxProvider mailboxProvider, final boolean isDefault) {
      this.name = name;
      this.mailboxProvider = mailboxProvider;
      this.isDefault = isDefault;
    }
  }
}
