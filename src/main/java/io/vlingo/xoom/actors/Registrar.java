// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public interface Registrar {
  void register(final String name, final CompletesEventuallyProvider completesEventuallyProvider);
  void register(final String name, final boolean isDefault, final LoggerProvider loggerProvider);
  void register(final String name, final boolean isDefault, final MailboxProvider mailboxProvider);

  void registerCommonSupervisor(final String stageName, final String name, final Class<?> supervisedProtocol, final Class<? extends Actor> supervisorClass);
  void registerDefaultSupervisor(final String stageName, final String name, final Class<? extends Actor> supervisorClass);
  void registerCompletesEventuallyProviderKeeper(final CompletesEventuallyProviderKeeper keeper);
  void registerLoggerProviderKeeper(final LoggerProviderKeeper keeper);
  void registerMailboxProviderKeeper(final MailboxProviderKeeper keeper);

  World world();
}
