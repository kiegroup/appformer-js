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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;
import static org.uberfire.jsbridge.tsexporter.util.Utils.distinctBy;

public class IndirectHashMap<DK, V> {

    private final Function<DK, ?> keyMapper;
    private final Map<Object, V> map = new HashMap<>();
    private final Set<DK> directKeys = new HashSet<>();

    public IndirectHashMap(final Function<DK, ?> keyMapper) {
        this.keyMapper = keyMapper;
    }

    public V get(final DK directKey) {
        return map.get(keyMapper.apply(directKey));
    }

    private Set<DK> keySet() {
        return directKeys.stream()
                .filter(distinctBy(keyMapper))
                .collect(toSet());
    }

    public V merge(final DK directKey,
                   final V value,
                   final BiFunction<V, V, V> mergeFunction) {

        final V merged = map.merge(keyMapper.apply(directKey), value, mergeFunction);
        if (merged != null) {
            directKeys.add(directKey);
        } else {
            directKeys.remove(directKey);
        }
        return merged;
    }

    public Set<Map.Entry<DK, V>> entrySet() {
        return keySet().stream()
                .map(dk -> new SimpleImmutableEntry<>(dk, map.get(keyMapper.apply(dk))))
                .collect(toSet());
    }
}
