package webcurve.util;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.PropertyConfigurator;
/**
 * @author dennis_d_chen@yahoo.com
 */
public class Log4JSetup {
	/*      samples from log4j
		
    log4j.rootLogger=debug, stdout, R

    log4j.appender.stdout=org.apache.log4j.ConsoleAppender
    log4j.appender.stdout.layout=org.apache.log4j.PatternLayout

    # Pattern to output the caller's file name and line number.
    log4j.appender.stdout.layout.ConversionPattern=%5p [%t] (%F:%L) - %m%n

    log4j.appender.R=org.apache.log4j.RollingFileAppender
    log4j.appender.R.File=example.log

    log4j.appender.R.MaxFileSize=100KB
    # Keep one backup file
    log4j.appender.R.MaxBackupIndex=1

    log4j.appender.R.layout=org.apache.log4j.PatternLayout
    log4j.appender.R.layout.ConversionPattern=%p %t %c - %m%n
*/            		
/*
	public static void applicationSetup()
	{
        // Set up a simple configuration that logs on the console.

        BasicConfigurator.configure();

        
 	   Properties properties = new Properties();
	   properties.put("log4j.rootLogger","debug, stdout, file");
	   properties.put("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
	   properties.put("log4j.appender.stdout.layout","org.apache.log4j.PatternLayout");
	   properties.put("log4j.appender.stdout.layout.ConversionPattern","%d{HH:mm:ss.SSS} %5p %M - %m [%t] %n");
	   
	   properties.put("log4j.appender.file","org.apache.log4j.RollingFileAppender");
	   properties.put("log4j.appender.file.layout","org.apache.log4j.PatternLayout");
	   properties.put("log4j.appender.file.layout.ConversionPattern","%d{HH:mm:ss.SSS} %5p [%t] - %m%n");
	   properties.put("log4j.appender.file.File","app.log");
	   
	   PropertyConfigurator.configure(properties);
        		
	}
	public static void appletSetup()
	{
        // Set up a simple configuration that logs on the console.
    	
        BasicConfigurator.configure();

        
 	   Properties properties = new Properties();
	   properties.put("log4j.rootLogger","debug, stdout");
	   properties.put("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
	   properties.put("log4j.appender.stdout.layout","org.apache.log4j.PatternLayout");
	   properties.put("log4j.appender.stdout.layout.ConversionPattern","%d{HH:mm:ss.SSS} %5p %M - %m [%t] %n");
   	   
	   PropertyConfigurator.configure(properties);
        		
	}
*/	
}
