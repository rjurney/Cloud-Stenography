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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.pig.ComparisonFunc;
import org.apache.pig.FuncSpec;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.builtin.AVG;
import org.apache.pig.builtin.COUNT;
import org.apache.pig.builtin.SUM;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.PigContext;
import org.apache.pig.impl.builtin.GFCross;
import org.apache.pig.impl.logicalLayer.LogicalPlan;
import org.apache.pig.impl.plan.OperatorKey;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.MRCompiler;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.MRCompilerException;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.MapReduceOper;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.plans.MROperPlan;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.PhysicalOperator;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.plans.PhysicalPlan;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.plans.PlanPrinter;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.relationalOperators.*;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.expressionOperators.POProject;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.expressionOperators.POUserComparisonFunc;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.expressionOperators.POUserFunc;
import org.apache.pig.impl.plan.PlanException;
import org.apache.pig.impl.plan.VisitorException;
import org.apache.pig.test.TestPOSort.WeirdComparator;
import org.apache.pig.test.utils.GenPhyOp;
import org.apache.pig.test.utils.LogicalPlanTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases to test the MRCompiler.
 * VERY IMPORTANT NOTE: The tests here compare results with a
 * "golden" set of outputs. In each testcase here, the operators
 * generated have a random operator key which uses Java's Random 
 * class. So if there is a code change which changes the number of
 * operators created in a plan, then not only will the "golden" file
 * for that test case need to be changed, but also for the tests
 * that follow it since the operator keys that will be generated through
 * Random will be different.
 */
public class TestMRCompiler extends junit.framework.TestCase {
//    MiniCluster cluster = MiniCluster.buildCluster();
    
    static PigContext pc;

    static final int MAX_SIZE = 100000;

    static final long SEED = 1013;
    
