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

import junit.framework.Assert;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static java.nio.file.FileVisitResult.CONTINUE;

public class SearchSenderTest {

	private ClassPathXmlApplicationContext context;
	private JMSConsumer consumer;
	private SearchPublisher searchPublisher;
	String retryDir;

	@BeforeTest
	public void initContext() {
		context = new ClassPathXmlApplicationContext("classpath:test-commons-context.xml");
		consumer = context.getBean(JMSConsumer.class);
		searchPublisher = context.getBean(SearchPublisher.class);
		retryDir = context.getBean("retryDir", String.class);
		while (!consumer.isStarted()) {
			//wait until the consumers are started
		}
	}
	
	
	@BeforeMethod
	public void init() {
		emptyRetryDir();
		consumer.reset();
	}

	@AfterTest
	public void tearDown() {
		context.close();
	}

	@Test
	public void testRegularSend() {		
		consumer.startRecording();
		String text = "{\"deploymentId\":546589,\"releaseId\":546559,\"maxExecOrder\":6,\"nsPath\":\"/local-dev/prod1/dev/bom\",\"deploymentState\":\"paused\",\"processId\":\"87820!87820\",\"createdBy\":\"bannama\",\"updatedBy\":\"bannama\",\"comments\":\"\",\"created\":\"Jan 5, 2016 7:15:37 PM\",\"updated\":\"Jan 8, 2016 3:56:55 PM\"}";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("messageId", "1");
		headers.put("source", "test");
		MessageData data = new MessageData(text, headers);
		searchPublisher.publish(data);
		await().atMost(5, TimeUnit.SECONDS).until(() -> (consumer.getCounter() == 1));
		Assert.assertEquals(consumer.getMessages().getFirst().getPayload(), text);
		Assert.assertEquals(consumer.getMessages().getFirst().getHeaders(), headers);
	}

