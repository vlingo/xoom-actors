// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public interface AddressFactory {
  <T> Address findableBy(final T id);
  Address from(final long reservedId, final String name);
  Address from(final String idString);
  Address from(final String idString, final String name);
  Address none();
  Address unique();
  Address uniquePrefixedWith(final String prefixedWith);
  Address uniqueWith(final String name);
  Address withHighId();
  Address withHighId(final String name);
  long testNextIdValue();
}
