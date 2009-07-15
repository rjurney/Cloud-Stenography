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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.hadoop.io.WritableComparable;

import org.apache.pig.impl.util.Spillable;

/**
 * A collection of Tuples.  A DataBag may or may not fit into memory.
 * DataBag extends spillable, which means that it registers with a memory
 * manager.  By default, it attempts to keep all of its contents in memory.
 * If it is asked by the memory manager to spill to disk (by a call to
 * spill()), it takes whatever it has in memory, opens a spill file, and
 * writes the contents out.  This may happen multiple times.  The bag
 * tracks all of the files it's spilled to.
 * 
 * DataBag provides an Iterator interface, that allows callers to read
 * through the contents.  The iterators are aware of the data spilling.
 * They have to be able to handle reading from files, as well as the fact
 * that data they were reading from memory may have been spilled to disk
 * underneath them.
 *
 * The DataBag interface assumes that all data is written before any is
 * read.  That is, a DataBag cannot be used as a queue.  If data is written
 * after data is read, the results are undefined.  This condition is not
 * checked on each add or read, for reasons of speed.  Caveat emptor.
 *
 * Since spills are asynchronous (the memory manager requesting a spill
 * runs in a separate thread), all operations dealing with the mContents
 * Collection (which is the collection of tuples contained in the bag) have
 * to be synchronized.  This means that reading from a DataBag is currently
 * serialized.  This is ok for the moment because pig execution is
 * currently single threaded.  A ReadWriteLock was experimented with, but
 * it was found to be about 10x slower than using the synchronize keyword.
 * If pig changes its execution model to be multithreaded, we may need to
 * return to this issue, as synchronizing reads will most likely defeat the
 * purpose of multi-threading execution.
 *
 * DataBags come in several types, default, sorted, and distinct.  The type
 * must be chosen up front, there is no way to convert a bag on the fly.
 */
public interface DataBag extends Spillable, WritableComparable, Iterable<Tuple>, Serializable {
    /**
     * Get the number of elements in the bag, both in memory and on disk.
     */
    long size();

    /**
     * Find out if the bag is sorted.
     */
    boolean isSorted();
    
    /**
     * Find out if the bag is distinct.
     */
    boolean isDistinct();
    
    /**
     * Get an iterator to the bag. For default and distinct bags,
     * no particular order is guaranteed. For sorted bags the order
     * is guaranteed to be sorted according
     * to the provided comparator.
     */
    Iterator<Tuple> iterator();

    /**
     * Add a tuple to the bag.
     * @param t tuple to add.
     */
    void add(Tuple t);

    /**
     * Add contents of a bag to the bag.
     * @param b bag to add contents of.
     */
    void addAll(DataBag b);

    /**
     * Clear out the contents of the bag, both on disk and in memory.
     * Any attempts to read after this is called will produce undefined
     * results.
     */
    void clear();

    /**
     * This is used by FuncEvalSpec.FakeDataBag.
     * @param stale Set stale state.
     */
    void markStale(boolean stale);
}
