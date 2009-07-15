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

/** 
 * This is helper class for parameter substitution
 */

package org.apache.pig.tools.parameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreprocessorContext {

    private Hashtable<String , String> param_val ;
    
    private final Log log = LogFactory.getLog(getClass());

    /**
     * @param limit - max number of parameters. Passing
     *                smaller number only impacts performance
     */         
    public PreprocessorContext(int limit){
        param_val = new Hashtable<String, String> (limit);
    }
    
    /* 
    public  void processLiteral(String key, String val) {
        processLiteral(key, val, true);
    } */

    /**
     * This method generates parameter value by running specified command
     *
     * @param key - parameter name
     * @param val - string containing command to be executed
     */
    public  void processShellCmd(String key, String val) {
        processShellCmd(key, val, true);
    }

    /**
     * This method generates value for the specified key by
     * performing substitution if needed within the value first.
     *
     * @param key - parameter name
     * @param val - value supplied for the key
     */
    public  void processOrdLine(String key, String val) {
        processOrdLine(key, val, true);
    }

    /*
    public  void processLiteral(String key, String val, Boolean overwrite) {

        if (param_val.containsKey(key)) {
            if (overwrite) {
                log.warn("Warning : Multiple values found for " + key + ". Using value " + val);
            } else {
                return;
            }
        }

        String sub_val = substitute(val);
        param_val.put(key, sub_val);
    } */

    /**
     * This method generates parameter value by running specified command
     *
     * @param key - parameter name
     * @param val - string containing command to be executed
     */
    public  void processShellCmd(String key, String val, Boolean overwrite) {

        if (param_val.containsKey(key)) {
            if (overwrite) {
                log.warn("Warning : Multiple values found for " + key + ". Using value " + val);
            } else {
                return;
            }
        }

        val=val.substring(1, val.length()-1); //to remove the backticks
        String sub_val = substitute(val);
        sub_val = executeShellCommand(sub_val);
        param_val.put(key, sub_val);
    } 

    /**
     * This method generates value for the specified key by
     * performing substitution if needed within the value first.
     *
     * @param key - parameter name
     * @param val - value supplied for the key
     * @param overwrite - specifies whether the value should be replaced if it already exists
     */
    public  void processOrdLine(String key, String val, Boolean overwrite) {

        if (param_val.containsKey(key)) {
            if (overwrite) {
                log.warn("Warning : Multiple values found for " + key + ". Using value " + val);
            } else {
                return;
            }
        }

        String sub_val = substitute(val);
        param_val.put(key, sub_val);
    } 


    /*
     * executes the 'cmd' in shell and returns result
     */
    private String executeShellCommand (String cmd) 
    {
        Process p;
        String streamData="";
        String streamError="";
        try {
            log.info("Executing command : " + cmd);
            // we can't use exec directly since it does not handle
            // case like foo -c "bar bar" correctly. It splits on white spaces even in presents of quotes
            String[] cmdArgs = new String[3];
            cmdArgs[0] = "bash";
            cmdArgs[1] = "-c";
            StringBuffer sb  = new StringBuffer("exec ");
            sb.append(cmd);
            cmdArgs[2] = sb.toString();

            p = Runtime.getRuntime().exec(cmdArgs);

        } catch (IOException e) {
            RuntimeException rte = new RuntimeException("IO Exception while executing shell command : "+e.getMessage() , e);
            throw rte;
        }

        int exitVal;
        try {
            exitVal = p.waitFor();
        } catch (InterruptedException e) {
            RuntimeException rte = new RuntimeException("Interrupted Thread Exception while waiting for command to get over"+e.getMessage() , e);
            throw rte;
        }

        if (exitVal != 0) {
            RuntimeException rte = new RuntimeException("Error executing shell command: " + cmd + ". Command exit with exit code of " + exitVal );
            throw rte;
        }

        try{
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null){
                streamData+=(line+"\n");
            }
        } catch (IOException e){
            RuntimeException rte = new RuntimeException("IO Exception while executing shell command : "+e.getMessage() , e);
            throw rte;
        }

        try {
            InputStreamReader isr = new InputStreamReader(p.getErrorStream());
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null ) {
                streamError += (line+"\n");
            }
            log.debug("Error stream while executing shell command : " + streamError);
        } catch (Exception e) {
            RuntimeException rte = new RuntimeException("IO Exception while executing shell command : "+e.getMessage() , e);
            throw rte;
        }

        return streamData.trim();
    }

    private static String id_regex = "\\$[_]*[a-zA-Z][a-zA-Z_0-9]*";
    public  String substitute(String line) {

        int index = line.indexOf('$');
        if (index == -1)	return line;

        String replaced_line = new String(line);

        Pattern identifier = Pattern.compile( id_regex );
        Matcher keyMatcher = identifier.matcher( line );
        String key="";
        String val="";

        while (keyMatcher.find()) {
            // make sure that we don't perform parameter substitution
            // for escaped vars of the form \$<id>
            if ( (keyMatcher.start() == 0) || (line.charAt( keyMatcher.start() - 1)) != '\\' ) {
                key = keyMatcher.group().substring(1);  	//skip the '$'
                if (!(param_val.containsKey(key))) {
                    throw new RuntimeException("Undefined parameter : "+key);
                }
                val = param_val.get(key);
                //String litVal = Matcher.quoteReplacement(val);
                replaced_line = replaced_line.replaceFirst("\\$"+key, val); 
            }
        }

        // unescape $<id>
        replaced_line = replaced_line.replaceAll("\\\\\\$","\\$");
        return replaced_line;
    }

}


