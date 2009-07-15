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

import org.apache.pig.impl.plan.OperatorKey;

/**
 * This is a base class for all binary comparison operators. Supports the
 * use of operand type instead of result type as the result type is
 * always boolean.
 * 
 * All comparison operators fetch the lhs and rhs operands and compare
 * them for each type using different comparison methods based on what
 * comparison is being implemented.
 *
 */
public abstract class BinaryComparisonOperator extends BinaryExpressionOperator
        implements ComparisonOperator {
    protected byte operandType;

    // Default instances of true and false, used so that all the equality
    // operators don't have to instantiate a true or false object on each
    // test.
    protected Boolean trueRef;
    protected Boolean falseRef;
    
    public BinaryComparisonOperator(OperatorKey k) {
        this(k,-1);
    }

    public BinaryComparisonOperator(OperatorKey k, int rp) {
        super(k, rp);
    }

    public byte getOperandType() {
        return operandType;
    }

    public void setOperandType(byte operandType) {
        this.operandType = operandType;
    }

    // Necessary because the objects are serialized, not constructed on the
    // other side.
    protected void initializeRefs() {
        trueRef = new Boolean(true);
        falseRef = new Boolean(false);
    }

    protected void cloneHelper(BinaryComparisonOperator op) {
        operandType = op.operandType;
        super.cloneHelper(op);
    }
}
