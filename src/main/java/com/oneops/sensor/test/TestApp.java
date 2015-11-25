package com.oneops.sensor.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String condition = "avg(metrics('CpuIdle') + blah('aaa')) + sum(metrics('CpuIdle')) / historic('CpuIdle:3600:300:avg') * \n" + "historic('CpuIdle:3600:400:avg') * 100 < 90 + max(metrics('CpuIdle') + blah('aaa'))";
		Pattern histFunc = Pattern.compile("historic\\('(.*?)'\\)");
		Matcher histMatch = histFunc.matcher(condition);
		while (histMatch.find()) {
			System.out.println(histMatch.group());
			System.out.println(histMatch.group(1));
		}

		System.out.println();
		
		//Pattern aggFunc = Pattern.compile("(avg|sum|max|min)\\((.*?)\\)+)\\)");
		Pattern aggFunc = Pattern.compile("(avg|sum|max|min)\\((.*?(\\(.*?\\))+)\\)");
		//  /avg\((.*?(\(.*?\))+)\)/
		
		Matcher aggMatch = aggFunc.matcher(condition);
		while (aggMatch.find()) {
			System.out.println(aggMatch.group());
			//System.out.println(aggMatch.group(1));
		}
		
		
	}

}
