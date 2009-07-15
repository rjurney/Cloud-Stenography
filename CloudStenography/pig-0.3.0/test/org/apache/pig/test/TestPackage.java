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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.hadoop.HDataType;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.POStatus;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.Result;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.relationalOperators.POPackage;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.DefaultBagFactory;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.io.NullableTuple;
import org.apache.pig.impl.io.PigNullableWritable;
import org.apache.pig.impl.plan.OperatorKey;
import org.apache.pig.impl.util.Pair;
import org.apache.pig.test.utils.GenRandomData;
import org.apache.pig.test.utils.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestPackage extends junit.framework.TestCase {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    private void runTest(Object key, boolean inner[]) throws ExecException, IOException {
        Random r = new Random();
        DataBag db1 = GenRandomData.genRandSmallTupDataBag(r, 10, 100);
        DataBag db2 = GenRandomData.genRandSmallTupDataBag(r, 10, 100);
        List<NullableTuple> db = new ArrayList<NullableTuple>(200);
        Iterator db1Iter = db1.iterator();
        if(!inner[0]){
            while (db1Iter.hasNext()) {
                NullableTuple it = new NullableTuple((Tuple)db1Iter.next());
                it.setIndex((byte)0);
                db.add(it);
            }
        }
        Iterator db2Iter = db2.iterator();
        while (db2Iter.hasNext()) {
            NullableTuple it = new NullableTuple((Tuple) db2Iter.next());
            it.setIndex((byte)1);
            db.add(it);
        }
        //ITIterator iti = new TestPackage.ITIterator(db.iterator());
        POPackage pop = new POPackage(new OperatorKey("", r.nextLong()));
        pop.setNumInps(2);
        pop.setInner(inner);
        PigNullableWritable k = HDataType.getWritableComparableTypes(key, (byte)0);
        pop.attachInput(k, db.iterator());
        
        // we are not doing any optimization to remove
        // parts of the "value" which are present in the "key" in this
        // unit test - so set up the "keyInfo" accordingly in 
        // the POPackage
        Map<Integer, Pair<Boolean, Map<Integer, Integer>>> keyInfo = 
            new HashMap<Integer, Pair<Boolean, Map<Integer,Integer>>>();
        Pair<Boolean, Map<Integer, Integer>> p = 
            new Pair<Boolean, Map<Integer, Integer>>(false, new HashMap<Integer, Integer>());
        keyInfo.put(0, p);
        keyInfo.put(1, p);
        pop.setKeyInfo(keyInfo);
        Tuple t = null;
        Result res = null;
        res = (Result) pop.getNext(t);
        if(res.returnStatus==POStatus.STATUS_NULL && inner[0]) return;
        assertEquals(POStatus.STATUS_OK, res.returnStatus);

        t = (Tuple) res.result;
        Object outKey = t.get(0);
        DataBag outDb1 = (DataBag) t.get(1);
        DataBag outDb2 = (DataBag) t.get(2);

        assertEquals(key, outKey);
        assertTrue(TestHelper.compareBags(db1, outDb1));
        assertTrue(TestHelper.compareBags(db2, outDb2));
    }

    /**
     * To show that it does not have any type specific
     * code
     */
    private void pickTest(byte t, boolean[] inner) throws ExecException, IOException {
        Random r = new Random();
        switch (t) {
        case DataType.BAG:
            runTest(GenRandomData.genRandSmallTupDataBag(r, 10, 100),inner);
            break;
        case DataType.BOOLEAN:
            runTest(r.nextBoolean(),inner);
            break;
        case DataType.BYTEARRAY:
            runTest(GenRandomData.genRandDBA(r),inner);
            break;
        case DataType.BIGCHARARRAY: {
			String s = GenRandomData.genRandString(r);			
			for(;s.length() < 65535;) {
				s += GenRandomData.genRandString(r);
			}
			runTest(s,inner);
        	break;
        }        	
        case DataType.CHARARRAY:
            runTest(GenRandomData.genRandString(r),inner);
            break;
        case DataType.DOUBLE:
            runTest(r.nextDouble(),inner);
            break;
        case DataType.FLOAT:
            runTest(r.nextFloat(),inner);
            break;
        case DataType.INTEGER:
            runTest(r.nextLong(),inner);
            break;
        case DataType.LONG:
            runTest(r.nextLong(),inner);
            break;
        case DataType.MAP:
        case DataType.BYTE:
            return; // map not key type
        case DataType.TUPLE:
            runTest(GenRandomData.genRandSmallBagTuple(r, 10, 100),inner);
            break;

        default:
            fail("No test case for type " + DataType.findTypeName(t));
        }
    }

    @Test
    public void testOperator() throws ExecException, IOException{
        byte[] types = DataType.genAllTypes();
        for (byte b : types) {
            System.out.println("Type " + DataType.findTypeName(b));
            boolean succ = true;
            int NUM_TRIALS = 10;
            boolean[] inner1 = { false , false };
            for (int i = 0; i < NUM_TRIALS; i++)
                pickTest(b, inner1);
            
            boolean[] inner2 = { true , false };
            for (int i = 0; i < NUM_TRIALS; i++)
                pickTest(b, inner2);
            /*if (succ)
                System.out.println("Success!!");
            else
                System.out.println("Failure");*/
        }
    }
}
