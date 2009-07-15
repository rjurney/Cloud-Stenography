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
package org.apache.pig.test;

import java.util.Iterator;
import java.util.Random;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.BagFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.DefaultBagFactory;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.plan.OperatorKey;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.plans.PhysicalPlan;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.expressionOperators.ConstantExpression;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.expressionOperators.ExpressionOperator;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.expressionOperators.POBinCond;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.expressionOperators.POProject;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.expressionOperators.EqualToExpr;
import org.apache.pig.impl.plan.PlanException;
import org.apache.pig.test.utils.GenPhyOp;
import org.junit.Before;

import junit.framework.TestCase;

public class TestPOBinCond extends TestCase {
    Random r = new Random();
    DataBag bag = BagFactory.getInstance().newDefaultBag();
    DataBag bagDefault = BagFactory.getInstance().newDefaultBag();
    DataBag bagWithNull = BagFactory.getInstance().newDefaultBag();
    DataBag bagWithBoolean = BagFactory.getInstance().newDefaultBag();
    DataBag bagWithBooleanAndNull = BagFactory.getInstance().newDefaultBag();

    final int MAX = 10;

    /***
     *  POBinCondition tests
     *  
     *  (r1, 1, 0 )
     *  (r2, 1, 0 )
     *  (r3, 1, 0 )
     *  ...
     *  (rn, 1, 0 )
     *    
     *   where r is a random number ( r1 .. rn )
     *   
     *   The POBinCondition to test is:  Integer(result)= ( r == 1 )? Ingeger(1), Ingeger(0);
     *   but the condition can be of any datatype: Interger, Float, Double...
     *   
     * @throws ExecException
     */
    
    @Before
    @Override
    public void setUp() {
    	
    	//default bag as shown above
        for(int i = 0; i < 10; i ++) {
            Tuple t = TupleFactory.getInstance().newTuple();
            t.append(r.nextInt(2));
            t.append(1);
            t.append(0);
            bagDefault.add(t);
        }
       
        //same as default bag but contains nulls
        for(int i = 0; i < 10; i ++) {
            Tuple t = TupleFactory.getInstance().newTuple();
            if (r.nextInt(4)%3 == 0){
            	t.append(null);
            	
            }else{
                t.append(r.nextInt(2));
            }
            t.append(1);
            t.append(0);
            bagWithNull.add(t);

        }
        
        //r is a boolean  
        for(int i = 0; i < 10; i ++) {
            Tuple t = TupleFactory.getInstance().newTuple();
            if (r.nextInt(2)%2 == 0 ){
            		t.append(true);
            } else  {
            		t.append(false);
            }
            t.append(1);
            t.append(0);
            bagWithBoolean.add(t);

    	}
    
        //r is a boolean with nulls
        for(int i = 0; i < 10; i ++) {

            Tuple t = TupleFactory.getInstance().newTuple();
            if (r.nextInt(3)%2 == 0){
            	
             	t.append(null);
             	
            }else{
            	
               	if (r.nextInt(2)%2 == 0 ){
            		t.append(true);
            	} else {
            		t.append(false);
            	}

            }
            t.append(1);
            t.append(0);
            bagWithBooleanAndNull.add(t);

        }

        
    }
  
    /* ORIGINAL TEST
    public void testPOBinCond() throws ExecException, PlanException {
        ConstantExpression rt = (ConstantExpression) GenPhyOp.exprConst();
        rt.setValue(1);
        rt.setResultType(DataType.INTEGER);
        
        POProject prj1 = GenPhyOp.exprProject();
        prj1.setColumn(0);
        prj1.setResultType(DataType.INTEGER);
        
        EqualToExpr equal = (EqualToExpr) GenPhyOp.compEqualToExpr();
        equal.setLhs(prj1);
        equal.setRhs(rt);
        equal.setOperandType(DataType.INTEGER);
        
        POProject prjLhs = GenPhyOp.exprProject();
        prjLhs.setResultType(DataType.INTEGER);
        prjLhs.setColumn(1);
        
        POProject prjRhs = GenPhyOp.exprProject();
        prjRhs.setResultType(DataType.INTEGER);
        prjRhs.setColumn(2);
        
        POBinCond op = new POBinCond(new OperatorKey("", r.nextLong()), -1, equal, prjLhs, prjRhs);
        op.setResultType(DataType.INTEGER);
        
        PhysicalPlan plan = new PhysicalPlan();
        plan.add(op);
        plan.add(prjLhs);
        plan.add(prjRhs);
        plan.add(equal);
        plan.connect(equal, op);
        plan.connect(prjLhs, op);
        plan.connect(prjRhs, op);
        
        plan.add(prj1);
        plan.add(rt);
        plan.connect(prj1, equal);
        plan.connect(rt, equal);
        
        for(Iterator<Tuple> it = bag.iterator(); it.hasNext(); ) {
            Tuple t = it.next();
            plan.attachInput(t);
            Integer i = (Integer) t.get(0);
            assertEquals(1, i | (Integer)op.getNext(i).result);
//            System.out.println(t + " " + op.getNext(i).result.toString());
        }
        
        
    }
  */
    

