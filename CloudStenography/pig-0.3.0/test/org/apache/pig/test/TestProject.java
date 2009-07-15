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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayList;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.data.Tuple;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.POStatus;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.Result;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.expressionOperators.POProject;
import org.apache.pig.test.utils.GenPhyOp;
import org.apache.pig.test.utils.GenRandomData;
import org.apache.pig.test.utils.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestProject extends  junit.framework.TestCase {
    Random r;

    Tuple t, tRandom, tRandomAndNull;

    Result res;

    POProject proj;

    @Before
    public void setUp() throws Exception {
        r = new Random();
        tRandom = GenRandomData.genRandSmallBagTuple(r, 10, 100);
        tRandomAndNull = GenRandomData.genRandSmallBagTupleWithNulls(r, 10, 100);
        res = new Result();
        proj = GenPhyOp.exprProject();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetNext() throws ExecException, IOException {
    	t=tRandom;
        proj.attachInput(t);
        for (int j = 0; j < t.size(); j++) {
            proj.attachInput(t);
            proj.setColumn(j);

            res = proj.getNext();
            assertEquals(POStatus.STATUS_OK, res.returnStatus);
            assertEquals(t.get(j), res.result);
        }
    }

    

    @Test
    public void testGetNextTuple() throws IOException, ExecException {
    	t=tRandom;
        proj.attachInput(t);
        proj.setColumn(0);
        proj.setOverloaded(true);
        DataBag inpBag = (DataBag) t.get(0);
        int cntr = 0;
        boolean contains = true;
        while (true) {
            res = proj.getNext(t);
            if (res.returnStatus == POStatus.STATUS_EOP)
                break;
            if (!TestHelper.bagContains(inpBag, (Tuple) res.result)) {
                contains = false;
                break;
            }
            ++cntr;
        }
        assertEquals((float) (inpBag).size(), (float) cntr, 0.01f);
        assertEquals(true, contains);

        proj.attachInput(t);
        proj.setColumn(9);
        proj.setOverloaded(false);
        res = proj.getNext(t);
        assertEquals(POStatus.STATUS_OK, res.returnStatus);
        assertEquals(t.get(9), res.result);
    }




    @Test
    public void testGetNextMultipleProjections() throws ExecException, IOException {
    	t=tRandom;
        ArrayList<Integer> cols = new ArrayList<Integer>();
        proj.attachInput(t);
        for (int j = 0; j < t.size() - 1; j++) {
            proj.attachInput(t);
            cols.add(j);
            cols.add(j+1);
            proj.setColumns(cols);

            res = proj.getNext();
	        TupleFactory tupleFactory = TupleFactory.getInstance();
	        ArrayList<Object> objList = new ArrayList<Object>(); 
            objList.add(t.get(j)); 
            objList.add(t.get(j+1)); 
		    Tuple expectedResult = tupleFactory.newTuple(objList);
            assertEquals(POStatus.STATUS_OK, res.returnStatus);
            assertEquals(expectedResult, res.result);
            cols.clear();
        }
    }

    

    
    @Test
    public void testGetNextTupleMultipleProjections() throws IOException, ExecException {
    	t=tRandom;
        proj.attachInput(t);
        proj.setOverloaded(true);
        int j = 0;
        ArrayList<Integer> cols = new ArrayList<Integer>();

        while (true) {
            cols.add(j);
            cols.add(j+1);
            proj.setColumns(cols);
            res = proj.getNext(t);
            if (res.returnStatus == POStatus.STATUS_EOP)
                break;
	        TupleFactory tupleFactory = TupleFactory.getInstance();
	        ArrayList<Object> objList = new ArrayList<Object>(); 
            objList.add(t.get(j)); 
            objList.add(t.get(j+1)); 
		    Tuple expectedResult = tupleFactory.newTuple(objList);
            assertEquals(POStatus.STATUS_OK, res.returnStatus);
            assertEquals(expectedResult, res.result);
            ++j;
            cols.clear();
        }

        proj.attachInput(t);
        proj.setColumn(9);
        proj.setOverloaded(false);
        res = proj.getNext(t);
        assertEquals(POStatus.STATUS_OK, res.returnStatus);
        assertEquals(t.get(9), res.result);
    }
    
    @Test
    public void testGetNextWithNull() throws ExecException, IOException {
    	t= tRandomAndNull;
        proj.attachInput(t);
        for (int j = 0; j < t.size(); j++) {
            proj.attachInput(t);
            proj.setColumn(j);

            res = proj.getNext();
            assertEquals(POStatus.STATUS_OK, res.returnStatus);
            assertEquals(t.get(j), res.result);
        }
    }

    
	@Test
    public void testGetNextTupleWithNull() throws IOException, ExecException {
    	t= tRandomAndNull;
        proj.attachInput(t);
        proj.setColumn(0);
        proj.setOverloaded(true);
        DataBag inpBag = (DataBag) t.get(0);
        int cntr = 0;
        boolean contains = true;
        while (true) {
            res = proj.getNext(t);
            if (res.returnStatus == POStatus.STATUS_EOP)
                break;
            if (!TestHelper.bagContains(inpBag, (Tuple) res.result)) {
                contains = false;
                break;
            }
            ++cntr;
        }
        assertEquals((float) (inpBag).size(), (float) cntr, 0.01f);
        assertEquals(true, contains);

        proj.attachInput(t);
        proj.setColumn(9);
        proj.setOverloaded(false);
        res = proj.getNext(t);
        assertEquals(POStatus.STATUS_OK, res.returnStatus);
        assertEquals(t.get(9), res.result);
    }




    @Test
    public void testGetNextMultipleProjectionsWithNull() throws ExecException, IOException {
    	t= tRandomAndNull;
        ArrayList<Integer> cols = new ArrayList<Integer>();
        proj.attachInput(t);
        for (int j = 0; j < t.size() - 1; j++) {
            proj.attachInput(t);
            cols.add(j);
            cols.add(j+1);
            proj.setColumns(cols);

            res = proj.getNext();
	        TupleFactory tupleFactory = TupleFactory.getInstance();
	        ArrayList<Object> objList = new ArrayList<Object>(); 
            objList.add(t.get(j)); 
            objList.add(t.get(j+1)); 
		    Tuple expectedResult = tupleFactory.newTuple(objList);
            assertEquals(POStatus.STATUS_OK, res.returnStatus);
            assertEquals(expectedResult, res.result);
            cols.clear();
        }
    }

    

    
    @Test
    public void testGetNextTupleMultipleProjectionsWithNull() throws IOException, ExecException {
    	t= tRandomAndNull;
        proj.attachInput(t);
        proj.setOverloaded(true);
        int j = 0;
        ArrayList<Integer> cols = new ArrayList<Integer>();

        while (true) {
            cols.add(j);
            cols.add(j+1);
            proj.setColumns(cols);
            res = proj.getNext(t);
            if (res.returnStatus == POStatus.STATUS_EOP)
                break;
	        TupleFactory tupleFactory = TupleFactory.getInstance();
	        ArrayList<Object> objList = new ArrayList<Object>(); 
            objList.add(t.get(j)); 
            objList.add(t.get(j+1)); 
		    Tuple expectedResult = tupleFactory.newTuple(objList);
            assertEquals(POStatus.STATUS_OK, res.returnStatus);
            assertEquals(expectedResult, res.result);
            ++j;
            cols.clear();
        }

        proj.attachInput(t);
        proj.setColumn(9);
        proj.setOverloaded(false);
        res = proj.getNext(t);
        assertEquals(POStatus.STATUS_OK, res.returnStatus);
        assertEquals(t.get(9), res.result);
    }

}
