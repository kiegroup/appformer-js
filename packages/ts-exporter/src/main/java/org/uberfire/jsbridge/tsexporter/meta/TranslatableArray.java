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

package org.uberfire.jsbridge.tsexporter.meta;

import java.util.List;

import org.uberfire.jsbridge.tsexporter.dependency.ImportEntry;

import static java.lang.String.format;
import static org.uberfire.jsbridge.tsexporter.meta.Translatable.SourceUsage.TYPE_ARGUMENT_USE;

public class TranslatableArray implements Translatable {

    private final Translatable componentTranslatable;

    public TranslatableArray(final Translatable componentTranslatable) {
        this.componentTranslatable = componentTranslatable;
    }

    @Override
    public String toTypeScript(final SourceUsage sourceUsage) {
        switch (sourceUsage) {
            case TYPE_ARGUMENT_USE:
            case TYPE_ARGUMENT_DECLARATION:
                return format("%s[]", componentTranslatable.toTypeScript(sourceUsage));
            case IMPORT_STATEMENT:
                return componentTranslatable.toTypeScript(sourceUsage);
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public List<ImportEntry> getAggregatedImportEntries() {
        return componentTranslatable.getAggregatedImportEntries();
    }
}
