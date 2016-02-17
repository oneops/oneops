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
package com.oneops.util;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MessageSender {

	private int concurrency;
	private AtomicBoolean isRunning = new AtomicBoolean(false);
	private AsyncSearchPublisher searchPublisher;
	
	private AtomicLong sequence = new AtomicLong(0);
	private AtomicLong counter = new AtomicLong(0);
	private final Random rand = new Random();
	
	private ExecutorService executors;
	private Thread taskProviderThread;
	private long lastRunPendingCount;
	
	private String baseMsg = "{\"deploymentId\":%s,\"releaseId\":546559,\"maxExecOrder\":6,\"nsPath\":\"/local-dev/prod1/dev/bom\",\"created\":\"%2$ta %2$tb %2$td %2$tT %2$tZ %2$tY\",\"updated\":\"%2$ta %2$tb %2$td %2$tT %2$tZ %2$tY\"}";
	
	public MessageSender(int concurrency, AsyncSearchPublisher searchPublisher) {
		this.concurrency = concurrency;
		this.searchPublisher = searchPublisher;
	}
	
	public void init() {
		executors = Executors.newFixedThreadPool(concurrency);
	}
	
	public void start() {
		if (isRunning.compareAndSet(false, true)) {
			if (executors.isShutdown()) {
				executors = Executors.newFixedThreadPool(concurrency);
			}
			taskProviderThread = new Thread(new TaskSubmitter());
			taskProviderThread.start();
		}
	}
	
	public void stop() {
		if (isRunning.compareAndSet(true, false)) {
			try {
				taskProviderThread.interrupt();
				executors.shutdown();
				executors.awaitTermination(1, TimeUnit.SECONDS);
				if (!executors.isTerminated()) {
					lastRunPendingCount = executors.shutdownNow().size();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}
	
	private MessageData getData() {
		long id = sequence.getAndIncrement();
		String payload = String.format(baseMsg, id, Calendar.getInstance());
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("messageId", Long.toString(id));
		headers.put("source", "test");
		MessageData data = new MessageData(payload, headers);
		return data;
	}
	
	
	class TaskSubmitter implements Runnable {
		
		@Override
		public void run() {
			while (isRunning.get()) {
				executors.submit(() -> {
					try {
						searchPublisher.publishAsync(getData());
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					counter.getAndIncrement();
				});
				try {
					Thread.sleep(rand.nextInt(10));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public long getCount() {
		return counter.get();
	}

	public long getLastRunPendingCount() {
		return lastRunPendingCount;
	}

	public void setConcurrency(int concurrency) {
		this.concurrency = concurrency;
	}

	public void setSearchPublisher(AsyncSearchPublisher searchPublisher) {
		this.searchPublisher = searchPublisher;
	}
	
}
