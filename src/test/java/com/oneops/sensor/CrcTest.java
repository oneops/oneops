package com.oneops.sensor;

import java.util.zip.CRC32;

public class CrcTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CRC32 crc = new CRC32();
		
		String s = "as;ldkasldkgj;alskdgj;alsdkgja;sldgkjq";
		crc.update(s.getBytes());
		System.out.println("crc: " + crc.getValue());

	}

}