	@Test
	public void testQueueFailure() {
		BrokerService searchBroker = context.getBean("searchBroker", BrokerService.class);
		try {
			searchBroker.stop();
		} catch (Exception e) {
			Assert.fail();
		}
		try {
			searchBroker.waitUntilStopped();
			if (searchBroker.isStopped()) {

				MessageData[] dataList = getMessages();

				for (MessageData data : dataList) {
					searchPublisher.publish(data);
				}

				Thread.sleep(2000);
				consumer.startRecording();
				searchBroker.start(true);
				searchBroker.waitUntilStarted(5000);
				if (searchBroker.isStarted()) {
					await().atMost(10, TimeUnit.SECONDS).until(() -> (consumer.getCounter() == 3));
					LinkedList<MessageData> list = consumer.getMessages();
					assertMessages(dataList, list.toArray(new MessageData[] {}));
				}
				consumer.reset();
				// send messages again
				for (MessageData data : dataList) {
					searchPublisher.publish(data);
				}
				await().atMost(10, TimeUnit.SECONDS).until(() -> (consumer.getCounter() == 3));
				LinkedList<MessageData> list = consumer.getMessages();
				assertMessages(dataList, list.toArray(new MessageData[] {}));
			}
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testMultipleFailures() {
		System.out.println("running testMultipleFailures");
		MessageSender sender = new MessageSender(5, searchPublisher);
		sender.init();
		sender.start();
		BrokerService searchBroker = context.getBean("searchBroker", BrokerService.class);
		try {
			// stop activemq broker
			stopBroker(searchBroker);
			int count = consumer.getCounter();
			Thread.sleep(2000);
			// verify that the failed messages are stored as files
			Assert.assertFalse("retry directory should not be empty", isRetryDirectoryEmpty());
			// start broker
			startBroker(searchBroker);
			await().atMost(10, TimeUnit.SECONDS).until(() -> (consumer.getCounter() > count));
			Thread.sleep(1500);
			// stop the sender and verify that all messages sent so far reached the queue
			sender.stop();
			await().atMost(10, TimeUnit.SECONDS).until(() -> (consumer.getCounter() == sender.getCount()));
			Assert.assertTrue("retry directory should be empty", isRetryDirectoryEmpty());
			
			// start sender again and verify that the messages are getting processed as usual
			sender.start();
			Thread.sleep(1500);
			sender.stop();
			await().atMost(10, TimeUnit.SECONDS).until(() -> (consumer.getCounter() == sender.getCount()));
			
			// again stop broker
			sender.start();
			stopBroker(searchBroker);
			Thread.sleep(2000);
			// start broker and verify that the messages reach the broker
			startBroker(searchBroker);
			sender.stop();
			await().atMost(10, TimeUnit.SECONDS).until(() -> (consumer.getCounter() == sender.getCount()));

		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	private boolean isRetryDirectoryEmpty() {
		DirectoryStream<Path> dirStream = null;
		try {
			Path retryPath = FileSystems.getDefault().getPath(retryDir);
			dirStream = java.nio.file.Files.newDirectoryStream(retryPath);
			return !dirStream.iterator().hasNext();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				dirStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private void emptyRetryDir() {
		Path directory = Paths.get(retryDir);
		try {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return CONTINUE;
				}
			});
			Files.createDirectories(directory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void startBroker(BrokerService searchBroker) throws Exception {
		searchBroker.start(true);
		searchBroker.waitUntilStarted(5000);
	}

	private void stopBroker(BrokerService searchBroker) throws Exception {
		searchBroker.stop();
		searchBroker.waitUntilStopped();
	}

	private void assertMessages(MessageData[] expected, MessageData[] actual) {
		if (ArrayUtils.isEmpty(expected) && ArrayUtils.isEmpty(actual))
			return;
		if (ArrayUtils.isEmpty(expected) || ArrayUtils.isEmpty(actual) || expected.length != actual.length) {
			Assert.fail("expected and actual array lengths are not matching");
		} else {
			Map<String, MessageData> expectedMap = new HashMap<String, MessageData>(expected.length);
			for (MessageData data : expected) {
				expectedMap.put(data.getPayload(), data);
			}
			for (MessageData data : actual) {
				MessageData matching = expectedMap.get(data.getPayload());
				Assert.assertNotNull(matching);
				Assert.assertEquals(matching.getHeaders(), data.getHeaders());
			}
		}
	}

	private MessageData[] getMessages() {
		String text1 = "{\"deploymentId\":546589,\"releaseId\":546559,\"maxExecOrder\":6,\"nsPath\":\"/local-dev/prod1/dev/bom\",\"deploymentState\":\"paused\",\"processId\":\"87820!87820\",\"createdBy\":\"bannama\",\"updatedBy\":\"bannama\",\"comments\":\"\",\"created\":\"Jan 5, 2016 7:15:37 PM\",\"updated\":\"Jan 8, 2016 8:12:34 PM\"}";
		Map<String, String> headers1 = new HashMap<String, String>();
		headers1.put("messageId", "1");
		headers1.put("source", "test1");
		MessageData data1 = new MessageData(text1, headers1);

		String text2 = "{\"deploymentId\":546590,\"releaseId\":546565,\"maxExecOrder\":6,\"nsPath\":\"/local-dev/prod1/dev1/bom\",\"deploymentState\":\"paused\",\"processId\":\"87821!87825\",\"createdBy\":\"bannama\",\"updatedBy\":\"bannama\",\"comments\":\"\",\"created\":\"Jan 6, 2016 7:15:37 PM\",\"updated\":\"Jan 9, 2016 3:33:55 PM\"}";
		Map<String, String> headers2 = new HashMap<String, String>();
		headers2.put("messageId", "2");
		headers2.put("source", "test2");
		MessageData data2 = new MessageData(text2, headers2);

		String text3 = "{\"deploymentId\":546591,\"releaseId\":546574,\"maxExecOrder\":6,\"nsPath\":\"/local-dev/prod1/dev2/bom\",\"deploymentState\":\"paused\",\"processId\":\"84320!87876\",\"createdBy\":\"bannama\",\"updatedBy\":\"bannama\",\"comments\":\"\",\"created\":\"Jan 7, 2016 7:15:37 PM\",\"updated\":\"Jan 10, 2016 1:26:55 PM\"}";
		Map<String, String> headers3 = new HashMap<String, String>();
		headers3.put("messageId", "3");
		headers3.put("source", "test3");
		MessageData data3 = new MessageData(text3, headers3);
		MessageData[] data = new MessageData[] { data1, data2, data3 };
		return data;
	}

}
