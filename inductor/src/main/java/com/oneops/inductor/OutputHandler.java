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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class OutputHandler implements ExecuteStreamHandler {
	private String logKey;
	private Logger logger;
	private static String REBOOT_FLAG = "***REBOOT_FLAG***";
	private static String RESULT_KEY = "***RESULT:";
	private static String FAULT_KEY = "***FAULT:";
	private static String TAG_KEY = "***TAG:";
	private static String RESULTJSON_KEY = "***RESULTJSON:";

	private static String ADDITIONAL_INFO_KEY = "***ADDITIONAL_INFO:";

	private ProcessResult result;

	final private Gson gson = new Gson();

	// limit amt of output a cmd can log
	private int rowCount = 0;
	private static int maxRowCount = 2000;

	public OutputHandler(Logger logger, String logKey, ProcessResult result) {
		this.logger = logger;
		this.logKey = logKey;
		this.result = result;
	}

	@Override
	public void setProcessOutputStream(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {

			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {

				if (rowCount < maxRowCount) {

					// no print private keys
					if (!line.contains("PRIVATE KEY")) {
						logger.info(logKey + "cmd out: " + line);
						result.appendStdOut(line + "\n");
					}

					if (line.indexOf(REBOOT_FLAG) > -1)
						result.setRebooting(true);

					int keyIndex = line.indexOf(RESULT_KEY);
					if (keyIndex == 0) {
						String withOutResultKey = line.substring(keyIndex + 10,
								line.length());
						String k = withOutResultKey.substring(0,
								withOutResultKey.indexOf("="));
						String v = withOutResultKey.substring(
								withOutResultKey.indexOf("=") + 1,
								withOutResultKey.length());
						result.getResultMap().put(k, v);
						logger.debug(logKey + " resultCi " + k + ": " + v);
					}

					keyIndex = line.indexOf(FAULT_KEY);
					if (keyIndex == 0) {
						String withOutResultKey = line.substring(keyIndex + 9,
								line.length());
						String k = withOutResultKey.substring(0,
								withOutResultKey.indexOf("="));
						String v = withOutResultKey.substring(
								withOutResultKey.indexOf("=") + 1,
								withOutResultKey.length());
						result.getFaultMap().put(k, v);
						logger.info(logKey + " fault: " + k + ": " + v);
					}

					keyIndex = line.indexOf(TAG_KEY);
					if (keyIndex == 0) {
						String withOutResultKey = line.substring(keyIndex + 7,
								line.length());
						String k = withOutResultKey.substring(0,
								withOutResultKey.indexOf("="));
						String v = withOutResultKey.substring(
								withOutResultKey.indexOf("=") + 1,
								withOutResultKey.length());
						result.getTagMap().put(k, v);
						logger.info(logKey + " tag: " + k + ": " + v);
					}

					// multi-line attributes encoded w/ json
					keyIndex = line.indexOf(RESULTJSON_KEY);
					if (keyIndex == 0) {
						int firstEquals = line.indexOf("=");
						String key = line.substring(keyIndex + 14, firstEquals);
						String val = line.substring(firstEquals + 1,
								line.length());

						MultiLineValue value = gson.fromJson(val,
								MultiLineValue.class);

						result.getResultMap().put(key, value.getValue());
						if (!value.getValue().contains("PRIVATE KEY"))
							logger.debug(logKey + " resultCi " + key + ": "
									+ val);

					}

					keyIndex = line.indexOf(ADDITIONAL_INFO_KEY);
					if (keyIndex > -1) {
						int firstEquals = line.indexOf("=", keyIndex);
						if (firstEquals > -1) {
							String key = line.substring(keyIndex + ADDITIONAL_INFO_KEY.length(), firstEquals).trim();
							String value = line.substring(firstEquals + 1);
							logger.info(ADDITIONAL_INFO_KEY + " key: " + key + ", value: " + value);
							result.getAdditionInfoMap().put(key, value);
						}
					}

					// set last error to use if faults are empty
					keyIndex = line.indexOf("ERROR:");
					if (keyIndex > -1) {
						result.setLastError(line.substring(keyIndex+6,line.length()));
					}
					

				} else if (rowCount > maxRowCount) {
					continue;
				} else if (rowCount == maxRowCount) {
					logger.warn(logKey
							+ " hit max amt of output per process of "
							+ maxRowCount
							+ " lines. Please run the workorder on the box: chef-solo -c /home/oneops/cookbooks/chef.rb -j /opt/oneops/workorder/someworkorder ");
				}

				rowCount++;
			}

		} catch (IOException e) {
			logger.error("Error parsing output of command: ", e);
		}

	}

	@Override
	public void setProcessErrorStream(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {

			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {

				// allow 2x to get errors
				if (rowCount < maxRowCount * 2) {

					logger.info(logKey + "cmd error: " + line);
					result.appendStdErr(line + "\n");

				} else if (rowCount > maxRowCount * 2) {
					continue;
				} else if (rowCount == maxRowCount * 2) {
					logger.warn(logKey
							+ " hit max amt of output per process of "
							+ maxRowCount
							+ " lines. Please run the workorder on the box: chef-solo -c /home/oneops/cookbooks/chef.rb -j /opt/oneops/workorder/someworkorder ");
				}

				rowCount++;
			}

		} catch (IOException e) {
			logger.error("Error parsing output of command: ", e);
		}
	}

	@Override
	public void start() throws IOException {

	}

	@Override
	public void stop() {

	}

	@Override
	public void setProcessInputStream(OutputStream arg0) throws IOException {
		// TODO Auto-generated method stub

	}

}
