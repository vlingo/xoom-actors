// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.supervision;

import io.vlingo.actors.Actor;

class ConfiguredSupervisor {
  public final String stageName;
  public final Class<? extends Actor> supervisorClass;
  public final String supervisorName;
  public final Class<?> supervisedProtocol;

  ConfiguredSupervisor(final String stageName, final String supervisorName, final Class<?> supervisedProtocol, final Class<? extends Actor> supervisorClass) {
    this.stageName = stageName;
    this.supervisorName = supervisorName;
    this.supervisedProtocol = supervisedProtocol;
    this.supervisorClass = supervisorClass;
  }

  ConfiguredSupervisor(final String stageName, final String supervisorName, final String supervisedProtocol, final String supervisorClassname) {
    this.stageName = stageName;
    this.supervisorName = supervisorName;
    this.supervisedProtocol = protocolFrom(supervisedProtocol);
    this.supervisorClass = supervisorFrom(supervisorClassname);
  }

  ConfiguredSupervisor(final String stageName, final String supervisorName, final Class<? extends Actor> supervisorClass) {
    this.stageName = stageName;
    this.supervisorName = supervisorName;
    this.supervisedProtocol = null;
    this.supervisorClass = supervisorClass;
  }

  ConfiguredSupervisor(final String stageName, final String supervisorName, final String supervisorClassname) {
    this.stageName = stageName;
    this.supervisorName = supervisorName;
    this.supervisedProtocol = null;
    this.supervisorClass = supervisorFrom(supervisorClassname);
  }

  private Class<?> protocolFrom(final String supervisedProtocol) {
    try {
      return Class.forName(supervisedProtocol);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot load class for: " + supervisedProtocol);
    }
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Actor> supervisorFrom(final String supervisorClassname) {
    try {
      return (Class<? extends Actor>) Class.forName(supervisorClassname);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot load class for: " + supervisorClassname);
    }
  }
}
