// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public interface SupervisionStrategy {
  public static enum Scope { All, One }
  
  public static final int DefaultIntensity = 1;
  public static final int ForeverIntensity = -1;
  
  public static final long DefaultPeriod = 5000;
  public static final long ForeverPeriod = Long.MAX_VALUE;
  
  int intensity();
  long period();
  Scope scope();
}
