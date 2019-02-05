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

package org.uberfire.jsbridge.tsexporter.util;

import java.util.List;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.STATIC;

public final class ElementUtils {

    public static List<Element> getAllNonStaticFields(final TypeElement typeElement) {

        final List<Element> currentTypeFields = nonStaticFieldsIn(typeElement.getEnclosedElements());

        if (typeElement.getSuperclass().toString().equals("java.lang.Object")) {
            return currentTypeFields;
        }

        final TypeElement superElement = (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement();
        final List<Element> inheritedTypeFields = getAllNonStaticFields(superElement);

        return Stream.concat(inheritedTypeFields.stream(), currentTypeFields.stream()).collect(toList());
    }

    public static List<Element> nonStaticFieldsIn(final List<? extends Element> elements) {
        return elements.stream()
                .filter(e -> e.getKind().isField())
                .filter(e -> !e.getModifiers().contains(STATIC))
                .filter(e -> !e.asType().toString().contains("java.util.function"))
                .collect(toList());
    }
}
