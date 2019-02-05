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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.uberfire.jsbridge.tsexporter.Main;
import org.uberfire.jsbridge.tsexporter.meta.JavaType;

import static java.util.stream.Collectors.toList;

public class TestingUtils {

    public static Types types;
    public static Elements elements;

    public static void init(final Types types, final Elements elements) {
        TestingUtils.types = Main.types = types;
        TestingUtils.elements = Main.elements = elements;
    }

    public static WildcardType wildcard(final TypeMirror extendsBound, final TypeMirror superBound) {
        return types.getWildcardType(extendsBound, superBound);
    }

    public static PrimitiveType primitive(final TypeKind kind) {
        return types.getPrimitiveType(kind);
    }

    public static ArrayType array(final TypeMirror type) {
        return types.getArrayType(type);
    }

    public static TypeMirror erased(final TypeMirror type) {
        return types.erasure(type);
    }

    public static DeclaredType type(final Class<?> clazz) {
        return (DeclaredType) elements.getTypeElement(clazz.getCanonicalName()).asType();
    }

    public static TypeElement element(final Class<?> clazz) {
        return elements.getTypeElement(clazz.getCanonicalName());
    }

    public static JavaType member(final String name, final TypeMirror owner) {
        return new JavaType(memberElement(name, owner).asType(), owner);
    }

    public static Element memberElement(final String name, final TypeMirror owner) {
        return elements.getAllMembers((TypeElement) types.asElement(owner)).stream()
                .filter(s -> s.getSimpleName().toString().equals(name))
                .collect(toList())
                .get(0);
    }

    public static JavaType param(final int i, final JavaType owner) {
        return new JavaType(((ExecutableType) owner.getType()).getParameterTypes().get(i), owner.getOwner());
    }

    public static class Foo {

        public static class Bar {

        }
    }

    public abstract static class DeclaredTypes {

        Map<String, String> map;
        TreeMap<String, String> treeMap;
        List<String> list;
        ArrayList<String> arrayList;
        LinkedList<String> linkedList;
        Set<String> set;
        HashSet<String> hashSet;
        TreeSet<String> treeSet;
        Collection<String> collection;
        Class<String> clazz;
        Optional<String> optional;
    }

    public abstract static class Circle<T extends Circle<T>> {

        T field1;
        Circle<T> field2;

        abstract void get1(T t);

        abstract <U> void get2(T t, U u);

        abstract <U extends T> void get3(T t, U u);

        abstract <U extends T, S extends U> void get4(T t, U u, S s);

        abstract <U extends T, S extends List<? extends T>> void get5(T t, U u, S s);

        abstract <U extends T, S extends Circle<T>> void get6(T t, U u, S s);

        abstract <U extends T, S extends Circle<T>> void get7(T t, U u, S s);
    }

    public abstract static class Cylinder extends Circle<Cylinder> {

    }

    public abstract static class Sphere<J> extends Circle<Sphere<J>> {

    }
}
