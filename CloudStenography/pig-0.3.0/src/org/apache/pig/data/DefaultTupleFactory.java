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
package org.apache.pig.data;

import java.lang.Class;
import java.util.List;

import org.apache.pig.backend.executionengine.ExecException;

/**
 * Default implementation of TupleFactory.
 */
public class DefaultTupleFactory extends TupleFactory {
    public Tuple newTuple() {
        return new DefaultTuple();
    
    }

    public Tuple newTuple(int size) {
        return new DefaultTuple(size);
    }
    
    public Tuple newTuple(List c) {
        return new DefaultTuple(c);
    }

    public Tuple newTupleNoCopy(List list) {
        return new DefaultTuple(list, 1);
    }

    public Tuple newTuple(Object datum) {
        Tuple t = new DefaultTuple(1);
        try {
            t.set(0, datum);
        } catch (ExecException e) {
            // The world has come to an end, we just allocated a tuple with one slot
            // but we can't write to that slot.
            throw new RuntimeException("Unable to write to field 0 in newly " +
                "allocated tuple of size 1!", e);
        }
        return t;
    }

    public Class tupleClass() {
        return DefaultTuple.class;
    }

    DefaultTupleFactory() {
    }

}

