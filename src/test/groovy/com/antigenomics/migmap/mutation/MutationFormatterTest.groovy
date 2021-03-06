/*
 * Copyright 2014-2015 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.migmap.mutation

import com.antigenomics.migmap.blast.Alignment
import com.antigenomics.migmap.genomic.SegmentDatabase
import com.antigenomics.migmap.mapping.RegionMarkup
import org.junit.Test

class MutationFormatterTest {
    def segmentDatabase = new SegmentDatabase("data/", "human", ["IGH"])

    @Test
    void reverseTest() {
        def alignment = new Alignment(0,
                //0000000001  11111111122222222223333
                //1234567890  12345678901234567890123
                "AGATCGATCGA--CTGCTACGACTGCATGACTCAAT", 0,
                //0000000001111111111222222   2222333
                //1234567890123456789012345   6789012
                "AAATCGATCGAAACTGCTACGACTGC---ACTCAGT")

        def mutations = MutationExtractor.extract(alignment).mutations

        assert !mutations.empty

        def sseq = alignment.sseq.replaceAll("-", ""), qseq = alignment.qseq.replaceAll("-", "")

        assert sseq == MutationFormatter.mutateBack(qseq, mutations)
        assert qseq != MutationFormatter.mutateBack(sseq, mutations)
    }

    @Test
    void ntMutationsTest() {
        def query = "CAGCTGGAGTTGGTACAGTCTGGGGCTGAGGAGAAGAAGCCTGGGGCCTCAGTGAAGGTCTCCTGCAAGGCTTCTGGATCCACATTCAGC" +
                "GGCCACTTTATGCACTGGGTGCGACAGGCCCCTGGACAAGGGCTTGAGTGGATGGGGTGGATCAACTCTTACAGTGGTGCCACAAAGTAT" +
                "GCACAGAAGTTTCAGGGCAGGGTCACCATGACCAGGGACACGTCCATGACCACAATCTACATGGAGCTGAGCGGACTCACATCTGACGAC" +
                "ACGGCCGTGTATTTTTGTACCAGA"

        def segment = segmentDatabase.segments["IGHV1-2*02"]
        def regionMarkup = new RegionMarkup(75, 99, 150, 174, 287, 288)
        def alignment = new Alignment(0, query, 0, segment.sequence.substring(0, query.length()))
        def mutations = new MutationExtractor(segment, alignment, regionMarkup).mutations

        assert MutationFormatter.toStringNT(mutations).split("\t") ==
                ['S3:G>C,S6:C>G,S9:C>T,S14:G>A,S31:T>A',
                 'S79:A>C,S83:C>A,S88:C>G,S93:T>C,S97:A>T',
                 'S146:A>G',
                 'S156:C>T,S159:A>T,S169:G>C',
                 'S176:C>G,S227:C>G,S229:G>C,S234:G>A,S235:C>T,S252:A>G,S254:G>A,S257:G>C,S259:G>C,S283:A>T,S284:C>T',
                 'S288:G>A,S290:G>C']
    }

    @Test
    void aaMutationsTest() {
        def query = "CAGCTGGAGTTGGTACAGTCTGGGGCTGAGGAGAAGAAGCCTGGGGCCTCAGTGAAGGTCTCCTGCAAGGCTTCTGGATCCACATTCAGC" +
                "GGCCACTTTATGCACTGGGTGCGACAGGCCCCTGGACAAGGGCTTGAGTGGATGGGGTGGATCAACTCTTACAGTGGTGCCACAAAGTAT" +
                "GCACAGAAGTTTCAGGGCAGGGTCACCATGACCAGGGACACGTCCATGACCACAATCTACATGGAGCTGAGCGGACTCACATCTGACGAC" +
                "ACGGCCGTGTATTTTTGTACCAGA"

        def segment = segmentDatabase.segments["IGHV1-2*02"]
        def regionMarkup = new RegionMarkup(75, 99, 150, 174, 287, 288)
        def alignment = new Alignment(0, query, 0, segment.sequence.substring(0, query.length()))
        def mutations = new MutationExtractor(segment, alignment, regionMarkup).mutations

        assert MutationFormatter.toStringAA(mutations, query, 0, 0).split("\t") ==
                ['S1:V>L,S2:Q>E,S3:L>L,S4:V>V,S10:V>E',
                 'S26:Y>S,S27:T>T,S29:T>S,S31:Y>H,S32:Y>F',
                 'S48:G>G',
                 'S52:P>S,S53:N>Y,S56:G>A',
                 'S58:N>K,S75:I>M,S76:S>T,S78:A>I,S78:A>I,S84:R>G,S84:R>G,S85:L>L,S86:R>T,S94:Y>F,S94:Y>F',
                 'S96:A>T,S96:A>T']
    }

    @Test
    void aaMutationsTestShift1() {
        def query = "GCTGGAGTTGGTACAGTCTGGGGCTGAGGAGAAGAAGCCTGGGGCCTCAGTGAAGGTCTCCTGCAAGGCTTCTGGATCCACATTCAGC" +
                "GGCCACTTTATGCACTGGGTGCGACAGGCCCCTGGACAAGGGCTTGAGTGGATGGGGTGGATCAACTCTTACAGTGGTGCCACAAAGTAT" +
                "GCACAGAAGTTTCAGGGCAGGGTCACCATGACCAGGGACACGTCCATGACCACAATCTACATGGAGCTGAGCGGACTCACATCTGACGAC" +
                "ACGGCCGTGTATTTTTGTACCAGA"

        def segment = segmentDatabase.segments["IGHV1-2*02"]
        def regionMarkup = new RegionMarkup(75 - 2, 99 - 2, 150 - 2, 174 - 2, 287 - 2, 288 - 2)
        def alignment = new Alignment(0, query, 2, segment.sequence.substring(2, 2 + query.length()))
        def mutations = new MutationExtractor(segment, alignment, regionMarkup).mutations

        assert MutationFormatter.toStringNT(mutations).split("\t") ==
                ['S3:G>C,S6:C>G,S9:C>T,S14:G>A,S31:T>A',
                 'S79:A>C,S83:C>A,S88:C>G,S93:T>C,S97:A>T',
                 'S146:A>G',
                 'S156:C>T,S159:A>T,S169:G>C',
                 'S176:C>G,S227:C>G,S229:G>C,S234:G>A,S235:C>T,S252:A>G,S254:G>A,S257:G>C,S259:G>C,S283:A>T,S284:C>T',
                 'S288:G>A,S290:G>C']

        assert MutationFormatter.toStringAA(mutations, query, alignment.sstart, alignment.qstart).split("\t") ==
                ['S1:V>L,S2:Q>E,S3:L>L,S4:V>V,S10:V>E',
                 'S26:Y>S,S27:T>T,S29:T>S,S31:Y>H,S32:Y>F',
                 'S48:G>G',
                 'S52:P>S,S53:N>Y,S56:G>A',
                 'S58:N>K,S75:I>M,S76:S>T,S78:A>I,S78:A>I,S84:R>G,S84:R>G,S85:L>L,S86:R>T,S94:Y>F,S94:Y>F',
                 'S96:A>T,S96:A>T']
    }

    @Test
    void aaMutationsTestShift2() {
        def query = "CGCTGGAGTTGGTACAGTCTGGGGCTGAGGAGAAGAAGCCTGGGGCCTCAGTGAAGGTCTCCTGCAAGGCTTCTGGATCCACATTCAGC" +
                "GGCCACTTTATGCACTGGGTGCGACAGGCCCCTGGACAAGGGCTTGAGTGGATGGGGTGGATCAACTCTTACAGTGGTGCCACAAAGTAT" +
                "GCACAGAAGTTTCAGGGCAGGGTCACCATGACCAGGGACACGTCCATGACCACAATCTACATGGAGCTGAGCGGACTCACATCTGACGAC" +
                "ACGGCCGTGTATTTTTGTACCAGA"

        def segment = segmentDatabase.segments["IGHV1-2*02"]
        def regionMarkup = new RegionMarkup(75 - 1, 99 - 1, 150 - 1, 174 - 1, 287 - 1, 288 - 1)
        def alignment = new Alignment(1, query.substring(1), 2, segment.sequence.substring(2, 2 + query.length() - 1))
        def mutations = new MutationExtractor(segment, alignment, regionMarkup).mutations

        assert MutationFormatter.toStringNT(mutations).split("\t") ==
                ['S3:G>C,S6:C>G,S9:C>T,S14:G>A,S31:T>A',
                 'S79:A>C,S83:C>A,S88:C>G,S93:T>C,S97:A>T',
                 'S146:A>G',
                 'S156:C>T,S159:A>T,S169:G>C',
                 'S176:C>G,S227:C>G,S229:G>C,S234:G>A,S235:C>T,S252:A>G,S254:G>A,S257:G>C,S259:G>C,S283:A>T,S284:C>T',
                 'S288:G>A,S290:G>C']

        assert MutationFormatter.toStringAA(mutations, query, alignment.sstart, alignment.qstart).split("\t") ==
                ['S1:V>L,S2:Q>E,S3:L>L,S4:V>V,S10:V>E',
                 'S26:Y>S,S27:T>T,S29:T>S,S31:Y>H,S32:Y>F',
                 'S48:G>G',
                 'S52:P>S,S53:N>Y,S56:G>A',
                 'S58:N>K,S75:I>M,S76:S>T,S78:A>I,S78:A>I,S84:R>G,S84:R>G,S85:L>L,S86:R>T,S94:Y>F,S94:Y>F',
                 'S96:A>T,S96:A>T']
    }
}