    public void testPOBinCondWithInteger() throws  ExecException, PlanException {
    	
	    bag= getBag(DataType.INTEGER);
    	TestPoBinCondHelper testHelper= new TestPoBinCondHelper(DataType.INTEGER, new Integer(1) );
 
    	for(Iterator<Tuple> it = bag.iterator(); it.hasNext(); ) {
            Tuple t = it.next();
            testHelper.getPlan().attachInput(t);
            Integer value = (Integer) t.get(0);
            int expected = (value.intValue() == 1)? 1:0 ;
            Integer result=(Integer)testHelper.getOperator().getNext(value).result;
            int actual = result.intValue();
            assertEquals( expected, actual );
        }

    }
   
    public void testPOBinCondWithLong() throws  ExecException, PlanException {
        bag= getBag(DataType.LONG);
       	TestPoBinCondHelper testHelper= new TestPoBinCondHelper(DataType.LONG, new Long(1L) );
    
       	for(Iterator<Tuple> it = bag.iterator(); it.hasNext(); ) {
               Tuple t = it.next();
               testHelper.getPlan().attachInput(t);
               Long value = (Long) t.get(0);
               int expected = (value.longValue() == 1L )? 1:0 ;
               Integer dummy = new Integer(0);
               Integer result=(Integer)testHelper.getOperator().getNext(dummy).result;
               int actual = result.intValue();
               assertEquals( expected, actual );
        }
    }

    public void testPOBinCondWithFloat() throws  ExecException, PlanException {
	   	
		bag= getBag(DataType.FLOAT);
	   	TestPoBinCondHelper testHelper= new TestPoBinCondHelper(DataType.FLOAT, new Float(1.0f) );

	   	for(Iterator<Tuple> it = bag.iterator(); it.hasNext(); ) {
	           Tuple t = it.next();
	           testHelper.getPlan().attachInput(t);
	           Float value = (Float) t.get(0);
	           int expected = (value.floatValue() == 1.0f )? 1:0 ;
	           Integer dummy = new Integer(0);
	           Integer result=(Integer)testHelper.getOperator().getNext(dummy).result;
	           int actual = result.intValue();
	           assertEquals( expected, actual );
	    }

	}
   
    public void testPOBinCondWithDouble() throws  ExecException, PlanException {
	   	
		bag= getBag(DataType.DOUBLE);
	   	TestPoBinCondHelper testHelper= new TestPoBinCondHelper(DataType.DOUBLE, new Double(1.0) );

	   	for(Iterator<Tuple> it = bag.iterator(); it.hasNext(); ) {
	           Tuple t = it.next();
	           testHelper.getPlan().attachInput(t);
	           Double value = (Double) t.get(0);
	           int expected = (value.doubleValue() == 1.0 )? 1:0 ;
	           Integer dummy = new Integer(0);
	           Integer result=(Integer)testHelper.getOperator().getNext(dummy).result;
	           int actual = result.intValue();
	           assertEquals( expected, actual );
	    }

    }
   
    public void testPOBinCondIntWithNull() throws  ExecException, PlanException {
   	
    	bag= getBagWithNulls(DataType.INTEGER);
       	TestPoBinCondHelper testHelper= new TestPoBinCondHelper(DataType.INTEGER, new Integer(1) );
    
       	for(Iterator<Tuple> it = bag.iterator(); it.hasNext(); ) {
            Tuple t = it.next();
            testHelper.getPlan().attachInput(t);
            Integer value = null;
            Integer result;

            if (t.get(0) != null) {
                value = (Integer) t.get(0);
                result = (Integer) testHelper.getOperator().getNext(value).result;
            } else {
                result = (Integer) testHelper.getOperator().getNext(
                        (Integer) null).result;
            }
            int actual;
            if (value != null) {
                int expected = (value.intValue() == 1) ? 1 : 0;
                actual = result.intValue();
                assertEquals(expected, actual);
            } else {
                assertEquals(null, result);
            }
 
       }

   }
   
    public void testPOBinCondLongWithNull() throws  ExecException, PlanException {
	   	
	    bag= getBagWithNulls(DataType.LONG);
	   	TestPoBinCondHelper testHelper= new TestPoBinCondHelper(DataType.LONG, new Long(1L) );

	   	for(Iterator<Tuple> it = bag.iterator(); it.hasNext(); ) {
	           Tuple t = it.next();
	           testHelper.getPlan().attachInput(t);
	           
	           Long value=null;
	           if ( t.get(0)!=null){
	        	   value = (Long) t.get(0);
	           }
	           Integer dummy = new Integer(0);
	           Integer result=(Integer)testHelper.getOperator().getNext(dummy).result;	                
	           int expected;
	           int actual;
	           if ( value!=null ) {
	        	   expected=(value.intValue() == 1)? 1:0 ;
	        	   actual  = result.intValue();
	        	   assertEquals( expected, actual );
	           } else {
	        	   assertEquals( null, result );
	           }
	       }
	}
   
