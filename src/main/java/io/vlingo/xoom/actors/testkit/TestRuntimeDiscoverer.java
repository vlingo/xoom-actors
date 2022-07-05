package io.vlingo.xoom.actors.testkit;

/**
 * Discover whether the runtime is currently under test.
 */
public class TestRuntimeDiscoverer {

  /**
   * Answer whether the runtime is testing.
   * 
   * @return boolean
   */
  public static boolean isUnderTest() {
    final Exception exception = new Exception();

    boolean junitStack = false;
    int junitCalls = 0;

    for (final StackTraceElement element : exception.getStackTrace()) {
      final String className = element.getClassName();

      if (className.startsWith("org.junit.")) {
        junitStack = true;
        ++junitCalls;
      } else if (junitStack) {
        junitStack = false;
        break;
      }
    }

    // generally there are many consecutive junit calls,
    // such as +-18. Figure that if there are at least 5
    // consecutive junit calls, it's definitely a test.

    return junitCalls >= 5;
  }

  /**
   * Answer whether the class of {@code className} with method of {@code methodName} is
   * a test method. If this method were implemented there could be multiple discovery
   * approaches. Consider, in priority order: (1) method has the {@code org.junit.Test}
   * annotation; (2) method is {@code public void}, begins with the word {@code test}
   * and takes zero parameters.
   * 
   * <p>Currently unsupported.
   * 
   * @param className the String fully-qualified name of the class to check
   * @param methodName the String name of the method to check
   * @return boolean
   */
  public static boolean isUnderTestWith(final String className, final String methodName) {
    throw new UnsupportedOperationException("Currently not implemented");
  }
}
