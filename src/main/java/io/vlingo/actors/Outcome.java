// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public abstract class Outcome<O> {
  private final O value;
  
  public Outcome(final O value) {
    this.value = value;
  }
  
  public O value() {
    return value;
  }
}
