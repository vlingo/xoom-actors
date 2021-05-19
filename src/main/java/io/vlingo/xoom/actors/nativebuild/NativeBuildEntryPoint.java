package io.vlingo.xoom.actors.nativebuild;

import io.vlingo.xoom.actors.Configuration;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.plugin.logging.slf4j.Slf4jLoggerPlugin;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

public final class NativeBuildEntryPoint {
  @CEntryPoint(name = "Java_io_vlingo_xoom_actorsnative_Native_start")
  public static int start(@CEntryPoint.IsolateThreadContext long isolateId, CCharPointer name) {
    final String nameString = CTypeConversion.toJavaString(name);

    Configuration configuration = Configuration.define()
        .with(Slf4jLoggerPlugin.Slf4jLoggerPluginConfiguration.define().defaultLogger().name("XOOM"));
    World.start(nameString, configuration);
    return 0;
  }
}