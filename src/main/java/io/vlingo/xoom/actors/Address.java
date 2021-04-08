// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public interface Address extends Comparable<Address> {
  static Actor NoActor = new Actor() {};

  long id();
  long idSequence();
  String idSequenceString();
  String idString();
  <T> T idTyped();
  String name();
  boolean isDistributable();
}
