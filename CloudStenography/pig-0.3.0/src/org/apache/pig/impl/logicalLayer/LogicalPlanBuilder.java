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
package org.apache.pig.impl.logicalLayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.pig.impl.PigContext;
import org.apache.pig.impl.logicalLayer.parser.ParseException;
import org.apache.pig.impl.logicalLayer.parser.QueryParser;
import org.apache.pig.impl.plan.OperatorKey;


/**
 * PlanBuilder class outputs a logical plan given a query String and set of ValidIDs
 * 
 */
public class LogicalPlanBuilder {

    public static ClassLoader classloader = LogicalPlanBuilder.class.getClassLoader();
    private PigContext pigContext;
    public LogicalPlanBuilder(PigContext pigContext) {
        this.pigContext = pigContext;
    }

    public LogicalPlan parse(String scope, 
                             String query, 
                             Map<LogicalOperator, LogicalPlan> aliases,
                             Map<OperatorKey, LogicalOperator> opTable,
                             Map<String, LogicalOperator> aliasOp,
                             Map<String, String> fileNameMap)
        throws IOException, ParseException {
        return parse(scope, query, aliases, opTable, aliasOp, 1, fileNameMap);
   }
    
    public LogicalPlan parse(String scope, 
                             String query, 
                             Map<LogicalOperator, LogicalPlan> aliases,
                             Map<OperatorKey, LogicalOperator> opTable,
                             Map<String, LogicalOperator> aliasOp, 
                             int start,
                             Map<String, String> fileNameMap)
        throws IOException, ParseException {
        ByteArrayInputStream in = new ByteArrayInputStream(query.getBytes());        
        QueryParser parser = new QueryParser(in, pigContext, scope, aliases, opTable, 
                                             aliasOp, start, fileNameMap);
        return parser.Parse();        
    }

}
