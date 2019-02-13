#ts-exporter

This module provides a Java Annotation Processor to generate and locally publish TypeScript packages containing the code necessary to interact with the Errai Bus. It works both for RPC and Events.

##Building
mvn clean install

#Usage
Simply add its dependency on an existing Maven module and the Annotation Processor present will scan for `@Remote`- and `@Portable`-annotated classes. 

For `lib` modules, files containing the Full Qualified Class Names (fqcn) of the annotated types will be created.
 
> **NOTE:** `lib` modules don't need any special configuration. Just the dependency will suffice.

For `app` modules, TypeScript code will be generated and published to your local NPM registry running on `http://localhost:4873`. `@Portable` classes will be present on `@kiegroup-ts-generated/[my-module]` and `@Remote` ones will be on `@kiegroup-ts-generated/[my-module]-rpc`. We recommend using [verdaccio](https://github.com/verdaccio/verdaccio) as your local npm registry. To install it simply run `npm install -g verdaccio`. To run it, simply run `verdaccio`. The `app` module must have at least one class annotated with `@EntryPoint`, `@Portable`, or `@Remote`.

> **NOTE:** `app` modules require a special configuration to actually generate and lcoally publish the generated TypeScript code:

>> **ts-exporter-output-dir** must be configured with the directory where the root of generated code will be.

>> **ts-exporter** must be set to "export".

The following Maven profile configuration is recommended.
```xml
    <profile>
      <id>ts-exporter-build</id>
      <build>
        <plugins>
          <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>properties-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
              <execution>
                <id>set-ts-exporter-all</id>
                <phase>validate</phase>
                <goals>
                  <goal>set-system-properties</goal>
                </goals>
                <configuration>
                  <properties>
                    <property>
                      <name>ts-exporter-output-dir</name>
                      <value>${project.build.directory}</value>
                    </property>
                    <property>
                      <name>ts-exporter</name>
                      <value>export</value>
                    </property>
                  </properties>
                </configuration>
              </execution>
              <execution>
                <id>set-ts-exporter-none</id>
                <phase>prepare-package</phase>
                <goals>
                  <goal>set-system-properties</goal>
                </goals>
                <configuration>
                  <properties>
                    <property>
                      <name>ts-exporter</name>
                      <value>default</value>
                    </property>
                  </properties>
                </configuration>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </profile>
```

