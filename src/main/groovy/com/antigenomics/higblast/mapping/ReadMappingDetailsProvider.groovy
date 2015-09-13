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

package com.antigenomics.higblast.mapping

import com.antigenomics.higblast.io.Read

import static com.antigenomics.higblast.Util.translateLinear
import static java.lang.Math.max

class ReadMappingDetailsProvider {
    public static
    final List<String> ALLOWED_FIELDS = Collections.unmodifiableList(["fr1nt", "cdr1nt", "fr2nt", "cdr2nt", "fr3nt",
                                                                      "contignt",
                                                                      "fr1aa", "cdr1aa", "fr2aa", "cdr2aa", "fr3aa",
                                                                      "contigaa"])
    public static ReadMappingDetailsProvider DUMMY = new ReadMappingDetailsProvider([])

    private final String sep
    private final List<String> fields

    ReadMappingDetailsProvider(List<String> fields) {
        this.fields = fields.collect { it.toLowerCase() }
        this.sep = fields.empty ? "" : "\t"
        def badFields = fields.findAll { !ALLOWED_FIELDS.contains(it) }
        if (!badFields.empty) {
            throw new RuntimeException("Bad fields supplied: ${badFields.join(",")}, " +
                    "allowed values: ${ALLOWED_FIELDS.join(",")}.")
        }
    }

    String getHeader() {
        sep + fields.join("\t")
    }

    String getDetailsString(ReadMapping readMapping) {
        def details = getDetails(readMapping)

        sep + fields.collect { details."$it" }.join("\t")
    }

    static ReadMappingDetails getDetails(ReadMapping readMapping) {
        readMapping.mapped ?
                new ReadMappingDetails(readMapping.read, readMapping.mapping) :
                ReadMappingDetails.DUMMY
    }

    static class ReadMappingDetails {
        final String seq
        final RegionMarkup readMarkup, referenceMarkup
        final int vStartInRef, vStartInQuery

        static final ReadMappingDetails DUMMY = new ReadMappingDetails()

        ReadMappingDetails(Read read, Mapping mapping) {
            this.seq = read.seq
            this.readMarkup = mapping.regionMarkup
            this.vStartInRef = mapping.vStartInRef
            this.vStartInQuery = mapping.vStartInQuery
            this.referenceMarkup = mapping.vSegment.regionMarkup
        }

        ReadMappingDetails() {
            this.seq = null
            this.vStartInRef = 480011
            this.referenceMarkup = RegionMarkup.DUMMY
            this.readMarkup = RegionMarkup.DUMMY
        }

        int nCount(int pos) {
            max(vStartInRef - pos, 0)
        }

        int sCount(int pos) {
            max(vStartInQuery, pos)
        }

        String getFr1nt() {
            vStartInRef < referenceMarkup.cdr1Start ?
                    'N' * nCount(0) + seq.substring(sCount(0), readMarkup.cdr1Start) :
                    'N' * max(0, referenceMarkup.cdr1Start)
        }

        String getCdr1nt() {
            vStartInRef < referenceMarkup.cdr1End ?
                    'N' * nCount(referenceMarkup.cdr1Start) + seq.substring(sCount(readMarkup.cdr1Start), readMarkup.cdr1End) :
                    'N' * (referenceMarkup.cdr1End - referenceMarkup.cdr1Start)
        }

        String getFr2nt() {
            vStartInRef < referenceMarkup.cdr2Start ?
                    'N' * nCount(referenceMarkup.cdr1End) + seq.substring(sCount(readMarkup.cdr1End), readMarkup.cdr2Start) :
                    'N' * (referenceMarkup.cdr2Start - referenceMarkup.cdr1End)
        }

        String getCdr2nt() {
            vStartInRef < referenceMarkup.cdr2End ?
                    'N' * nCount(referenceMarkup.cdr2Start) + seq.substring(sCount(readMarkup.cdr2Start), readMarkup.cdr2End) :
                    'N' * (referenceMarkup.cdr2End - referenceMarkup.cdr2Start)
        }

        String getFr3nt() {
            vStartInRef < referenceMarkup.cdr3Start ?
                    'N' * nCount(referenceMarkup.cdr2End) + seq.substring(sCount(readMarkup.cdr2End), readMarkup.cdr3Start) :
                    'N' * (referenceMarkup.cdr3Start - referenceMarkup.cdr2End)
        }

        String getContignt() {
            fr1nt + cdr1nt + fr2nt + cdr2nt + fr3nt + downstream
        }

        String getFr1aa() {
            translateLinear(fr1nt)
        }

        String getCdr1aa() {
            translateLinear(cdr1nt)
        }

        String getFr2aa() {
            translateLinear(fr2nt)
        }

        String getCdr2aa() {
            translateLinear(cdr2nt)
        }

        String getFr3aa() {
            translateLinear(fr3nt)
        }

        String getContigaa() {
            translateLinear(contignt)
        }

        String getDownstream() {
            readMarkup.cdr3Start >= 0 ? seq.substring(readMarkup.cdr3Start) : ""
        }
    }
}