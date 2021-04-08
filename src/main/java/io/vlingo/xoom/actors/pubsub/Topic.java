// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.pubsub;

public abstract class Topic {

    private final String name;

    public Topic(final String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public abstract boolean isSubTopic(final Topic anotherTopic);

    public boolean equals(final Object other) {

        if(other == null || !other.getClass().equals(getClass())) {
            return false;
        }

        final Topic otherTopic = (Topic) other;

        if(!this.name.equals(otherTopic.name())) {
            return false;
        }

        return true;
    }
}