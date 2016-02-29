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


import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public abstract class ReliableExecutor <I> {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private Class<I> clazz;

	private static final String PROPERTY_CONF = "/oneops-config.properties";
	private static final String SCAN_PERIOD_PROP = "oo.antenna.client.scan.period";
	private static final String SCAN_FOLDER_PROP = "oo.antenna.client.scan.folder";

	protected long scanPeriod = 5;
	protected String scanFolder;

	protected int backlogThreshold = 1000;
	protected String name;
	protected String shortName;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1 );
	
	private final ExecutorService executors = Executors.newCachedThreadPool();
	
	protected Gson gson = new Gson();
	
	public void setScanPeriod( int scanPeriod ) {
		this.scanPeriod = scanPeriod;
	}

	public void setScanFolder( String scanFolder ) {
		this.scanFolder = scanFolder;
	}

	@SuppressWarnings("unchecked")
	public void init() {
		this.checkAndCreateFolder();
		this.clazz = (Class<I>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		this.scheduler.scheduleWithFixedDelay( scanner, scanPeriod, scanPeriod, SECONDS );
	}
	
	public void executeAsync(I param) {
		Executor task = new Executor( param );
		executors.submit(task);
	}

	public boolean executeSync(I param) {
		return firstOneRun( param );
	}

	public void destroy() {
		scheduler.shutdown();
	}

	@SuppressWarnings("unused")
	private void loadPropertyConfiguration() {
		InputStream is = this.getClass().getResourceAsStream(PROPERTY_CONF);
		if(is != null) {
			Properties p =new Properties();
			try {
				p.load(is);
			} catch( IOException e ) {
				e.printStackTrace();
			}

			String sp = p.getProperty(SCAN_PERIOD_PROP);
			if(sp != null) {
				this.scanPeriod = Integer.parseInt(sp);
			}
			String sf = p.getProperty(SCAN_FOLDER_PROP);
			if(sf != null) {
				this.scanFolder = sf;
			}
		}

	}

	private void checkAndCreateFolder() {
		if(this.scanFolder != null) {
			File f = new File(this.scanFolder);
			if(f.exists()) {
				if(f.isFile()) {
					throw new RuntimeException( "Given path: "+this.scanFolder+" already exists as file!" );
				}
			} else {
				if(f.mkdirs()) {
					logger.info( "Folder "+this.scanFolder+" has been created successfully." );
				} else {
					throw new RuntimeException( "Given path:"+this.scanFolder+" is not valid!" );
				}
			}
		} else {
			throw new RuntimeException( "Scan folder path cnnot be null!" );
		}
	}
    
	abstract protected boolean process(I param);

	final Runnable scanner = new Runnable() {
        public void run() {
        	logger.trace("Scanning folder ...");
	        File folder = new File( scanFolder );
	        for(String fileName: new TreeSet<String>( Arrays.asList(folder.list()))) {
		        File file = new File(scanFolder + File.separator + fileName);
		        logger.trace("Found file : {}",file);
		        try {
			        Reader reader = new FileReader(file);
			        I param = gson.fromJson( reader , clazz );
			        reader.close();
			        if(process(param)) {
			            if(!file.delete()) {
				            logger.error( "File {} cannot be deleted.", file.getName() );
		                }
			        } else {
				        break;
			        }
		        } catch( FileNotFoundException e ) {
			        logger.error(  "File {} not found.", file.getName() );
		        } catch( IOException e ) {
			        logger.error( e.getMessage());
		        }
	        }
	        logger.trace("Scanning folder finish.");
        }
    };
 
	private boolean firstOneRun(I param) {
		if( process( param ) ){
		    return true;
		}
		try {
			if (name != null) {
				logger.warn(name + " execution failed. storing data to a file.");	
			}
			
			checkBacklog();
			FileWriter wr  = new FileWriter( getFileName(param) );
			gson.toJson(param,  wr );
			wr.close();
		} catch( Exception e ) {
			logger.error( e.getMessage() );
			logger.debug( e.getMessage(), e );
		}
		return false;
	}
	
	private void checkBacklog() {
		File folder = new File(scanFolder);
		String[] files = folder.list();
		if (name != null) {
			if (files.length > backlogThreshold) {
				logger.warn(name + " - retry backlog is high : " + files.length);
			}
		}
	}
	
	private String getFileName(I param) {
		String fileName = null;
		if (StringUtils.isEmpty(shortName)) {
			fileName = scanFolder + File.separator + System.currentTimeMillis() + String.valueOf(param.hashCode());	
		}
		else {
			fileName = scanFolder + File.separator + shortName + "-" + System.currentTimeMillis() + String.valueOf(param.hashCode());
		}
		return fileName;
	}

	class Executor implements Runnable {

		private I param;

		Executor(I param) {
			this.param = param;
		}

		@Override
		public void run() {
			firstOneRun( param );
		}
	}

	public void setBacklogThreshold(int backlogThreshold) {
		this.backlogThreshold = backlogThreshold;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
}
