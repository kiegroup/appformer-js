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

package org.uberfire.jsbridge.tsexporter.model;

import com.google.testing.compile.CompilationRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.uberfire.jsbridge.tsexporter.meta.JavaType;

import static org.junit.Assert.assertEquals;
import static org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore.NO_DECORATORS;
import static org.uberfire.jsbridge.tsexporter.util.TestingUtils.init;
import static org.uberfire.jsbridge.tsexporter.util.TestingUtils.type;
import static org.uberfire.jsbridge.tsexporter.util.Utils.lines;

public class PojoTsClassTestInterfaces {

    @Rule
    public final CompilationRule compilationRule = new CompilationRule();

    @Before
    public void before() {
        init(compilationRule.getTypes(), compilationRule.getElements());
        JavaType.SIMPLE_NAMES.set(true);
    }

    @After
    public void after() {
        JavaType.SIMPLE_NAMES.set(false);
    }

    interface A {

    }

    @Test
    public void testInterfaceA() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(A.class), NO_DECORATORS);
        assertEquals(lines("",
                           "",
                           "",
                           "export interface A  {",
                           "}"),
                     pojoTsClass.toSource());
    }

    interface B extends A {

    }

    @Test
    public void testInterfaceB() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(B.class), NO_DECORATORS);
        assertEquals(lines("",
                           "import { A as A } from '../../../../../../org/uberfire/jsbridge/tsexporter/model/PojoTsClassTestInterfaces/A';",
                           "",
                           "export interface B extends A {",
                           "}"),
                     pojoTsClass.toSource());
    }

    interface C<T> {

    }

    @Test
    public void testInterfaceC() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(C.class), NO_DECORATORS);
        assertEquals(lines("",
                           "",
                           "",
                           "export interface C<T>  {",
                           "}"),
                     pojoTsClass.toSource());
    }

    interface D<T extends A> {

    }

    @Test
    public void testInterfaceD() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(D.class), NO_DECORATORS);
        assertEquals(lines("",
                           "import { A as A } from '../../../../../../org/uberfire/jsbridge/tsexporter/model/PojoTsClassTestInterfaces/A';",
                           "",
                           "export interface D<T extends A>  {",
                           "}"),
                     pojoTsClass.toSource());
    }

    interface E<J extends D<A>> {

    }

    @Test
    public void testInterfaceE() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(E.class), NO_DECORATORS);
        assertEquals(lines("",
                           "import { A as A } from '../../../../../../org/uberfire/jsbridge/tsexporter/model/PojoTsClassTestInterfaces/A';",
                           "import { D as D } from '../../../../../../org/uberfire/jsbridge/tsexporter/model/PojoTsClassTestInterfaces/D';",
                           "",
                           "export interface E<J extends D<A>>  {",
                           "}"),
                     pojoTsClass.toSource());
    }

    interface F<B extends F<?>> {

    }

    @Test
    public void testInterfaceF() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(F.class), NO_DECORATORS);
        assertEquals(lines("",
                           "",
                           "",
                           "export interface F<B extends F<any /* wildcard */>>  {",
                           "}"),
                     pojoTsClass.toSource());
    }

    interface G<T> extends A {

    }

    @Test
    public void testInterfaceG() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(G.class), NO_DECORATORS);
        assertEquals(lines("",
                           "import { A as A } from '../../../../../../org/uberfire/jsbridge/tsexporter/model/PojoTsClassTestInterfaces/A';",
                           "",
                           "export interface G<T> extends A {",
                           "}"),
                     pojoTsClass.toSource());
    }

    interface H<T> extends C<T> {

    }

    @Test
    public void testInterfaceH() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(H.class), NO_DECORATORS);
        assertEquals(lines("",
                           "import { C as C } from '../../../../../../org/uberfire/jsbridge/tsexporter/model/PojoTsClassTestInterfaces/C';",
                           "",
                           "export interface H<T> extends C<T> {",
                           "}"),
                     pojoTsClass.toSource());
    }

    interface I<T extends C<I>> extends H<T> {

    }

    @Test
    public void testInterfaceI() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(I.class), NO_DECORATORS);
        assertEquals(lines("",
                           "import { C as C } from '../../../../../../org/uberfire/jsbridge/tsexporter/model/PojoTsClassTestInterfaces/C';",
                           "import { H as H } from '../../../../../../org/uberfire/jsbridge/tsexporter/model/PojoTsClassTestInterfaces/H';",
                           "",
                           "export interface I<T extends C<I<any>>> extends H<T> {",
                           "}"),
                     pojoTsClass.toSource());
    }
}