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

package org.apache.pig.impl.plan;

import java.io.Serializable;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;

import org.apache.pig.impl.plan.OperatorKey;

/**
 * Base class for all types of operators.
 */
abstract public class Operator<V extends PlanVisitor> implements Serializable, Comparable<Operator>, Cloneable {
    private static final long serialVersionUID = 1L;

    /**
     * OperatorKey associated with this operator. This key is used to find the
     * operator in an OperatorPlan.
     */
    protected OperatorKey mKey;

    /**
     * @param k Operator key to assign to this node.
     */
    public Operator(OperatorKey k) {
        mKey = k;
    }

    /**
     * Get the operator key for this operator.
     */
    public OperatorKey getOperatorKey() {
        return mKey;
    }

    /**
     * Visit this node with the provided visitor. This should only be called by
     * the visitor class itself, never directly.
     * 
     * @param v
     *            Visitor to visit with.
     * @throws VisitorException
     *             if the visitor has a problem.
     */
    public abstract void visit(V v) throws VisitorException;

    /**
     * Indicates whether this operator supports multiple inputs.
     * 
     * @return true if it does, otherwise false.
     */
    public abstract boolean supportsMultipleInputs();

    /**
     * Indicates whether this operator supports multiple outputs.
     * 
     * @return true if it does, otherwise false.
     */
    public abstract boolean supportsMultipleOutputs();

    public abstract String name();

    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();

        msg.append("(Name: " + name() + " Operator Key: " + mKey + ")");

        return msg.toString();
    }
    
    /**
     * Compares to Operators based on their opKey
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Operator){
            Operator opObj = (Operator)obj;
            if(obj==this)
                return true;
            return mKey.equals(opObj);
        }
        return false;
    }
    
    /**
     * Needed to ensure that the list iterators'
     * outputs are deterministic. Without this
     * we are totally at object id's mercy.
     */
    @Override
    public int hashCode() {
        return mKey.hashCode();
    }

    public int compareTo(Operator o) {
        return mKey.compareTo(o.mKey);
    }
    
    /**
     * @see java.lang.Object#clone()
     * Do not use the clone method directly. Operators are cloned when logical plans
     * are cloned using {@link LogicalPlanCloner}
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        Object o = super.clone();
        Operator opClone = (Operator)o;
        opClone.mKey = new OperatorKey(mKey.scope, NodeIdGenerator.getGenerator().getNextNodeId(mKey.scope));
        return opClone;
    }
    
}
