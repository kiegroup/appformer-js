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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ComponentConfiguration {

    private final String componentId;
    private final ComponentType type;
    private final String params;

    public ComponentConfiguration(final JsonElement json) {

        final JsonObject jsonObj = json.getAsJsonObject();

        this.componentId = jsonObj.get("id").getAsString();
        this.type = ComponentType.valueOf(jsonObj.get("type").getAsString().trim().toUpperCase());

        final JsonObject params = jsonObj.get("params").getAsJsonObject();
        validateParams(params);
        this.params = params.toString();
    }

    private void validateParams(final JsonObject params) {
        switch (this.type) {
            case EDITOR:
                final JsonElement matches = params.get("matches");
                if (matches == null || !matches.isJsonPrimitive()) {
                    throw new RuntimeException("Property 'matches' should be a string containing a regex.");
                }
                break;
            case PERSPECTIVE:
                final JsonElement isDefault = params.get("is_default");
                if (isDefault == null || !isDefault.isJsonPrimitive()) {
                    throw new RuntimeException("Property 'is_default' should be a boolean.");
                }
                break;
            case SCREEN:
                break;
        }
    }

    public String getComponentId() {
        return this.componentId;
    }

    public ComponentType getType() {
        return this.type;
    }

    public String getParams() {
        return this.params;
    }

    public enum ComponentType {
        PERSPECTIVE,
        SCREEN,
        EDITOR,
    }
}
