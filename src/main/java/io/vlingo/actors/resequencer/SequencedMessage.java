// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.resequencer;

public class SequencedMessage {

    private final String correlationId;
    private final int index;
    private final int total;

    public static SequencedMessage of(final String correlationId, final int index, final int total) {
        return new SequencedMessage(correlationId, index, total);
    }

    public static SequencedMessage asNonIndexedMessage(final String correlationId, final int total) {
        return of(correlationId, -1, total);
    }

    private SequencedMessage(final String correlationId, final int index, final int total) {
        this.correlationId = correlationId;
        this.index = index;
        this.total = total;
    }

    public String correlationId() {
        return correlationId;
    }

    public int index() {
        return index;
    }

    public int total() {
        return total;
    }

    public boolean isIndexed() {
        return index >= 1;
    }
}