    static Random r;
    static{
        pc = new PigContext();
        try {
            pc.connect();
        } catch (ExecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        r = new Random(SEED);
    }
    
    LogicalPlanTester planTester = new LogicalPlanTester() ;

    // if for some reason, the golden files need
    // to be regenerated, set this to true - THIS
    // WILL OVERWRITE THE GOLDEN FILES - So use this
    // with caution and only for the testcases you need
    // and are sure of
    private boolean generate = false;

    @Before
    public void setUp() throws ExecException {
        GenPhyOp.setR(r);
        
        GenPhyOp.setPc(pc);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testRun1() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        PhysicalPlan part1 = new PhysicalPlan();
        POLoad lC = GenPhyOp.topLoadOp();
        POFilter fC = GenPhyOp.topFilterOp();
        fC.setRequestedParallelism(20);
        POLocalRearrange lrC = GenPhyOp.topLocalRearrangeOp();
        lrC.setRequestedParallelism(10);
        POGlobalRearrange grC = GenPhyOp.topGlobalRearrangeOp();
        POPackage pkC = GenPhyOp.topPackageOp();
        part1.add(lC);
        part1.add(fC);
        part1.connect(lC, fC);
        part1.add(lrC);
        part1.connect(fC, lrC);
        part1.add(grC);
        part1.connect(lrC, grC);
        part1.add(pkC);
        part1.connect(grC, pkC);

        POPackage pkD = GenPhyOp.topPackageOp();
        pkD.setRequestedParallelism(20);
        POLocalRearrange lrD = GenPhyOp.topLocalRearrangeOp();
        lrD.setRequestedParallelism(30);
        POGlobalRearrange grD = GenPhyOp.topGlobalRearrangeOp();
        POLoad lD = GenPhyOp.topLoadOp();
        part1.add(lD);
        part1.add(lrD);
        part1.connect(lD, lrD);

        part1.add(grD);
        part1.connect(lrD, grD);
        part1.add(pkD);
        part1.connect(grD, pkD);

        POPackage pkCD = GenPhyOp.topPackageOp();
        POLocalRearrange lrCD1 = GenPhyOp.topLocalRearrangeOp();
        POLocalRearrange lrCD2 = GenPhyOp.topLocalRearrangeOp();
        POGlobalRearrange grCD = GenPhyOp.topGlobalRearrangeOp();
        part1.add(lrCD1);
        part1.add(lrCD2);
        part1.connect(pkC, lrCD1);
        part1.connect(pkD, lrCD2);
        part1.add(grCD);
        part1.connect(lrCD1, grCD);
        part1.connect(lrCD2, grCD);
        part1.add(pkCD);
        part1.connect(grCD, pkCD);

        POLoad lA = GenPhyOp.topLoadOp();
        POLoad lB = GenPhyOp.topLoadOp();

        // POLoad lC = lA;
        POFilter fA = GenPhyOp.topFilterOp();

        POLocalRearrange lrA = GenPhyOp.topLocalRearrangeOp();
        POLocalRearrange lrB = GenPhyOp.topLocalRearrangeOp();

        POGlobalRearrange grAB = GenPhyOp.topGlobalRearrangeOp();

        POPackage pkAB = GenPhyOp.topPackageOp();

        POFilter fAB = GenPhyOp.topFilterOp();
        POUnion unABC = GenPhyOp.topUnionOp();

        php.add(lA);
        php.add(lB);

        php.add(fA);

        php.connect(lA, fA);

        php.add(lrA);
        php.add(lrB);

        php.connect(fA, lrA);
        php.connect(lB, lrB);

        php.add(grAB);
        php.connect(lrA, grAB);
        php.connect(lrB, grAB);

        php.add(pkAB);
        php.connect(grAB, pkAB);

        php.add(fAB);
        php.connect(pkAB, fAB);

        php.merge(part1);

        List<PhysicalOperator> leaves = new ArrayList<PhysicalOperator>();
        for (PhysicalOperator phyOp : php.getLeaves()) {
            leaves.add(phyOp);
        }

        php.add(unABC);
        for (PhysicalOperator physicalOperator : leaves) {
            php.connect(physicalOperator, unABC);
        }

        POStore st = GenPhyOp.topStoreOp();

        php.add(st);
        php.connect(unABC, st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC10.gld");
    }

    public void testRun2() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        PhysicalPlan part1 = new PhysicalPlan();
        POLoad lC = GenPhyOp.topLoadOp();
        POFilter fC = GenPhyOp.topFilterOp();
        POLocalRearrange lrC = GenPhyOp.topLocalRearrangeOp();
        POGlobalRearrange grC = GenPhyOp.topGlobalRearrangeOp();
        POPackage pkC = GenPhyOp.topPackageOp();
        part1.add(lC);
        part1.add(fC);
        part1.connect(lC, fC);
        part1.add(lrC);
        part1.connect(fC, lrC);
        part1.add(grC);
        part1.connect(lrC, grC);
        part1.add(pkC);
        part1.connect(grC, pkC);

        POPackage pkD = GenPhyOp.topPackageOp();
        POLocalRearrange lrD = GenPhyOp.topLocalRearrangeOp();
        POGlobalRearrange grD = GenPhyOp.topGlobalRearrangeOp();
        POLoad lD = GenPhyOp.topLoadOp();
        part1.add(lD);
        part1.add(lrD);
        part1.connect(lD, lrD);

        part1.add(grD);
        part1.connect(lrD, grD);
        part1.add(pkD);
        part1.connect(grD, pkD);
        part1.connect(pkD, grC);

        POLoad lA = GenPhyOp.topLoadOp();
        POLoad lB = GenPhyOp.topLoadOp();

        // POLoad lC = lA;
        POFilter fA = GenPhyOp.topFilterOp();

        POLocalRearrange lrA = GenPhyOp.topLocalRearrangeOp();
        POLocalRearrange lrB = GenPhyOp.topLocalRearrangeOp();

        POGlobalRearrange grAB = GenPhyOp.topGlobalRearrangeOp();

        POPackage pkAB = GenPhyOp.topPackageOp();

        POFilter fAB = GenPhyOp.topFilterOp();
        POUnion unABC = GenPhyOp.topUnionOp();

        php.add(lA);
        php.add(lB);

        php.add(fA);

        php.connect(lA, fA);

        php.add(lrA);
        php.add(lrB);

        php.connect(fA, lrA);
        php.connect(lB, lrB);

        php.add(grAB);
        php.connect(lrA, grAB);
        php.connect(lrB, grAB);

        php.add(pkAB);
        php.connect(grAB, pkAB);

        php.add(fAB);
        php.connect(pkAB, fAB);

        php.merge(part1);

        List<PhysicalOperator> leaves = new ArrayList<PhysicalOperator>();
        for (PhysicalOperator phyOp : php.getLeaves()) {
            leaves.add(phyOp);
        }

        php.add(unABC);
        for (PhysicalOperator physicalOperator : leaves) {
            php.connect(physicalOperator, unABC);
        }

        POStore st = GenPhyOp.topStoreOp();

        php.add(st);
        php.connect(unABC, st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC11.gld");
    }

    public void testSpl1() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        POLoad lA = GenPhyOp.topLoadOp();
        POSplit spl = GenPhyOp.topSplitOp();
        php.add(lA);
        php.add(spl);
        php.connect(lA, spl);

        POFilter fl1 = GenPhyOp.topFilterOp();
        POFilter fl2 = GenPhyOp.topFilterOp();
        php.add(fl1);
        php.add(fl2);
        php.connect(spl, fl1);
        php.connect(spl, fl2);

        POLocalRearrange lr1 = GenPhyOp.topLocalRearrangeOp();
        POLocalRearrange lr2 = GenPhyOp.topLocalRearrangeOp();
        php.add(lr1);
        php.add(lr2);
        php.connect(fl1, lr1);
        php.connect(fl2, lr2);

        POGlobalRearrange gr = GenPhyOp.topGlobalRearrangeOp();
        php.add(gr);
        php.connect(lr1, gr);
        php.connect(lr2, gr);

        POPackage pk = GenPhyOp.topPackageOp();
        php.add(pk);
        php.connect(gr, pk);

        POStore st = GenPhyOp.topStoreOp();
        php.add(st);
        php.connect(pk, st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC12.gld");

    }

    public void testSpl2() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        POLoad lA = GenPhyOp.topLoadOp();
        POSplit spl = GenPhyOp.topSplitOp();
        php.add(lA);
        php.add(spl);
        php.connect(lA, spl);

        POFilter fl1 = GenPhyOp.topFilterOp();
        POFilter fl2 = GenPhyOp.topFilterOp();
        php.add(fl1);
        php.add(fl2);
        php.connect(spl, fl1);
        php.connect(spl, fl2);

        POLocalRearrange lr1 = GenPhyOp.topLocalRearrangeOp();
        POLocalRearrange lr2 = GenPhyOp.topLocalRearrangeOp();
        php.add(lr1);
        php.add(lr2);
        php.connect(fl1, lr1);
        php.connect(fl2, lr2);

        POGlobalRearrange gr = GenPhyOp.topGlobalRearrangeOp();
        php.addAsLeaf(gr);

        POPackage pk = GenPhyOp.topPackageOp();
        php.addAsLeaf(pk);

        POSplit sp2 = GenPhyOp.topSplitOp();
        php.addAsLeaf(sp2);

        POFilter fl3 = GenPhyOp.topFilterOp();
        POFilter fl4 = GenPhyOp.topFilterOp();
        php.add(fl3);
        php.add(fl4);
        php.connect(sp2, fl3);
        php.connect(sp2, fl4);

        POUnion un = GenPhyOp.topUnionOp();
        php.addAsLeaf(un);

        POStore st = GenPhyOp.topStoreOp();
        php.addAsLeaf(st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC13.gld");

    }

    public void testSpl3() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        POLoad lA = GenPhyOp.topLoadOp();
        POSplit spl = GenPhyOp.topSplitOp();
        php.add(lA);
        php.add(spl);
        php.connect(lA, spl);

        POFilter fl1 = GenPhyOp.topFilterOp();
        fl1.setRequestedParallelism(10);
        POFilter fl2 = GenPhyOp.topFilterOp();
        fl2.setRequestedParallelism(20);
        php.add(fl1);
        php.add(fl2);
        php.connect(spl, fl1);
        php.connect(spl, fl2);

        POSplit sp11 = GenPhyOp.topSplitOp();
        POSplit sp21 = GenPhyOp.topSplitOp();
        php.add(sp11);
        php.add(sp21);
        php.connect(fl1, sp11);
        php.connect(fl2, sp21);

        POFilter fl11 = GenPhyOp.topFilterOp();
        fl11.setRequestedParallelism(10);
        POFilter fl21 = GenPhyOp.topFilterOp();
        fl21.setRequestedParallelism(20);
        POFilter fl22 = GenPhyOp.topFilterOp();
        fl22.setRequestedParallelism(30);
        php.add(fl11);
        php.add(fl21);
        php.add(fl22);
        php.connect(sp11, fl11);
        php.connect(sp21, fl21);
        php.connect(sp21, fl22);

        POLocalRearrange lr1 = GenPhyOp.topLocalRearrangeOp();
        lr1.setRequestedParallelism(40);
        POLocalRearrange lr21 = GenPhyOp.topLocalRearrangeOp();
        lr21.setRequestedParallelism(15);
        POLocalRearrange lr22 = GenPhyOp.topLocalRearrangeOp();
        lr22.setRequestedParallelism(35);
        php.add(lr1);
        php.add(lr21);
        php.add(lr22);
        php.connect(fl11, lr1);
        php.connect(fl21, lr21);
        php.connect(fl22, lr22);

        POGlobalRearrange gr = GenPhyOp.topGlobalRearrangeOp();
        php.addAsLeaf(gr);

        POPackage pk = GenPhyOp.topPackageOp();
        pk.setRequestedParallelism(25);
        php.addAsLeaf(pk);

        POSplit sp2 = GenPhyOp.topSplitOp();
        php.addAsLeaf(sp2);

        POFilter fl3 = GenPhyOp.topFilterOp();
        fl3.setRequestedParallelism(100);
        POFilter fl4 = GenPhyOp.topFilterOp();
        fl4.setRequestedParallelism(80);
        php.add(fl3);
        php.add(fl4);
        php.connect(sp2, fl3);
        php.connect(sp2, fl4);

        POUnion un = GenPhyOp.topUnionOp();
        php.addAsLeaf(un);

        POStore st = GenPhyOp.topStoreOp();
        php.addAsLeaf(st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC14.gld");

    }

     // Tests Single input case for both blocking and non-blocking
     // with both map and reduce phases
    public void testSim1() throws Exception {
        PhysicalPlan php = new PhysicalPlan();
        POLoad ld = GenPhyOp.topLoadOp();
        php.add(ld);
        PhysicalPlan grpChain1 = GenPhyOp.grpChain();
        php.merge(grpChain1);

        php.connect(ld, grpChain1.getRoots().get(0));

        PhysicalOperator leaf = php.getLeaves().get(0);

        PhysicalPlan grpChain2 = GenPhyOp.grpChain();
        php.merge(grpChain2);

        php.connect(leaf, grpChain2.getRoots().get(0));

        leaf = php.getLeaves().get(0);
        POFilter fl = GenPhyOp.topFilterOp();
        php.add(fl);

        php.connect(leaf, fl);

        POStore st = GenPhyOp.topStoreOp();
        php.add(st);

        php.connect(fl, st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC1.gld");

    }

    public void testSim2() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        PhysicalPlan ldGrpChain1 = GenPhyOp.loadedGrpChain();
        PhysicalPlan ldGrpChain2 = GenPhyOp.loadedGrpChain();

        php.merge(ldGrpChain1);
        php.merge(ldGrpChain2);

        POUnion un = GenPhyOp.topUnionOp();
        php.addAsLeaf(un);

        POStore st = GenPhyOp.topStoreOp();
        php.add(st);

        php.connect(un, st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC2.gld");

    }

    public void testSim3() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        PhysicalPlan ldGrpChain1 = GenPhyOp.loadedGrpChain();
        PhysicalPlan ldGrpChain2 = GenPhyOp.loadedGrpChain();

        php.merge(ldGrpChain1);
        php.merge(ldGrpChain2);

        POUnion un = GenPhyOp.topUnionOp();
        php.addAsLeaf(un);

        PhysicalPlan ldFil1 = GenPhyOp.loadedFilter();

        php.merge(ldFil1);
        php.connect(ldFil1.getLeaves().get(0), un);

        POStore st = GenPhyOp.topStoreOp();
        php.add(st);

        php.connect(un, st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC3.gld");
    }

    public void intTestSim4() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        PhysicalPlan ldGrpChain1 = GenPhyOp.loadedGrpChain();
        PhysicalPlan ldGrpChain2 = GenPhyOp.loadedGrpChain();

        php.merge(ldGrpChain1);
        php.merge(ldGrpChain2);

        POUnion un = GenPhyOp.topUnionOp();
        php.addAsLeaf(un);

        PhysicalPlan ldFil1 = GenPhyOp.loadedFilter();
        PhysicalPlan ldFil2 = GenPhyOp.loadedFilter();

        php.merge(ldFil1);
        php.connect(ldFil1.getLeaves().get(0), un);

        php.merge(ldFil2);
        php.connect(ldFil2.getLeaves().get(0), un);

        POStore st = GenPhyOp.topStoreOp();
        php.add(st);

        php.connect(un, st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC4.gld");
    }

    public void testSim5() throws Exception {
        PhysicalPlan php = new PhysicalPlan();
        PhysicalPlan ldFil1 = GenPhyOp.loadedFilter();
        PhysicalPlan ldFil2 = GenPhyOp.loadedFilter();
        php.merge(ldFil1);
        php.merge(ldFil2);

        POUnion un = GenPhyOp.topUnionOp();
        php.addAsLeaf(un);

        POStore st = GenPhyOp.topStoreOp();
        php.add(st);

        php.connect(un, st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC5.gld");
    }

    public void testSim6() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        PhysicalPlan ldGrpChain1 = GenPhyOp.loadedGrpChain();
        PhysicalPlan ldGrpChain2 = GenPhyOp.loadedGrpChain();

        POLocalRearrange lr1 = GenPhyOp.topLocalRearrangeOp();
        POLocalRearrange lr2 = GenPhyOp.topLocalRearrangeOp();

        ldGrpChain1.addAsLeaf(lr1);
        ldGrpChain2.addAsLeaf(lr2);

        php.merge(ldGrpChain1);
        php.merge(ldGrpChain2);

        POGlobalRearrange gr = GenPhyOp.topGlobalRearrangeOp();
        php.addAsLeaf(gr);

        POPackage pk = GenPhyOp.topPackageOp();
        php.addAsLeaf(pk);

        POStore st = GenPhyOp.topStoreOp();
        php.addAsLeaf(st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC6.gld");

    }

    public void testSim7() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        PhysicalPlan ldGrpChain1 = GenPhyOp.loadedGrpChain();
        PhysicalPlan ldGrpChain2 = GenPhyOp.loadedGrpChain();

        POLocalRearrange lr1 = GenPhyOp.topLocalRearrangeOp();
        POLocalRearrange lr2 = GenPhyOp.topLocalRearrangeOp();

        ldGrpChain1.addAsLeaf(lr1);
        ldGrpChain2.addAsLeaf(lr2);

        php.merge(ldGrpChain1);
        php.merge(ldGrpChain2);

        POGlobalRearrange gr = GenPhyOp.topGlobalRearrangeOp();
        php.addAsLeaf(gr);

        PhysicalPlan ldFil1 = GenPhyOp.loadedFilter();

        php.merge(ldFil1);
        php.connect(ldFil1.getLeaves().get(0), gr);

        POPackage pk = GenPhyOp.topPackageOp();
        php.addAsLeaf(pk);

        POStore st = GenPhyOp.topStoreOp();
        php.addAsLeaf(st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC7.gld");
    }

    public void testSim8() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        PhysicalPlan ldGrpChain1 = GenPhyOp.loadedGrpChain();
        PhysicalPlan ldGrpChain2 = GenPhyOp.loadedGrpChain();

        POLocalRearrange lr1 = GenPhyOp.topLocalRearrangeOp();
        POLocalRearrange lr2 = GenPhyOp.topLocalRearrangeOp();

        ldGrpChain1.addAsLeaf(lr1);
        ldGrpChain2.addAsLeaf(lr2);

        php.merge(ldGrpChain1);
        php.merge(ldGrpChain2);

        POGlobalRearrange gr = GenPhyOp.topGlobalRearrangeOp();
        php.addAsLeaf(gr);

        PhysicalPlan ldFil1 = GenPhyOp.loadedFilter();
        PhysicalPlan ldFil2 = GenPhyOp.loadedFilter();

        php.merge(ldFil1);
        php.connect(ldFil1.getLeaves().get(0), gr);

        php.merge(ldFil2);
        php.connect(ldFil2.getLeaves().get(0), gr);

        POPackage pk = GenPhyOp.topPackageOp();
        php.addAsLeaf(pk);

        POStore st = GenPhyOp.topStoreOp();
        php.addAsLeaf(st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC8.gld");
    }

    public void testSim9() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        POGlobalRearrange gr = GenPhyOp.topGlobalRearrangeOp();
        php.addAsLeaf(gr);

        PhysicalPlan ldFil1 = GenPhyOp.loadedFilter();
        PhysicalPlan ldFil2 = GenPhyOp.loadedFilter();

        php.merge(ldFil1);
        php.connect(ldFil1.getLeaves().get(0), gr);

        php.merge(ldFil2);
        php.connect(ldFil2.getLeaves().get(0), gr);

        POPackage pk = GenPhyOp.topPackageOp();
        php.addAsLeaf(pk);

        POStore st = GenPhyOp.topStoreOp();
        php.addAsLeaf(st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC9.gld");
    }
    
    public void testSortUDF1() throws Exception {
        PhysicalPlan php = new PhysicalPlan();
        PhysicalPlan ldFil1 = GenPhyOp.loadedFilter();
        php.merge(ldFil1);
        
        // set up order by *
        String funcName = WeirdComparator.class.getName();
        POUserComparisonFunc comparator = new POUserComparisonFunc(
                new OperatorKey("", r.nextLong()), -1, null, new FuncSpec(funcName));
        POSort sort = new POSort(new OperatorKey("", r.nextLong()), -1, ldFil1.getLeaves(),
                null, new ArrayList<Boolean>(), comparator);
        sort.setRequestedParallelism(20);
        PhysicalPlan nesSortPlan = new PhysicalPlan();
        POProject topPrj = new POProject(new OperatorKey("", r.nextLong()));
        topPrj.setColumn(1);
        topPrj.setOverloaded(true);
        topPrj.setResultType(DataType.TUPLE);
        nesSortPlan.add(topPrj);
        
        POProject prjStar2 = new POProject(new OperatorKey("", r.nextLong()));
        prjStar2.setResultType(DataType.TUPLE);
        prjStar2.setStar(true);
        nesSortPlan.add(prjStar2);
        
        nesSortPlan.connect(topPrj, prjStar2);
        List<PhysicalPlan> nesSortPlanLst = new ArrayList<PhysicalPlan>();
        nesSortPlanLst.add(nesSortPlan);
        
        sort.setSortPlans(nesSortPlanLst);
        
        php.add(sort);
        php.connect(ldFil1.getLeaves().get(0), sort);
        // have a foreach which takes the sort output
        // and send it two two udfs
        List<String> udfs = new ArrayList<String>();
        udfs.add(COUNT.class.getName());
        udfs.add(SUM.class.getName());
        POForEach fe3 = GenPhyOp.topForEachOPWithUDF(udfs);
        php.add(fe3);
        php.connect(sort, fe3);
        
        // add a group above the foreach
        PhysicalPlan grpChain1 = GenPhyOp.grpChain();
        php.merge(grpChain1);
        php.connect(fe3,grpChain1.getRoots().get(0));
        
        
        udfs.clear();
        udfs.add(AVG.class.getName());
        POForEach fe4 = GenPhyOp.topForEachOPWithUDF(udfs);
        php.addAsLeaf(fe4);
        
        PhysicalPlan grpChain2 = GenPhyOp.grpChain();
        php.merge(grpChain2);
        php.connect(fe4,grpChain2.getRoots().get(0));

        udfs.clear();
        udfs.add(GFCross.class.getName());
        POForEach fe5 = GenPhyOp.topForEachOPWithUDF(udfs);
        php.addAsLeaf(fe5);
        
        POStore st = GenPhyOp.topStoreOp();
        php.addAsLeaf(st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC15.gld");
    }
    
    public void testDistinct1() throws Exception {
        PhysicalPlan php = new PhysicalPlan();
        PhysicalPlan ldFil1 = GenPhyOp.loadedFilter();
        php.merge(ldFil1);
        
        PODistinct op = new PODistinct(new OperatorKey("", r.nextLong()),
                -1, null);
        
        php.addAsLeaf(op);
        
        PhysicalPlan grpChain1 = GenPhyOp.grpChain();
        php.merge(grpChain1);
        php.connect(op,grpChain1.getRoots().get(0));
        
        PODistinct op1 = new PODistinct(new OperatorKey("", r.nextLong()),
                -1, null);
        
        php.addAsLeaf(op1);
        POStore st = GenPhyOp.topStoreOp();
        php.addAsLeaf(st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC16.gld");
    }
    
    public void testLimit() throws Exception {
        PhysicalPlan php = new PhysicalPlan();

        POLoad lC = GenPhyOp.topLoadOp();
        php.add(lC);

        POLimit op = new POLimit(new OperatorKey("", r.nextLong()),
                -1, null);

        php.add(op);
        php.connect(lC, op);

        POStore st = GenPhyOp.topStoreOp();
        php.addAsLeaf(st);
        run(php, "test/org/apache/pig/test/data/GoldenFiles/MRC17.gld");
    }
    
    @Test
    public void testMRCompilerErr() throws Exception {
    	planTester.buildPlan("a = load 'input';");
    	LogicalPlan lp = planTester.buildPlan("b = filter a by $0 > 5;");
    	
    	PhysicalPlan pp = Util.buildPhysicalPlan(lp, pc);
    	pp.remove(pp.getRoots().get(0));
    	try {
    		Util.buildMRPlan(new PhysicalPlan(), pc);
    		fail("Expected failure.");
    	} catch (MRCompilerException mrce) {
    		assertTrue(mrce.getErrorCode() == 2053);
    	}
    }

    @Test
    public void testMRCompilerErr1() throws Exception {   	
        PhysicalPlan pp = new PhysicalPlan();
        PhysicalPlan ldFil1 = GenPhyOp.loadedFilter();
        pp.merge(ldFil1);
        
        POSplit op = GenPhyOp.topSplitOp();
        pp.addAsLeaf(op);

    	try {
    		Util.buildMRPlan(pp, pc);
    		fail("Expected failure.");
    	} catch (MRCompilerException mrce) {
    		assertTrue(mrce.getErrorCode() == 2025);
    	}
    }

    /**
     * Test to ensure that the order by without parallel followed by a limit, i.e., top k
     * always produces the correct number of map reduce jobs. In the testcase below since
     * we are running the unit test locally, we will get reduce parallelism as 1. So we will
     * NOT introduce the extra MR job to do a final limit
     */
    @Test
    public void testNumReducersInLimit() throws Exception {
    	planTester.buildPlan("a = load 'input';");
    	planTester.buildPlan("b = order a by $0;");
    	planTester.buildPlan("c = limit b 10;");
    	LogicalPlan lp = planTester.buildPlan("store c into '/tmp';");
    	
    	PhysicalPlan pp = Util.buildPhysicalPlan(lp, pc);
    	MROperPlan mrPlan = Util.buildMRPlan(pp, pc);
    	MapReduceOper mrOper = mrPlan.getRoots().get(0);
    	int count = 1;
    	
    	while(mrPlan.getSuccessors(mrOper) != null) {
    		mrOper = mrPlan.getSuccessors(mrOper).get(0);
    		++count;
    	}        
    	assertTrue(count == 3);
    }
    
    /**
     * Test to ensure that the order by with parallel followed by a limit, i.e., top k
     * always produces the correct number of map reduce jobs
     */
    @Test
    public void testNumReducersInLimitWithParallel() throws Exception {
    	planTester.buildPlan("a = load 'input';");
    	planTester.buildPlan("b = order a by $0 parallel 2;");
    	planTester.buildPlan("c = limit b 10;");
    	LogicalPlan lp = planTester.buildPlan("store c into '/tmp';");
    	
    	PhysicalPlan pp = Util.buildPhysicalPlan(lp, pc);
    	MROperPlan mrPlan = Util.buildMRPlan(pp, pc);
    	MapReduceOper mrOper = mrPlan.getRoots().get(0);
    	int count = 1;
    	
    	while(mrPlan.getSuccessors(mrOper) != null) {
    		mrOper = mrPlan.getSuccessors(mrOper).get(0);
    		++count;
    	}        
    	assertTrue(count == 4);
    }


    public static class WeirdComparator extends ComparisonFunc {

        @Override
        public int compare(Tuple t1, Tuple t2) {
            // TODO Auto-generated method stub
            int result = 0;
            try {
                int i1 = (Integer) t1.get(1);
                int i2 = (Integer) t2.get(1);
                result = (i1 - 50) * (i1 - 50) - (i2 - 50) * (i2 - 50);
            } catch (ExecException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return result;
        }

    }

    private void run(PhysicalPlan pp, String expectedFile) throws Exception {
        String compiledPlan, goldenPlan = null;
        int MAX_SIZE = 100000;
        MRCompiler comp = new MRCompiler(pp, pc);
        comp.compile();

        MROperPlan mrp = comp.getMRPlan();
        PlanPrinter ppp = new PlanPrinter(mrp);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ppp.print(baos);
        compiledPlan = baos.toString();

        if(generate ){
            FileOutputStream fos = new FileOutputStream(expectedFile);
            fos.write(baos.toByteArray());
            return;
        }
        FileInputStream fis = new FileInputStream(expectedFile);
        byte[] b = new byte[MAX_SIZE];
        int len = fis.read(b);
        goldenPlan = new String(b, 0, len);
        if (goldenPlan.charAt(len-1) == '\n')
            goldenPlan = goldenPlan.substring(0, len-1);

        pp.explain(System.out);
        System.out.println();
        System.out.println("<<<" + compiledPlan + ">>>");
        System.out.println("-------------");
        System.out.println("Golden");
        System.out.println("<<<" + goldenPlan + ">>>");
        System.out.println("-------------");
        assertEquals(goldenPlan, compiledPlan);
    }


    

}
