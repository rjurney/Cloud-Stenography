/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pig.pen.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.LOFilter;
import org.apache.pig.impl.logicalLayer.LogicalOperator;
import org.apache.pig.impl.util.IdentityHashSet;

//Evaluates various metrics
public class MetricEvaluation {
    public static float getRealness(LogicalOperator op,
            Map<LogicalOperator, DataBag> exampleData, boolean overallRealness) {
        // StringBuffer str = new StringBuffer();
        int noTuples = 0;
        int noSynthetic = 0;
        for (Map.Entry<LogicalOperator, DataBag> e : exampleData.entrySet()) {
            // if(e.getKey() instanceof LORead) continue;
            if (e.getKey().getAlias() == null)
                continue;
            DataBag bag;
            if (overallRealness) {
                bag = exampleData.get(e.getKey());
            } else {
                bag = exampleData.get(op);
            }
            noTuples += bag.size();
            for (Iterator<Tuple> it = bag.iterator(); it.hasNext();) {
                if (((ExampleTuple) it.next()).synthetic)
                    noSynthetic++;
            }
            if (!overallRealness)
                break;

        }

        if (noTuples == 0) {
            if (noSynthetic == 0)
                return 0.0f;
            else
                return 100.0f;
        }
        return 100 * (1 - ((float) noSynthetic / (float) noTuples));

    }

    public static float getConciseness(
            LogicalOperator op,
            Map<LogicalOperator, DataBag> exampleData,
            Map<LogicalOperator, Collection<IdentityHashSet<Tuple>>> OperatorToEqClasses,
            boolean overallConciseness) {
        DataBag bag = exampleData.get(op);

        int noEqCl = OperatorToEqClasses.get(op).size();
        long noTuples = bag.size();

        float conciseness = 100 * ((float) noEqCl / (float) noTuples);
        if (!overallConciseness) {

            return ((conciseness > 100.0) ? 100.0f : conciseness);
        } else {

            noEqCl = 0;
            noTuples = 0;
            conciseness = 0;
            int noOperators = 0;

            for (Map.Entry<LogicalOperator, Collection<IdentityHashSet<Tuple>>> e : OperatorToEqClasses
                    .entrySet()) {
                if (e.getKey().getAlias() == null)
                    continue;
                noOperators++; // we need to keep a track of these and not use
                               // OperatorToEqClasses.size() as LORead shouldn't
                               // be considered a operator
                bag = exampleData.get(e.getKey());

                noTuples = bag.size();
                noEqCl = e.getValue().size();
                float concise = 100 * ((float) noEqCl / (float) noTuples);
                concise = (concise > 100) ? 100 : concise;
                conciseness += concise;
            }
            conciseness /= (float) noOperators;

            return conciseness;
        }

    }

    public static float getCompleteness(
            LogicalOperator op,
            Map<LogicalOperator, DataBag> exampleData,
            Map<LogicalOperator, Collection<IdentityHashSet<Tuple>>> OperatorToEqClasses,
            boolean overallCompleteness) {

        int noClasses = 0;
        int noCoveredClasses = 0;
        int noOperators = 0;
        Map<Integer, Boolean> coveredClasses;
        float completeness = 0;
        if (!overallCompleteness) {
            Collection<IdentityHashSet<Tuple>> eqClasses = OperatorToEqClasses
                    .get(op);
            DataBag bag;

            if (op instanceof LOFilter)
                bag = exampleData.get(((LOFilter) op).getInput());
            else
                bag = exampleData.get(op);
            coveredClasses = getCompletenessLogic(bag, eqClasses);
            noClasses = eqClasses.size();
            for (Map.Entry<Integer, Boolean> e : coveredClasses.entrySet()) {
                if (e.getValue()) {
                    noCoveredClasses++;
                }
            }

            return 100 * ((float) noCoveredClasses) / (float) noClasses;
        } else {
            for (Map.Entry<LogicalOperator, Collection<IdentityHashSet<Tuple>>> e : OperatorToEqClasses
                    .entrySet()) {
                noCoveredClasses = 0;
                noClasses = 0;

                // if(e.getKey() instanceof LORead) continue; //We don't
                // consider LORead a operator.
                if (e.getKey().getAlias() == null)
                    continue; // we want to consider join a single operator
                noOperators++;
                Collection<IdentityHashSet<Tuple>> eqClasses = e.getValue();
                LogicalOperator lop = e.getKey();
                DataBag bag;
                if (lop instanceof LOFilter)
                    bag = exampleData.get(((LOFilter) lop).getInput());
                else
                    bag = exampleData.get(lop);
                coveredClasses = getCompletenessLogic(bag, eqClasses);
                noClasses += eqClasses.size();
                for (Map.Entry<Integer, Boolean> e_result : coveredClasses
                        .entrySet()) {
                    if (e_result.getValue()) {
                        noCoveredClasses++;
                    }
                }
                completeness += 100 * ((float) noCoveredClasses / (float) noClasses);
            }
            completeness /= (float) noOperators;

            return completeness;
        }

    }

    private static Map<Integer, Boolean> getCompletenessLogic(DataBag bag,
            Collection<IdentityHashSet<Tuple>> eqClasses) {
        Map<Integer, Boolean> coveredClasses = new HashMap<Integer, Boolean>();

        for (Iterator<Tuple> it = bag.iterator(); it.hasNext();) {
            Tuple t = it.next();
            int classId = 0;
            for (IdentityHashSet<Tuple> eqClass : eqClasses) {

                if (eqClass.contains(t) || eqClass.size() == 0) {
                    coveredClasses.put(classId, true);
                }
                classId++;
            }
        }

        return coveredClasses;

    }
}
