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

package org.uberfire.jsbridge.tsexporter.dependency;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;

import org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore;
import org.uberfire.jsbridge.tsexporter.meta.JavaType;
import org.uberfire.jsbridge.tsexporter.model.PojoTsClass;

import static org.uberfire.jsbridge.tsexporter.meta.Translatable.SourceUsage.IMPORT_STATEMENT;

public class ImportEntryJava implements ImportEntry {

    private final DeclaredType declaredType;
    private final DecoratorStore decoratorStore;

    public ImportEntryJava(final DeclaredType declaredType,
                           final DecoratorStore decoratorStore) {

        this.declaredType = declaredType;
        this.decoratorStore = decoratorStore;
    }

    @Override
    public String getUniqueTsIdentifier(final DeclaredType owner) {
        return new JavaType(declaredType, owner).translate(decoratorStore).toTypeScript(IMPORT_STATEMENT);
    }

    @Override
    public String getRelativePath() {
        return new PojoTsClass(declaredType, decoratorStore).getRelativePath();
    }

    @Override
    public String getNpmPackageName() {
        return new PojoTsClass(declaredType, decoratorStore).getNpmPackageName();
    }

    @Override
    public Element asElement() {
        return declaredType.asElement();
    }

    @Override
    public boolean represents(final DeclaredType type) {
        return asElement().equals(type.asElement());
    }

    @Override
    public String toString() {
        return declaredType.toString();
    }
}
