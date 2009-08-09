/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the
 * NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.apache.pig.piggybank.test.storage;

import static org.apache.pig.ExecType.LOCAL;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.pig.PigServer;

import org.apache.pig.ExecType;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.io.BufferedPositionedInputStream;
import org.apache.pig.impl.io.FileLocalizer;
import org.apache.pig.piggybank.storage.apachelog.CommonLogLoader;
import org.junit.Test;

public class TestCommonLogLoader extends TestCase {
    public static ArrayList<String[]> data = new ArrayList<String[]>();
    static {
        data.add(new String[] { "1.2.3.4", "-", "-", "[01/Jan/2008:23:27:45 -0600]", "\"GET /zero.html HTTP/1.0\"", "200", "100" });
        data.add(new String[] { "2.3.4.5", "-", "-", "[02/Feb/2008:23:27:48 -0600]", "\"GET /one.js HTTP/1.1\"", "201", "101" });
        data.add(new String[] { "3.4.5.6", "-", "-", "[03/Mar/2008:23:27:48 -0600]", "\"GET /two.xml HTTP/1.2\"", "202", "102" });
    }

    public static ArrayList<DataByteArray[]> EXPECTED = new ArrayList<DataByteArray[]>();
    static {

        for (int i = 0; i < data.size(); i++) {
            ArrayList<DataByteArray> thisExpected = new ArrayList<DataByteArray>();
            for (int j = 0; j <= 2; j++) {
                thisExpected.add(new DataByteArray(data.get(i)[j]));
            }
            String temp = data.get(i)[3];
            temp = temp.replace("[", "");
            temp = temp.replace("]", "");
            thisExpected.add(new DataByteArray(temp));

            temp = data.get(i)[4];

            for (String thisOne : data.get(i)[4].split(" ")) {
                thisOne = thisOne.replace("\"", "");
                thisExpected.add(new DataByteArray(thisOne));
            }
            for (int j = 5; j <= 6; j++) {
                thisExpected.add(new DataByteArray(data.get(i)[j]));
            }

            DataByteArray[] toAdd = new DataByteArray[0];
            toAdd = (DataByteArray[]) (thisExpected.toArray(toAdd));
            EXPECTED.add(toAdd);
        }
    }

    @Test
    public void testInstantiation() {
        CommonLogLoader commonLogLoader = new CommonLogLoader();
        assertNotNull(commonLogLoader);
    }

    @Test
    public void testLoadFromBindTo() throws Exception {
        String filename = TestHelper.createTempFile(data, " ");
        CommonLogLoader commonLogLoader = new CommonLogLoader();
        PigServer pigServer = new PigServer(LOCAL);
        
        InputStream inputStream = FileLocalizer.open(filename, pigServer.getPigContext());
        commonLogLoader.bindTo(filename, new BufferedPositionedInputStream(inputStream), 0, Long.MAX_VALUE);

        int tupleCount = 0;

        while (true) {
            Tuple tuple = commonLogLoader.getNext();
            if (tuple == null)
                break;
            else {
                TestHelper.examineTuple(EXPECTED, tuple, tupleCount);
                tupleCount++;
            }
        }
        assertEquals(data.size(), tupleCount);
    }

    public void testLoadFromPigServer() throws Exception {
        String filename = TestHelper.createTempFile(data, " ");
        PigServer pig = new PigServer(ExecType.LOCAL);
        filename = filename.replace("\\", "\\\\");
        pig.registerQuery("A = LOAD 'file:" + filename + "' USING org.apache.pig.piggybank.storage.apachelog.CommonLogLoader();");
        Iterator<?> it = pig.openIterator("A");

        int tupleCount = 0;

        while (it.hasNext()) {
            Tuple tuple = (Tuple) it.next();
            if (tuple == null)
                break;
            else {
                TestHelper.examineTuple(EXPECTED, tuple, tupleCount);
                tupleCount++;
            }
        }
        assertEquals(data.size(), tupleCount);
    }
}