    public void testPOBinCondDoubleWithNull() throws  ExecException, PlanException {
	   	
	    bag= getBagWithNulls(DataType.DOUBLE);
	   	TestPoBinCondHelper testHelper= new TestPoBinCondHelper(DataType.DOUBLE, new Double(1.0) );

	   	for(Iterator<Tuple> it = bag.iterator(); it.hasNext(); ) {
	           Tuple t = it.next();
	           testHelper.getPlan().attachInput(t);

	           Double value=null;
	           if ( t.get(0)!=null){
	        	   value = (Double) t.get(0);
	           }
	           Integer dummy = new Integer(0);
	           Integer result=(Integer)testHelper.getOperator().getNext(dummy).result;
	                
	           int expected;
	           int actual;
	           if ( value!=null ) {
	        	   expected=(value.intValue() == 1)? 1:0 ;
	        	   actual  = result.intValue();
	        	   assertEquals( expected, actual );
	           } else {
	        	   assertEquals( null, result );
	           }
	          

	       }

	}
   
    protected class TestPoBinCondHelper {
    	
    	 PhysicalPlan plan= null;
     	 POBinCond op= null;
    	 
   
		public <U> TestPoBinCondHelper(   byte type,  U value  )  throws  ExecException, PlanException {
		    	
				
		        ConstantExpression rt = (ConstantExpression) GenPhyOp.exprConst();
		        rt.setValue(value);
		        rt.setResultType(type);
		        
		        POProject prj1 = GenPhyOp.exprProject();
		        prj1.setColumn(0);
		        prj1.setResultType(type);

		        
		        EqualToExpr equal = (EqualToExpr) GenPhyOp.compEqualToExpr();
		        equal.setLhs(prj1);
		        equal.setRhs(rt);
		        equal.setOperandType(type);
		        
		        POProject prjLhs = GenPhyOp.exprProject();
		        prjLhs.setResultType(DataType.INTEGER);
		        prjLhs.setColumn(1);
		        
		        POProject prjRhs =prjRhs = GenPhyOp.exprProject();
		        prjRhs.setResultType(DataType.INTEGER);
		        prjRhs.setColumn(2);
		     
		        op = new POBinCond(new OperatorKey("", r.nextLong()), -1, equal, prjLhs, prjRhs);
		        op.setResultType(DataType.INTEGER);
		       
		        plan= new PhysicalPlan();
		        plan.add(op);
		        plan.add(prjLhs);
		        plan.add(prjRhs);
		        plan.add(equal);
		        plan.connect(equal, op);
		        plan.connect(prjLhs, op);
		        plan.connect(prjRhs, op);
		        
		        plan.add(prj1);
		        plan.add(rt);
		        plan.connect(prj1, equal);
		        plan.connect(rt, equal);
		        
		       // File tmpFile = File.createTempFile("test", ".txt" );
		       //PrintStream ps = new PrintStream(new FileOutputStream(tmpFile));
		       //plan.explain(ps);
		       //ps.close();

		    }
		
		public PhysicalPlan getPlan(){
			return plan;
		}

		
		public POBinCond getOperator(){
			return op;
		}


	}
    
    private DataBag getBag(byte type) {
        DataBag bag = DefaultBagFactory.getInstance().newDefaultBag();
        for(int i = 0; i < 10; i ++) {
            Tuple t = TupleFactory.getInstance().newTuple();
            switch(type) {
                case DataType.INTEGER:
                    t.append(r.nextInt(2));
                    break;
                case DataType.LONG:
                    t.append(r.nextLong() % 2L);
                    break;
                case DataType.FLOAT:
                    t.append((i % 2 == 0 ? 1.0f : 0.0f));
                    break;
                case DataType.DOUBLE:
                    t.append((i % 2 == 0 ? 1.0 : 0.0));
                    break;                
            }
            t.append(1);
            t.append(0);
            bag.add(t);
        }        
        return bag;        
    }
    
    private DataBag getBagWithNulls(byte type) {
        DataBag bag = DefaultBagFactory.getInstance().newDefaultBag();
        for(int i = 0; i < 10; i ++) {
            Tuple t = TupleFactory.getInstance().newTuple();
            if (r.nextInt(4)%3 == 0){
                t.append(null);                
            }else{
                switch(type) {
                    case DataType.INTEGER:
                        t.append(r.nextInt(2));
                        break;
                    case DataType.LONG: 
                        t.append(r.nextLong() % 2L);
                        break;
                    case DataType.FLOAT:
                        t.append( (i % 2 == 0 ? 1.0f : 0.0f));
                        break;
                    case DataType.DOUBLE:
                        t.append( (i % 2 == 0 ? 1.0 : 0.0));
                        break;
                }
            }            
            t.append(1);
            t.append(0);
            bag.add(t);
        }
        return bag;
    }
}
