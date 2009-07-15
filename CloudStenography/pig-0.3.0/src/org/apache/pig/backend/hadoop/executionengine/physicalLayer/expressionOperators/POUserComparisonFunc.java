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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pig.ComparisonFunc;
import org.apache.pig.FuncSpec;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.POStatus;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.Result;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.plans.PhyPlanVisitor;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.PigContext;
import org.apache.pig.impl.plan.OperatorKey;
import org.apache.pig.impl.plan.NodeIdGenerator;
import org.apache.pig.impl.plan.VisitorException;

public class POUserComparisonFunc extends ExpressionOperator {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    FuncSpec funcSpec;
    Tuple t1, t2;
    transient ComparisonFunc func;
	private Log log = LogFactory.getLog(getClass());
	
	public POUserComparisonFunc(OperatorKey k, int rp, List inp, FuncSpec funcSpec, ComparisonFunc func) {
        super(k, rp);
        super.setInputs(inp);
        this.funcSpec = funcSpec;
		this.func = func;
        if(func==null)
            instantiateFunc();
	}
	
	public POUserComparisonFunc(OperatorKey k, int rp, List inp, FuncSpec funcSpec) {
		this(k, rp, inp, funcSpec, null);
	}
	
	private void instantiateFunc() {
		this.func = (ComparisonFunc) PigContext.instantiateFuncFromSpec(this.funcSpec);
        this.func.setReporter(reporter);
	}
	
	public ComparisonFunc getComparator() {
		return func;
	}
	
	@Override
	public Result getNext(Integer i) throws ExecException {
		Result result = new Result();

		result.result = func.compare(t1, t2);
		result.returnStatus = (t1 != null && t2 != null) ? POStatus.STATUS_OK
				: POStatus.STATUS_ERR;
		// the two attached tuples are used up now. So we set the
		// inputAttached flag to false
		inputAttached = false;
		return result;

	}
	
	private Result getNext() {
		Result res = null;
		log.error("getNext being called with non-integer");
		return res;
	}
	
	@Override
	public Result getNext(Boolean b) throws ExecException {
		return getNext();
	}

	@Override
	public Result getNext(DataBag db) throws ExecException {
		return getNext();
	}

	@Override
	public Result getNext(DataByteArray ba) throws ExecException {
		return getNext();
	}

	@Override
	public Result getNext(Double d) throws ExecException {
		return getNext();
	}

	@Override
	public Result getNext(Float f) throws ExecException {
		return getNext();
	}

	@Override
	public Result getNext(Long l) throws ExecException {
		return getNext();
	}

	@Override
	public Result getNext(Map m) throws ExecException {
		return getNext();
	}

	@Override
	public Result getNext(String s) throws ExecException {
		return getNext();
	}

	@Override
	public Result getNext(Tuple in) throws ExecException {
		return getNext();
	}

	public void attachInput(Tuple t1, Tuple t2) {
		this.t1 = t1;
		this.t2 = t2;
		inputAttached = true;

	}
    
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException{
        is.defaultReadObject();
        instantiateFunc();
    }

    @Override
    public void visit(PhyPlanVisitor v) throws VisitorException {
        v.visitComparisonFunc(this);
    }

    @Override
    public String name() {
        return "POUserComparisonFunc" + "(" + func.getClass().getName() + ")" + "[" + DataType.findTypeName(resultType) + "]" + " - " + mKey.toString();
    }

    @Override
    public boolean supportsMultipleInputs() {
        return false;
    }
    
    public FuncSpec getFuncSpec() {
        return funcSpec;
    }

    @Override
    public POUserComparisonFunc clone() throws CloneNotSupportedException {
        FuncSpec cloneFs = null;
        if (funcSpec != null) {
            cloneFs = funcSpec.clone();
        }
        POUserComparisonFunc clone =
            new POUserComparisonFunc(new OperatorKey(mKey.scope, 
            NodeIdGenerator.getGenerator().getNextNodeId(mKey.scope)),
            requestedParallelism, null, cloneFs);
        clone.cloneHelper(this);
        return clone;
    }

}
