package io.vlingo.actors;

final class SystemOutLogger implements Logger {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void log(final String message) {
        System.out.println(message);
    }
}
