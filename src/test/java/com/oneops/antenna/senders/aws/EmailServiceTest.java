package com.oneops.antenna.senders.aws;


import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.oneops.antenna.domain.EmailSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.senders.aws.EmailService;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
/**
 * test class to cover EmailService
 *
 */
public class EmailServiceTest {

	EmailService emailService ;

	private static final String STUB_SECRET = "stub-secret";
	private static final String STUB_KEY = "stub-key";

	@BeforeClass
	/** setting up test */
	public void init(){
		this.emailService = new EmailService();;
		emailService.setAwsAccessKey(STUB_KEY);
		emailService.setAwsSecretKey(STUB_SECRET);
		emailService.init();
	}

	@Test
	/** simple beans test */
	public void accessors(){
		emailService.setAwsAccessKey(STUB_KEY);
		emailService.setAwsSecretKey(STUB_SECRET);
		assertEquals(STUB_KEY,this.emailService.getAwsAccessKey());		
		assertEquals(STUB_SECRET,this.emailService.getAwsSecretKey());
	}
	

	/**
	 * send noop request to postMessage
	 * note the send email method cannot be mocked so not tested
	 */
	@Test
	public void postMessage(){
		NotificationMessage msg = new NotificationMessage();
		EmailSubscriber sub = mock(EmailSubscriber.class);
		when(sub.getEmail()).thenReturn(null);
		boolean result = this.emailService.postMessage(msg, sub);
		assertTrue(result);		
		
	}
}
