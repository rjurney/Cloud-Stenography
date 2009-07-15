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

package org.apache.pig.backend.executionengine;

import org.apache.pig.backend.BackendException;

public class ExecException extends BackendException {
    static final long serialVersionUID = 1;
    
    /**
     * Create a new ExecException with null as the error message.
     */
    public ExecException() {
        super();
    }
    
    /**
     * Create a new ExecException with the specified message and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     */
    public ExecException(String message) {
        super(message);
    }
    
    /**
     * Create a new ExecException with the specified cause.
     *
     * @param cause - The cause (which is saved for later retrieval by the <link>Throwable.getCause()</link> method) indicating the source of this exception. A null value is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public ExecException(Throwable cause) {
        super(cause);
    }

    /**
     * Create a new ExecException with the specified message and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     * @param cause - The cause (which is saved for later retrieval by the <link>Throwable.getCause()</link> method) indicating the source of this exception. A null value is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public ExecException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new ExecException with the specified message and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     * @param errCode - The error code shown to the user 
     */
    public ExecException(String message, int errCode) {
        super(message, errCode);
    }

    /**
     * Create a new ExecException with the specified message and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     * @param errCode - The error code shown to the user 
     * @param cause - The cause (which is saved for later retrieval by the <link>Throwable.getCause()</link> method) indicating the source of this exception. A null value is permitted, and indicates that the cause is nonexistent or unknown. 
     */
    public ExecException(String message, int errCode, Throwable cause) {
        super(message, errCode, cause);
    }

    /**
     * Create a new ExecException with the specified message and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     * @param errCode - The error code shown to the user 
     * @param errSrc - The error source 
     */
    public ExecException(String message, int errCode, byte errSrc) {
        super(message, errCode, errSrc);
    }   

    /**
     * Create a new ExecException with the specified message and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     * @param errCode - The error code shown to the user 
     * @param errSrc - The error source
     * @param cause - The cause (which is saved for later retrieval by the <link>Throwable.getCause()</link> method) indicating the source of this exception. A null value is permitted, and indicates that the cause is nonexistent or unknown. 
     */
    public ExecException(String message, int errCode, byte errSrc,
            Throwable cause) {
        super(message, errCode, errSrc, cause);
    }

    /**
     * Create a new ExecException with the specified message and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     * @param errCode - The error code shown to the user 
     * @param retry - If the exception is retriable or not
     */ 
    public ExecException(String message, int errCode, boolean retry) {
        super(message, errCode, retry);
    }

    /**
     * Create a new ExecException with the specified message and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     * @param errCode - The error code shown to the user 
     * @param errSrc - The error source 
     * @param retry - If the exception is retriable or not
     */
    public ExecException(String message, int errCode, byte errSrc,
            boolean retry) {
        super(message, errCode, errSrc, retry);
    }    

    /**
     * Create a new ExecException with the specified message, error code, error source, retriable or not, detalied message for the developer and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     * @param errCode - The error code shown to the user 
     * @param errSrc - The error source 
     * @param retry - If the exception is retriable or not
     * @param detailedMsg - The detailed message shown to the developer 
     */
    public ExecException(String message, int errCode, byte errSrc,
            boolean retry, String detailedMsg) {
        super(message, errCode, errSrc, retry, detailedMsg);
    }
    
    /**
     * Create a new ExecException with the specified message, error code, error source, retriable or not, detalied message for the developer and cause.
     *
     * @param message - The error message (which is saved for later retrieval by the <link>Throwable.getMessage()</link> method) shown to the user 
     * @param errCode - The error code shown to the user 
     * @param errSrc - The error source 
     * @param retry - If the exception is retriable or not
     * @param detailedMsg - The detailed message shown to the developer 
     * @param cause - The cause (which is saved for later retrieval by the <link>Throwable.getCause()</link> method) indicating the source of this exception. A null value is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public ExecException(String message, int errCode, byte errSrc,
            boolean retry, String detailedMsg, Throwable cause) {
        super(message, errCode, errSrc, retry, detailedMsg, cause);
    }
}
