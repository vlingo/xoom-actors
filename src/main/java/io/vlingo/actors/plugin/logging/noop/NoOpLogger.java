// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.noop;

import io.vlingo.actors.Logger;

public class NoOpLogger implements Logger {

  @Override
  public void close() {
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public void trace(String message) {
  }

  @Override
  public void trace(String message, Object... args) {
  }

  @Override
  public void trace(String message, Throwable throwable) {
  }

  @Override
  public void debug(String message) {
  }

  @Override
  public void debug(String message, Object... args) {
  }

  @Override
  public void debug(String message, Throwable throwable) {
  }

  @Override
  public void info(String message) {
  }

  @Override
  public void info(String message, Object... args) {
  }

  @Override
  public void info(String message, Throwable throwable) {
  }

  @Override
  public void warn(String message) {
  }

  @Override
  public void warn(String message, Object... args) {
  }

  @Override
  public void warn(String message, Throwable throwable) {
  }

  @Override
  public void error(String message) {
  }

  @Override
  public void error(String message, Object... args) {
  }

  @Override
  public void error(String message, Throwable throwable) {
  }

  @Override
  public String name() {
    return "no-op";
  }
}
