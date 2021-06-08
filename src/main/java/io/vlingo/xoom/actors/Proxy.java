// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

/**
 * Defines the interface for all Actor proxies. The Actor's {@code Address} is
 * available as well as {@code equals()}, {@code hashCode()}, and {@code toString()}.
 */
public interface Proxy {

  /**
   * Answer the {@code Proxy} instance for the {@code proxy}.
   * @param proxy the Object for which the Proxy instance will be obtained
   * @throws ClassCastException if proxy is not a Proxy
   * @return Proxy
   */
  static Proxy from(final Object proxy) {
    return Proxy.class.cast(proxy);
  }

  /**
   * Answer my underlying {@code Actor}'s {@code Address}.
   * @return Address
   */
  Address address();
}
