/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.ecv.health;


public class FailedHealthCheckException extends RuntimeException {

    public IHealth health;

    public FailedHealthCheckException() {
    }

    public FailedHealthCheckException(String message) {
        super(message);
    }


    public FailedHealthCheckException(IHealth health) {
        super();
        this.health = health;

    }


    public FailedHealthCheckException(Throwable cause) {
        super(cause);
    }

    public FailedHealthCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedHealthCheckException(IHealth health, Throwable cause) {
        super(cause);
        this.health = health;
    }

    public IHealth getHealth() {
        return health;
    }

    public void setHealth(IHealth health) {
        this.health = health;
    }


}
