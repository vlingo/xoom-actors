package io.vlingo.actors;

final class NoOpLogger implements Logger {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void log(final String message) {
    }
}
