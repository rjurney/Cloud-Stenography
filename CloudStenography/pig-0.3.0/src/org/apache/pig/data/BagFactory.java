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
import java.lang.ClassLoader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;
import java.util.List;

import org.apache.pig.impl.util.SpillableMemoryManager;

/**
 * Factory for constructing different types of bags.
 * This class is abstract so that users can
 * override the bag factory if they desire to provide their own that
 * returns their implementation of a bag.  If the property
 * pig.data.bag.factory.name is set to a class name and
 * pig.data.bag.factory.jar is set to a URL pointing to a jar that
 * contains the above named class, then getInstance() will create a
 * a instance of the named class using the indicatd jar.  Otherwise, it
 * will create an instance of DefaultBagFactory.
 */
public abstract class BagFactory {
    private static BagFactory gSelf = null;
    private static SpillableMemoryManager gMemMgr;

    /**
     * Get a reference to the singleton factory.
     */
    public static BagFactory getInstance() {
        if (gSelf == null) {
            String factoryName =
                System.getProperty("pig.data.bag.factory.name");
            String factoryJar =
                System.getProperty("pig.data.bag.factory.jar");
            if (factoryName != null && factoryJar != null) {
                try {
                    URL[] urls = new URL[1];
                    urls[0] = new URL(factoryJar);
                    ClassLoader loader = new URLClassLoader(urls,
                        BagFactory.class.getClassLoader());
                    Class c = Class.forName(factoryName, true, loader);
                    Object o = c.newInstance();
                    if (!(o instanceof BagFactory)) {
                        throw new RuntimeException("Provided factory " +
                            factoryName + " does not extend BagFactory!");
                    }
                    gSelf = (BagFactory)o;
                } catch (Exception e) {
                    if (e instanceof RuntimeException) {
                        // We just threw this
                        RuntimeException re = (RuntimeException)e;
                        throw re;
                    }
                    throw new RuntimeException("Unable to instantiate "
                        + "bag factory " + factoryName, e);
                }
            } else {
                gSelf = new DefaultBagFactory();
            }
        }
        return gSelf;
    }
    
    /**
     * Get a default (unordered, not distinct) data bag.
     */
    public abstract DataBag newDefaultBag();

    /**
     * Get a default (unordered, not distinct) data bag from
     * an existing list of tuples.
     */
    public abstract DataBag newDefaultBag(List<Tuple> listOfTuples);
    
    /**
     * Get a sorted data bag.
     * @param comp Comparator that controls how the data is sorted.
     * If null, default comparator will be used.
     */
    public abstract DataBag newSortedBag(Comparator<Tuple> comp);
    
    /**
     * Get a distinct data bag.
     */
    public abstract DataBag newDistinctBag();

    protected BagFactory() {
        gMemMgr = new SpillableMemoryManager();
    }

    protected void registerBag(DataBag b) {
        gMemMgr.registerSpillable(b);
    }

    /**
     * Provided for testing purposes only.  This function should never be
     * called by anybody but the unit tests.
     */
    public static void resetSelf() {
        gSelf = null;
    }

}

