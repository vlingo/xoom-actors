# VLINGO XOOM Platform - GraalVM Support

Use the VLINGO XOOM OSS platform SDK for rapid delivery of low-code to full-code Reactive, Event-Driven Microservices and Applications using DOMA, DDD, and other approaches.

Docs: https://docs.vlingo.io/

[Official VLINGO XOOM website](https://vlingo.io/).

## Getting started

Prerequisites:
* Java JDK 8 or greater
* Maven
* [GraalVM 21.1.0 Java 8/11](https://www.graalvm.org/docs/getting-started/)

## Native Image Maven Plugin & GraalVM SDK
```
<properties>
    ...
    <graalvm.version>21.1.0</graalvm.version>
    ...
</properties>
<dependencies>
    ...
    <dependency>
      <groupId>org.graalvm.sdk</groupId>
      <artifactId>graal-sdk</artifactId>
      <version>${graalvm.version}</version>
      <scope>provided</scope>
    </dependency>
    ...
</dependencies>

```
```
<profiles>
    ...
    <profile>
      <id>native-image</id>
      <build>
        <plugins>
          <plugin>
            <groupId>org.graalvm.nativeimage</groupId>
            <artifactId>native-image-maven-plugin</artifactId>
            <version>${graalvm.version}</version>
            <executions>
              <execution>
                <goals>
                  <goal>native-image</goal>
                </goals>
                <phase>package</phase>
              </execution>
            </executions>
            <configuration>
              <imageName>${project.name}</imageName>
              <buildArgs> 
                --shared 
                -H:DashboardDump=${project.name} -H:+DashboardAll 
                -H:+DashboardJson -H:+DashboardPretty 
                --allow-incomplete-classpath
              </buildArgs>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
    ...
</profiles>
```
## Class Library Build
- We need to have at least one entry point method for a native image to be useful. \
  For shared libraries, Native Image provides the @CEntryPoint annotation to specify entry point methods that should be exported and callable from C.
```
package io.vlingo.xoom.actors.implnative;

import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import io.vlingo.xoom.actors.plugin.logging.slf4j.Slf4jLoggerPlugin;

public final class NativeImpl {
    @CEntryPoint(name = "Java_io_vlingo_xoom_actorsnative_Native_start")
    public static int start(@CEntryPoint.IsolateThreadContext long isolateId, CCharPointer name) {
        final String nameString = CTypeConversion.toJavaString(name);

        Configuration configuration = Configuration.define()
                .with(Slf4jLoggerPlugin.Slf4jLoggerPluginConfiguration.define().defaultLogger().name("XOOM"));
        World.start(nameString, configuration);
        return 0;
    }
}
```
- To build the native image run:
```bash
mvn clean install -Pnative-image
```
- Inside target/, will find the generated lib, and the GraalVM Dashboard Dump files (${project.name}.dbv/.dump)\
  Go to [GraalVM Dashboard](https://www.graalvm.org/docs/tools/dashboard/) and upload the file.. 
  