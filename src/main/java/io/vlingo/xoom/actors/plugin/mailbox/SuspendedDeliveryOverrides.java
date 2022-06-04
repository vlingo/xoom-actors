package io.vlingo.xoom.actors.plugin.mailbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SuspendedDeliveryOverrides {
  private final AtomicBoolean accessible;
  private final List<Overrides> overrides;

  public SuspendedDeliveryOverrides() {
    this.accessible = new AtomicBoolean(false);
    this.overrides = new ArrayList<>(0);
  }

  public boolean isEmpty() {
    return overrides.isEmpty();
  }

  public List<Overrides> find(final String name) {
    int retries = 0;
    while (true) {
      if (accessible.compareAndSet(false, true)) {
        List<Overrides> overridesNamed = this.overrides.stream()
            .filter(o -> o.name.equals(name))
            .collect(Collectors.toCollection(ArrayList::new));

        accessible.set(false);
        return overridesNamed;
      } else {
        if (++retries > 100_000_000) {
          (new Exception()).printStackTrace();
          return Collections.emptyList();
        }
      }
    }
  }

  public boolean matchesTop(final Class<?> messageType) {
    final Overrides overrides = peek();
    if (overrides != null) {
      for (final Class<?> type : overrides.types) {
        if (messageType == type) {
          return true;
        }
      }
    }
    return false;
  }

  public Overrides peek() {
    int retries = 0;
    while (true) {
      if (accessible.compareAndSet(false, true)) {
        Overrides temp = null;
        if (!isEmpty()) {
          temp = overrides.get(0);
        }
        accessible.set(false);
        return temp;
      } else {
        if (++retries > 100_000_000) {
          (new Exception()).printStackTrace();
          return null;
        }
      }
    }
  }

  public boolean pop(final String name) {
    boolean popped = false;
    int retries = 0;
    while (true) {
      if (accessible.compareAndSet(false, true)) {
        int elements = overrides.size();
        for (int index = 0; index < elements; ++index) {
          if (name.equals(overrides.get(index).name)) {
            if (index == 0) {
              overrides.remove(index);
              popped = true;
              --elements;
              while (index < elements) {
                if (overrides.get(index).obsolete) {
                  overrides.remove(index);
                  --elements;
                } else {
                  break;
                }
              }
            } else {
              overrides.get(index).obsolete = true;
            }
            accessible.set(false);
            break;
          }
        }
        break;
      } else {
        if (++retries > 100_000_000) {
          (new Exception()).printStackTrace();
          return false;
        }
      }

    }
    return popped;
  }

  public void push(final Overrides overrides) {
    int retries = 0;
    while (true) {
      if (accessible.compareAndSet(false, true)) {
        this.overrides.add(overrides);
        accessible.set(false);
        break;
      } else {
        if (++retries > 100_000_000) {
          (new Exception()).printStackTrace();
          return;
        }
      }
    }
  }

  public static class Overrides {
    final String name;
    boolean obsolete;
    final Class<?>[] types;

    public Overrides(final String name, final Class<?>[] types) {
      this.name = name;
      this.types = types;
      this.obsolete = false;
    }
  }
}
