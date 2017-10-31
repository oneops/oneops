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
package com.oneops.inductor;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;

public class ProcessRunnerTest {

	/**
	 * test for mem leak

	public static void main(String[] args) {
		for (int i=1; i<=10000; i++) {
			ProcessRunner pr = new ProcessRunner();
			String[] cmd = new String[2];
			cmd[0] = "echo";
			cmd[1] = "\"hey there \"";
			ProcessResult procResult = pr.executeProcessRetry(cmd, "blah", 3);
			System.out.println(procResult.getStdOut());
			// Get current size of heap in bytes
			long heapSize = Runtime.getRuntime().totalMemory(); 
			// Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
			long heapMaxSize = Runtime.getRuntime().maxMemory();
			 // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
			long heapFreeSize = Runtime.getRuntime().freeMemory();
			System.out.println("run#" + i + "; heapSize=" + heapSize + "; heapMaxSize=" + heapMaxSize + "; heapFreeSize="+heapFreeSize+";");
		}
	}
	 */

	@Test
	public void testExecuteProcessRetry() {
		ProcessRunner pr = new ProcessRunner(null);
		String[] cmd = new String[2];
		cmd[0] = "echo";
		cmd[1] = "\"hey there \"";
		ProcessResult procResult = pr.executeProcessRetry(cmd, "", 3);
		assertTrue(procResult.getResultCode() == 0);
	}

	// @Test
	public void executeProcessWithEnv() throws IOException, URISyntaxException {
		Config c = new Config();
		c.setEnv("PATH1=/usr/local/ruby/bin,GEM_PATH1=/usr/local/gems");
		c.init();
		ProcessRunner pr = new ProcessRunner(c);
		String[] cmd = new String[2];
		cmd[0] = "chef-solo";
		cmd[1] = "-v";
		ProcessResult procResult = pr.executeProcessRetry(cmd, "", 3);
		assertTrue(procResult.getResultCode() == 0);
	}
}
