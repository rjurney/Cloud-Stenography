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
package org.apache.pig.impl.streaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.pig.LoadFunc;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.impl.PigContext;
import org.apache.pig.impl.io.BufferedPositionedInputStream;
import org.apache.pig.impl.streaming.StreamingCommand.HandleSpec;

/**
 * FileOutputHandler handles the output from the Pig-Streaming
 * executable in an asynchronous manner by reading it from
 * an external file specified by the user.  
 */
public class FileOutputHandler extends OutputHandler {

    String fileName;
    BufferedPositionedInputStream fileInStream;
    
    public FileOutputHandler(HandleSpec handleSpec) throws ExecException {
        fileName = handleSpec.name;
        deserializer = 
            (LoadFunc) PigContext.instantiateFuncFromSpec(handleSpec.spec);
    }

    public OutputType getOutputType() {
        return OutputType.ASYNCHRONOUS;
    }
    
    public void bindTo(String fileName, BufferedPositionedInputStream is,
            long offset, long end) throws IOException {
        // This is a trigger to start processing the output from the file ...
        // ... however, we must ignore the input parameters and use ones
        // provided during initialization
        File file = new File(this.fileName);
        this.fileInStream = 
            new BufferedPositionedInputStream(new FileInputStream(file)); 
        super.bindTo(this.fileName, this.fileInStream, 0, file.length());
    }
    
    public synchronized void close() throws IOException {
        if(!alreadyClosed) {
            super.close();
            fileInStream.close();
            fileInStream = null;
            alreadyClosed = true;
        }
    }

}
