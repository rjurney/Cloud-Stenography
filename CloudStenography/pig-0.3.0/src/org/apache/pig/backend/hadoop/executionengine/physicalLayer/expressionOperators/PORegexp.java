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
package org.apache.pig.backend.hadoop.executionengine.physicalLayer.expressionOperators;

import java.util.regex.PatternSyntaxException;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.POStatus;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.Result;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.plans.PhyPlanVisitor;
import org.apache.pig.data.DataType;
import org.apache.pig.impl.plan.OperatorKey;
import org.apache.pig.impl.plan.NodeIdGenerator;
import org.apache.pig.impl.plan.VisitorException;

public class PORegexp extends BinaryComparisonOperator {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PORegexp(OperatorKey k) {
        this(k, -1);
    }

    public PORegexp(OperatorKey k, int rp) {
        super(k, rp);
        resultType = DataType.BOOLEAN;
    }

    @Override
    public void visit(PhyPlanVisitor v) throws VisitorException {
        v.visitRegexp(this);
    }

    @Override
    public String name() {
        return "Matches - " + mKey.toString();
    }

    @Override
    public Result getNext(Boolean bool) throws ExecException {
        byte status;
        Result left, right;

        left = lhs.getNext(dummyString);
        right = rhs.getNext(dummyString);

        if (trueRef == null) initializeRefs();
        if (left.returnStatus != POStatus.STATUS_OK || left.result == null) return left;
        if (right.returnStatus != POStatus.STATUS_OK || right.result == null) return right;
        if (((String)left.result).matches((String)right.result)) {
            left.result = trueRef;
        } else {
            left.result = falseRef;
        }
        return left;
    }

    @Override
    public PORegexp clone() throws CloneNotSupportedException {
        PORegexp clone = new PORegexp(new OperatorKey(mKey.scope, 
            NodeIdGenerator.getGenerator().getNextNodeId(mKey.scope)));
        clone.cloneHelper(this);
        return clone;
    }
}
