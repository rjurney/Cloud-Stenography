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

import junit.framework.TestCase;
import org.junit.Test;
import org.apache.pig.impl.PigContext;

public class TestInstantiateFunc extends TestCase {

    static final String TEST_CLASS
                    = "org.apache.pig.test.TestInstantiateFunc$TestClass" ;

    static final String TEST_CLASS2
                    = "org.apache.pig.test.TestInstantiateFunc$TestClass2" ;

    // Positive case
    @Test
    public void testInstantiate1() throws Throwable {
        PigContext.instantiateFuncFromSpec(TEST_CLASS) ;
    }

    // Positive case
    @Test
    public void testInstantiate2() throws Throwable {
        PigContext.instantiateFuncFromSpec(TEST_CLASS + "('a','b')") ;
    }

    // Positive case
    @Test
    public void testInstantiate3() throws Throwable {
        PigContext.instantiateFuncFromSpec(TEST_CLASS + "('a','b','c')") ;
    }

    // Negative case
    @Test
    public void testInstantiate4() throws Throwable {
        try {
            PigContext.instantiateFuncFromSpec(TEST_CLASS2) ;
            fail("expect error here") ;
        } catch(Exception e) {
            // good
        }
    }

    // Negative case
    @Test
    public void testInstantiate5() throws Throwable {
        try {
            PigContext.instantiateFuncFromSpec(TEST_CLASS2 + "('a','b','c')") ;
            fail("expect error here") ;
        } catch(Exception e) {
            // good
        }
    }

    public static class TestClass{
        public TestClass() {

        }
        public TestClass(String a, String b) {

        }
        public TestClass(String... strList) {

        }
    }

    public static class TestClass2{
        public TestClass2(String a, String b) {

        }
    }
}
