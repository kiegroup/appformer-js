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

package org.uberfire.jsbridge.tsexporter.decorators;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;

import org.uberfire.jsbridge.tsexporter.dependency.ImportEntry;

import static org.uberfire.jsbridge.tsexporter.Main.elements;

public class ImportEntryForDecorator implements ImportEntry {

    private final String decoratedMvnModule;
    private final String npmPackageName;
    private final String decoratorPath;
    private final String decoratedFqcn;

    public ImportEntryForDecorator(final String decoratedMvnModule,
                                   final String npmPackageName,
                                   final String decoratorPath,
                                   final String decoratedFqcn) {

        this.decoratedMvnModule = decoratedMvnModule;
        this.npmPackageName = npmPackageName;
        this.decoratorPath = decoratorPath;
        this.decoratedFqcn = decoratedFqcn;
    }

    @Override
    public String getUniqueTsIdentifier(final DeclaredType owner) {
        return decoratorPath.replace("/", "_").replace("-", "");
    }

    @Override
    public String getRelativePath() {
        return decoratorPath;
    }

    public String getNpmPackageName() {
        return npmPackageName;
    }

    @Override
    public Element asElement() {
        return elements.getTypeElement(decoratedFqcn);
    }

    public String getDecoratedFqcn() {
        return decoratedFqcn;
    }

    public String getDecoratorPath() {
        return decoratorPath;
    }

    @Override
    public boolean represents(final DeclaredType type) {
        return false;
    }

    public String getDecoratedMvnModule() {
        return decoratedMvnModule;
    }
}
