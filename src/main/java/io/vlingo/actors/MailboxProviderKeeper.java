// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.HashMap;
import java.util.Map;

final class MailboxProviderKeeper {
  private final Map<String,MailboxProviderInfo> mailboxProviderInfos;

  MailboxProviderKeeper() {
    this.mailboxProviderInfos = new HashMap<>();
  }

  Mailbox assignMailbox(final String name, final int hashCode) {
    MailboxProviderInfo info = mailboxProviderInfos.get(name);

    if (info == null) {
      throw new IllegalStateException("No registered MailboxProvider named " + name);
    }

    return info.mailboxProvider.provideMailboxFor(hashCode);
  }

  void close() {
    for (final MailboxProviderInfo info : mailboxProviderInfos.values()) {
      info.mailboxProvider.close();
    }
  }

  String findDefault() {
    for (final MailboxProviderInfo info : mailboxProviderInfos.values()) {
      if (info.isDefault) {
        return info.name;
      }
    }

    throw new IllegalStateException("No registered default MailboxProvider.");
  }

  void keep(final String name, boolean isDefault, final MailboxProvider mailboxProvider) {
    if (mailboxProviderInfos.isEmpty()) {
      isDefault = true;
    }
    
    if (isDefault) {
      undefaultCurrentDefault();
    }

    mailboxProviderInfos.put(name, new MailboxProviderInfo(name, mailboxProvider, isDefault));
  }

  void undefaultCurrentDefault() {
    for (final String key : mailboxProviderInfos.keySet()) {
      final MailboxProviderInfo info = mailboxProviderInfos.get(key);

      if (info.isDefault) {
        mailboxProviderInfos.put(key, new MailboxProviderInfo(info.name, info.mailboxProvider, false));
      }
    }
  }

  boolean isValidMailboxName(final String candidateMailboxName) {
    final MailboxProviderInfo info = mailboxProviderInfos.get(candidateMailboxName);

    return info != null;
  }

  final class MailboxProviderInfo {
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
