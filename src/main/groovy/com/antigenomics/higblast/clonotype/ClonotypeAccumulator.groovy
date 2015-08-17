/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.higblast.clonotype

import com.antigenomics.higblast.InputPort
import com.antigenomics.higblast.mapping.ReadMapping

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class ClonotypeAccumulator implements InputPort<ReadMapping> {
    def total = new AtomicLong()
    def clonotypeMap = new ConcurrentHashMap<ClonotypeKey, ClonotypeData>()

    @Override
    void put(ReadMapping readMapping) {
        ClonotypeData clonotypeData = clonotypeMap.putIfAbsent(new ClonotypeKey(readMapping),
                new ClonotypeData(readMapping))
        if (clonotypeData) {
            clonotypeData.update(readMapping)
        }
        total.incrementAndGet()
    }

    @Override
    void close() {

    }

    long getTotal() {
        total.get()
    }
}