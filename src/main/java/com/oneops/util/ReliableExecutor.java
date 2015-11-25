package com.oneops.util;


import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class ReliableExecutor <I> {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private Class<I> clazz;

	private static final String PROPERTY_CONF = "/oneops-config.properties";
	private static final String SCAN_PERIOD_PROP = "oo.antenna.client.scan.period";
	private static final String SCAN_FOLDER_PROP = "oo.antenna.client.scan.folder";

	protected long scanPeriod = 5;
	protected String scanFolder;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1 );
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
		FirstOneThread th = new FirstOneThread( param );
		th.start();
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
			FileWriter wr  = new FileWriter( scanFolder + File.separator + System.currentTimeMillis() + String.valueOf(param.hashCode()));
			gson.toJson(param,  wr );
			wr.close();
		} catch( Exception e ) {
			logger.error( e.getMessage() );
			logger.debug( e.getMessage(), e );
		}
		return false;
	}

	class FirstOneThread extends Thread {

		private I param;

		FirstOneThread(I param) {
			this.param = param;
		}

		@Override
		public void run() {
			firstOneRun( param );
		}
	}
}
