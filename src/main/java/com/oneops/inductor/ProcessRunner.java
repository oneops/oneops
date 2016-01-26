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

import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

public class ProcessRunner {

	private static Logger logger = Logger.getLogger(ProcessRunner.class);

	private int timeoutInSeconds = 7200; // 2hr

	private Config config;

	ProcessRunner(Config config) {
		this.config = config;
	}

	public int getTimeoutInSeconds() {
		return timeoutInSeconds;
	}

	public void setTimeoutInSeconds(int timeoutInSeconds) {
		this.timeoutInSeconds = timeoutInSeconds;
	}

	/**
	 * Process retry over loaded method with shutdown cloud processing.
	 *
	 * @param wo          work order
	 * @param cmd         command to execute
	 * @param logKey      log key
	 * @param max_retries max retry count
	 * @return process result
	 */
	public ProcessResult executeProcessRetry(CmsWorkOrderSimpleBase wo, String[] cmd, String logKey, int max_retries, Config config) {

		boolean shutdown = config.hasCloudShutdownFor(logKey, wo);
		if (shutdown) {
			// If the cloud is already shutdown, set max retry count to 0.
			// This is to avoid unnecessary command retries for already
			// decommissioned/deleted cloud resources.
			max_retries = 0;
			// Reduce the rsync timeout (default is 10).
			long timeout = config.getCmdTimeout();
			for (int i = 0; i < cmd.length; i++) {
				if (cmd[i].startsWith("--timeout=")) {
					cmd[i] = "--timeout=" + timeout;
				}
			}
		}
		ProcessResult result = executeProcessRetry(cmd, logKey, max_retries, InductorConstants.PRIVATE_IP);
		// Mark the process execution result code to 0 for the shutdown.clouds.
		if (shutdown) {
			logger.warn(logKey + " ### Set the result code to 0 as the cloud resource for this component is already released.");
			result.setResultCode(0);
		}
		return result;
	}

	/**
	 * Wrapper for process retry
	 *
	 * @param cmd         command to execute
	 * @param logKey      log key
	 * @param max_retries max retry count
	 * @return process result
	 * @see {@link #executeProcessRetry(String[], String, int, String)}
	 */
	public ProcessResult executeProcessRetry(String[] cmd, String logKey, int max_retries) {
		return executeProcessRetry(cmd, logKey, max_retries, InductorConstants.PRIVATE_IP);
	}

	/**
	 * Execute the command and retry if it fails.
	 *
	 * @param cmd          command to execute
	 * @param logKey       log key
	 * @param max_retries  max retry count
	 * @param ip_attribute ip address
	 * @return process result
	 */
	public ProcessResult executeProcessRetry(String[] cmd, String logKey, int max_retries, String ip_attribute) {

		int count = 0;
		ProcessResult result = new ProcessResult();

		while ((result.getResultCode() != 0 && count < max_retries + 1)
				|| result.isRebooting()) {
			if (count > 0) {
				logger.warn(logKey + "retry #: " + count);
				if (result.getResultMap().containsKey(
						InductorConstants.SHARED_IP)
						&& ip_attribute
								.equalsIgnoreCase(InductorConstants.PUBLIC_IP)) {

					String ip = result.getResultMap().get(
							InductorConstants.SHARED_IP);
					String oldUserHost = cmd[1];
					logger.info("retrying using shared_ip: " + ip);
					String newUserHost = oldUserHost.replaceFirst("@.*", "@"
							+ ip);
					cmd[1] = newUserHost;
				}
			}
			result.setRebooting(false);

			long startTime = System.currentTimeMillis();

			executeProcess(cmd, logKey, result);
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			logger.info(logKey + "cmd took: " + duration + " ms");

			if (result.isRebooting()) {
				result.setResultCode(0);
				long sleepSec = 60L;
				logger.info(logKey + " rebooting ... sleeping " + sleepSec
						+ " sec...");
				try {
					Thread.sleep(sleepSec * 1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				count--;
			}

			count++;
			try {
				if (result.getResultCode() != 0 && count - 1 < max_retries) {
					// retry quick (1 sec) then decay
					long sleepSec = count * 7 - 6;
					logger.info("sleeping " + sleepSec + " sec...");
					Thread.sleep(sleepSec * 1000L);
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		logger.info(logKey + " ### EXEC EXIT CODE: " + result.getResultCode());
		return result;
	}

	/**
	 * Creates a process and logs the output
	 *
	 * @param cmd
	 * @param logKey
	 * @param result
	 */
	private void executeProcess(String[] cmd, String logKey, ProcessResult result) {

		Map<String, String> env = getEnvVars(logKey, cmd);
		logger.info(logKey + " Cmd: " + String.join(" ", cmd) + ", Env: " + env);

		// run the cmd
		try {

			CommandLine cmdLine = new CommandLine(cmd[0]);
			// add rest of cmd string[] as arguments
			for (int i = 1; i < cmd.length; i++) {
				// needs the quote handling=false or else doesn't work
				// http://www.techques.com/question/1-5080109/How-to-execute--bin-sh-with-commons-exec?
				cmdLine.addArgument(cmd[i], false);
			}
			DefaultExecutor executor = new DefaultExecutor();
			executor.setExitValue(0);
			executor.setWatchdog(new ExecuteWatchdog(timeoutInSeconds * 1000));
			executor.setStreamHandler(new OutputHandler(logger, logKey, result));
			result.setResultCode(executor.execute(cmdLine, env));

			// set fault to last error if fault map is empty
			if (result.getResultCode() != 0 && result.getFaultMap().keySet().size() < 1) {
				result.getFaultMap().put("ERROR", result.getLastError());
			}

		} catch (ExecuteException ee) {
			logger.error(logKey + ee);
			result.setResultCode(ee.getExitValue());
		} catch (IOException e) {
			logger.error(e);
			result.setResultCode(1);
		}

	}

	/**
	 * Returns env variables to be used for the cmd. Right now env var
	 * is explicitly set only for local workorders (chef-solo commands).
	 *
	 * @param cmd    command array
	 * @param logKey log key
	 * @return env vars map or <code>null</code> if there is no env
	 * vars configured or for remote wo command.
	 */
	private Map<String, String> getEnvVars(String logKey, String[] cmd) {
		Map<String, String> envVars = null;
		if ("chef-solo".equalsIgnoreCase(cmd[0])) {
			if (!config.getEnvVars().isEmpty()) {
				try {
					// Env = Process Env +  Inductor config env.
					envVars = EnvironmentUtils.getProcEnvironment();
					envVars.putAll(config.getEnvVars());
				} catch (IOException e) {
					logger.warn(logKey + " Can't get the process env: " + e.getMessage());
				}
			}
		}
		return envVars;
	}
}
