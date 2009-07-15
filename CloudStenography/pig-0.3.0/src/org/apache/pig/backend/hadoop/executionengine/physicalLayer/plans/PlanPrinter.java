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
package org.apache.pig.backend.hadoop.executionengine.physicalLayer.plans;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pig.PigException;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.relationalOperators.*;
import org.apache.pig.impl.plan.DepthFirstWalker;
import org.apache.pig.impl.plan.Operator;
import org.apache.pig.impl.plan.OperatorPlan;
import org.apache.pig.impl.plan.PlanVisitor;
import org.apache.pig.impl.plan.VisitorException;

public class PlanPrinter<O extends Operator, P extends OperatorPlan<O>> extends
        PlanVisitor<O, P> {

    String TAB1 = "    ";

    String TABMore = "|   ";

    String LSep = "|\n|---";
    
    String USep = "|   |\n|   ";

    int levelCntr = -1;

    OutputStream printer;
    
    PrintStream stream = System.out;

    boolean isVerbose = true;

    public PlanPrinter(P plan) {
        super(plan, new DepthFirstWalker<O, P>(plan));
    }
    
    public PlanPrinter(P plan, PrintStream stream) {
        super(plan, new DepthFirstWalker<O, P>(plan));
        this.stream = stream;
    }

    public void setVerbose(boolean verbose) {
        isVerbose = verbose;
    }

    @Override
    public void visit() throws VisitorException {
        try {
            stream.write(depthFirstPP().getBytes());
        } catch (IOException ioe) {
            int errCode = 2079;
            String msg = "Unexpected error while printing physical plan.";
            throw new VisitorException(msg, errCode, PigException.BUG, ioe);
        }
    }

    public void print(OutputStream printer) throws VisitorException, IOException {
        this.printer = printer;
        printer.write(depthFirstPP().getBytes());
    }

    protected void breadthFirst() throws VisitorException {
        List<O> leaves = mPlan.getLeaves();
        Set<O> seen = new HashSet<O>();
        breadthFirst(leaves, seen);
    }

    private void breadthFirst(Collection<O> predecessors, Set<O> seen)
            throws VisitorException {
        ++levelCntr;
        dispTabs();

        List<O> newPredecessors = new ArrayList<O>();
        for (O pred : predecessors) {
            if (seen.add(pred)) {
                List<O> predLst = mPlan.getPredecessors(pred);
                if (predLst != null)
                    newPredecessors.addAll(predLst);

                pred.visit(this);
            }
        }
        if (newPredecessors.size() > 0) {
            stream.println();
            breadthFirst(newPredecessors, seen);
        }
    }

    protected String depthFirstPP() throws VisitorException {
        StringBuilder sb = new StringBuilder();
        List<O> leaves = mPlan.getLeaves();
        Collections.sort(leaves);
        for (O leaf : leaves) {
            sb.append(depthFirst(leaf));
            sb.append("\n");
        }
        sb.delete(sb.length() - "\n".length(), sb.length());
        sb.delete(sb.length() - "\n".length(), sb.length());
        return sb.toString();
    }
    
    private String planString(PhysicalPlan pp){
        StringBuilder sb = new StringBuilder();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(pp!=null)
            pp.explain(baos, isVerbose);
        else
            return "";
        sb.append(USep);
        sb.append(shiftStringByTabs(baos.toString(), 2));
        return sb.toString();
    }
    
    private String planString(List<PhysicalPlan> lep){
        StringBuilder sb = new StringBuilder();
        if(lep!=null)
            for (PhysicalPlan ep : lep) {
                sb.append(planString(ep));
            }
        return sb.toString();
    }

    private String depthFirst(O node) throws VisitorException {
        StringBuilder sb = new StringBuilder(node.name() + "\n");
        if (isVerbose) {
          if(node instanceof POFilter){
            sb.append(planString(((POFilter)node).getPlan()));
          }
          else if(node instanceof POLocalRearrange){
            sb.append(planString(((POLocalRearrange)node).getPlans()));
          }
          else if(node instanceof POSort){
            sb.append(planString(((POSort)node).getSortPlans())); 
          }
          else if(node instanceof POForEach){
            sb.append(planString(((POForEach)node).getInputPlans()));
          }
          else if (node instanceof POMultiQueryPackage) {
              List<POPackage> pkgs = ((POMultiQueryPackage)node).getPackages();
              for (POPackage pkg : pkgs) {
                  sb.append(LSep + pkg.name() + "\n");
              }
          }
          else if(node instanceof POFRJoin){
            POFRJoin frj = (POFRJoin)node;
            List<List<PhysicalPlan>> joinPlans = frj.getJoinPlans();
            if(joinPlans!=null)
              for (List<PhysicalPlan> list : joinPlans) {
                sb.append(planString(list));
              }
          }
        }
        
        if (node instanceof POSplit) {
            sb.append(planString(((POSplit)node).getPlans()));
        }
        else if (node instanceof PODemux) {
            List<PhysicalPlan> plans = new ArrayList<PhysicalPlan>();
            Set<PhysicalPlan> pl = new HashSet<PhysicalPlan>();
            pl.addAll(((PODemux)node).getPlans());
            plans.addAll(pl);
            sb.append(planString(plans));
        }
        
        List<O> originalPredecessors = mPlan.getPredecessors(node);
        if (originalPredecessors == null)
            return sb.toString();
        
        List<O> predecessors =  new ArrayList<O>(originalPredecessors);
        
        Collections.sort(predecessors);
        int i = 0;
        for (O pred : predecessors) {
            i++;
            String DFStr = depthFirst(pred);
            if (DFStr != null) {
                sb.append(LSep);
                if (i < predecessors.size())
                    sb.append(shiftStringByTabs(DFStr, 2));
                else
                    sb.append(shiftStringByTabs(DFStr, 1));
            }
        }
        return sb.toString();
    }

    private String shiftStringByTabs(String DFStr, int TabType) {
        StringBuilder sb = new StringBuilder();
        String[] spl = DFStr.split("\n");

        String tab = (TabType == 1) ? TAB1 : TABMore;

        sb.append(spl[0] + "\n");
        for (int i = 1; i < spl.length; i++) {
            sb.append(tab);
            sb.append(spl[i]);
            sb.append("\n");
        }
        return sb.toString();
    }

    private void dispTabs() {
        for (int i = 0; i < levelCntr; i++)
            stream.print(TAB1);
    }

    public void visitLoad(POLoad op) {
        stream.print(op.name() + "   ");
    }

    public void visitStore(POStore op) {
        stream.print(op.name() + "   ");
    }

    public void visitFilter(POFilter op) {
        stream.print(op.name() + "   ");
    }

    public void visitLocalRearrange(POLocalRearrange op) {
        stream.print(op.name() + "   ");
    }

    public void visitGlobalRearrange(POGlobalRearrange op) {
        stream.print(op.name() + "   ");
    }

    public void visitPackage(POPackage op) {
        stream.print(op.name() + "   ");
    }

    public void visitStartMap(POUnion op) {
        stream.print(op.name() + "   ");
    }
    
}
