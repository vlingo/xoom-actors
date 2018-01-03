// Copyright 2012-2018 For Comprehension, Inc.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public interface DeadLetters extends Stoppable {
  void failedDelivery(final DeadLetter deadLetter);
  void registerListener(final DeadLettersListener listener);
}
