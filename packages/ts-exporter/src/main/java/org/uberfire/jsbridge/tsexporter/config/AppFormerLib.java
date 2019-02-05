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

import java.net.URL;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.uberfire.jsbridge.tsexporter.model.TsClass.getMavenModuleNameFromSourceFilePath;
import static org.uberfire.jsbridge.tsexporter.util.Utils.readClasspathResource;

public class AppFormerLib {

    private final String name;
    private final Type type;
    private final String associatedMvnModuleName;
    private final Map<String, String> decorators;
    private final String main;
    private final Set<ComponentConfiguration> components;

    public AppFormerLib(final URL url) {
        final String jsonString = readClasspathResource(url);
        final JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();

        name = json.get("name").getAsString();
        main = json.get("main").getAsString();

        type = Type.valueOf(json.get("type").getAsString().trim().toUpperCase());

        associatedMvnModuleName = getMavenModuleNameFromSourceFilePath(url.getFile());

        components = stream(json.get("components").getAsJsonArray().spliterator(), false)
                .map(ComponentConfiguration::new)
                .collect(toSet());

        decorators = json.get("decorators").getAsJsonObject()
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey,
                               e -> e.getValue().getAsString()));
    }

    public String getAssociatedMvnModuleName() {
        return associatedMvnModuleName;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getDecorators() {
        return decorators;
    }

    public Type getType() {
        return type;
    }

    public String getMain() {
        return main;
    }

    public Set<ComponentConfiguration> getComponents() {
        return components;
    }

    public enum Type {
        DECORATORS,
        LIB
    }
}
