/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.jsbridge.tsexporter.config;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.uberfire.jsbridge.tsexporter.TsCodegenLibBundler.getAppFormerLibMainFileCopyName;
import static org.uberfire.jsbridge.tsexporter.config.AppFormerLib.Type.LIB;
import static org.uberfire.jsbridge.tsexporter.util.Utils.lines;

public class AppFormerComponentsRegistry {

    private final Configuration configuration;

    public AppFormerComponentsRegistry(final Configuration configuration) {
        this.configuration = configuration;
    }

    public String toSource() {
        final String entries = configuration.getLibraries().stream()
                .filter(lib -> lib.getType().equals(LIB))
                .flatMap(this::bundleComponentsConfiguration)
                .map(e -> format("\"%s\": %s", e.getKey(), e.getValue()))
                .collect(joining(",\n"));

        return format(lines("window.AppFormerComponentsRegistry = { ",
                            "%s",
                            "}"),
                      entries);
    }

    private Stream<Map.Entry<String, String>> bundleComponentsConfiguration(final AppFormerLib appformerLib) {
        return appformerLib.getComponents().stream()
                .map(componentConfiguration -> {

                    final String type = componentConfiguration.getType().name().toLowerCase();
                    final String source = getAppFormerLibMainFileCopyName(appformerLib);
                    final String params = componentConfiguration.getParams();

                    final String configStr = format(
                            lines("{",
                                  "  \"type\": \"%s\",",
                                  "  \"source\": \"%s\",",
                                  "  \"params\": %s",
                                  "}"),
                            type, source, params);

                    return new AbstractMap.SimpleImmutableEntry<>(componentConfiguration.getComponentId(), configStr);
                });
    }
}
