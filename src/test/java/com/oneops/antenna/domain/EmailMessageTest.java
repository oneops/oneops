package com.oneops.antenna.domain;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.oneops.antenna.domain.EmailMessage;

import static org.testng.Assert.*;

/**
 * The Class EmailMessageTest.
 */
public class EmailMessageTest {
	
	private final EmailMessage message = new EmailMessage();
	private List<String> toAddresses= new ArrayList<String>(2);
	private List<String> toCCs = new ArrayList<String>(2);
	

	@BeforeClass
	public void mailSetup(){
		message.setSubject("subject-mockito");
		message.setFromAddress(this.getClass().getName());
		message.setTxtMessage(this.toString());
		
		String[] toArray = new String[] {"adam@goog.com","zed@food.com"};
		String[] ccArray = new String[] {"barb@evite.com","yeti@drink.com"};

		toAddresses.addAll(Arrays.asList(toArray));
		toCCs.addAll(Arrays.asList(ccArray));
		message.setToAddresses(toAddresses);
		message.setToCcAddresses(toCCs);
	}
	
	/**
	 * Accessor validity.
	 */
	@Test
	/*bean type test that the access is the same as the setter
	 * and that once we add more entries we do not ruin the existing */
	public void accessorValidity(){
		assertTrue(message.getToAddresses().containsAll(toAddresses));
		assertTrue(message.getToCcAddresses().containsAll(toCCs));
		
		String newToRecipient = "chuck@raid.com";
		String newCcRecipient = "nolan@tank.com";
		
		message.addToAddress(newToRecipient);
		message.addToCcAddresses(newCcRecipient);
		assertTrue(message.getToAddresses().containsAll(toAddresses));
		assertTrue(message.getToCcAddresses().containsAll(toCCs));
		assertTrue(message.getToAddresses().contains(newToRecipient));
		assertTrue(message.getToCcAddresses().contains(newCcRecipient));
	 
	}
}
