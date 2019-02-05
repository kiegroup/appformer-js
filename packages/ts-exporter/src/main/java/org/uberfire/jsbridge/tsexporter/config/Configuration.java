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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.list;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.uberfire.jsbridge.tsexporter.util.Utils.getResources;

public class Configuration {

    private final Map<String, AppFormerLib> libsByName;

    public Configuration() {
        libsByName = list(getResources("META-INF/appformer-js.json")).stream()
                .map(AppFormerLib::new)
                .collect(toMap(AppFormerLib::getName, identity(), (lib1, lib2) -> {

                    final String module1 = lib1.getAssociatedMvnModuleName();
                    final String module2 = lib2.getAssociatedMvnModuleName();

                    if (!module1.equals(module2)) {
                        final String errorMsg = format("Multiple AppFormerLibs with same name %s! (Modules %s and %s)",
                                                       lib1.getName(), module1, module2);
                        throw new IllegalStateException(errorMsg);
                    }

                    return lib1;
                }));
    }

    public Set<AppFormerLib> getLibraries() {
        return new HashSet<>(libsByName.values());
    }
}
