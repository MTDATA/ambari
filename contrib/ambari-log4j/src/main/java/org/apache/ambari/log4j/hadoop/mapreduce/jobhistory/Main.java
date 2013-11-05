/*
 * Copyright (c) 2010-2012 meituan.com
 * All rights reserved.
 * 
 */
package org.apache.ambari.log4j.hadoop.mapreduce.jobhistory;

import org.apache.ambari.log4j.common.MappedByteBufferWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Category;
import org.apache.log4j.spi.LoggingEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author chenchun
 * @version 1.0
 * @created 2013-09-04
 */
public class Main {

  private static final Log LOG = LogFactory.getLog(Main.class);

  static final char JOB_HISTORY_LINE_DELIMITER_CHAR = '.';
  static final String LOG_FLAG = " DEBUG org.apache.hadoop.mapred.JobHistory$JobHistoryLogger: ";
  static int LOG_PREFIX_LENGTH = "2013-09-03 23:59:43,795".length() + LOG_FLAG.length();

  public static void main(String[] args) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, InterruptedException {
    if (args.length < 2) {
      LOG.error("must specify a jobtrackcer log file name and a config file name");
      System.exit(1);
    }
    String fileName = args[0], configFileName = args[1];
    File f = new File(fileName), configFile = new File(configFileName);
    if (!f.exists()) {
      LOG.error("file " + fileName +" not exist. ");
      System.exit(1);
    }
    if (!configFile.exists()) {
      LOG.error("file " + configFileName +" not exist. ");
      System.exit(1);
    }
    Properties config = loadProperties(configFileName);
    if (config == null) {
      LOG.error("can't load properties from " + configFileName +" .");
      System.exit(1);
    }
    long startTime = System.currentTimeMillis();
    String database = config.getProperty("database.url");
    String driver = config.getProperty("database.driverClassName");
    String user = config.getProperty("database.username");
    String password = config.getProperty("database.password");
    if (args.length >= 3 && "--clean".equals(args[2])) {
      String dateStr = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
      Date date = parse(dateStr);
      date = date == null? new Date() : date;
      try {
        clean(database, user, password, driver, firstMin(date).getTime(), lastMin(date).getTime());
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
        System.exit(1);
      }
    }
    JobHistoryAppender appender = new JobHistoryAppender();
    appender.setDatabase(database);
    appender.setDriver(driver);
    appender.setUser(user);
    appender.setPassword(password);
    appender.activateOptions();
    Constructor<?> constructor = Category.class.getDeclaredConstructor(String.class);
    constructor.setAccessible(true);
    Category c = (Category) constructor.newInstance("test");
    LOG.info("begin parse jobtracker log :");
    LOG.info("logFile=" + fileName);
    LOG.info("configFile=" + configFileName);
    LOG.info("database=" + database);
    LOG.info("driver=" + driver);
    LOG.info("user=" + user);
    LOG.info("password=" + password);
    StringBuilder sb = new StringBuilder();
    int i = 0;
    MappedByteBufferWrapper mappedByteBufferWrapper = null;
    long position = 0;
    String log;
    Date today = firstMin(new Date());
    while (true) {
      i++;
      if (i % 1000 == 0) {
        LOG.info("parse line " + i);
      }
      if (mappedByteBufferWrapper == null || (log = mappedByteBufferWrapper.readLine()) == null) {
        if (mappedByteBufferWrapper != null) {
          mappedByteBufferWrapper.close();
          Thread.sleep(3 * 1000);
        }
        Date now = firstMin(new Date());
        if (!now.equals(today)) {
          position = 0;
        }
        mappedByteBufferWrapper = new MappedByteBufferWrapper(fileName, "r", position, FileChannel.MapMode.READ_ONLY);
        position = mappedByteBufferWrapper.nextPosition();
      } else {
        if (log.contains(LOG_FLAG)) {
          log = log.substring(LOG_PREFIX_LENGTH);
          if (log.charAt(log.length() - 1) == JOB_HISTORY_LINE_DELIMITER_CHAR) {
            LoggingEvent loggingEvent = newLoggingEvent(c, log);
            appender.append(loggingEvent);
          } else {
            sb.append(log);
          }
        } else {
          if (sb.length() != 0) {
            sb.append(log);
            if (log.length() > 1 && log.charAt(log.length() - 1) == JOB_HISTORY_LINE_DELIMITER_CHAR) {
              LoggingEvent loggingEvent = newLoggingEvent(c, sb.toString());
              appender.append(loggingEvent);
              sb = new StringBuilder();
            }
          }
        }
      }
    }
  }


  private static LoggingEvent newLoggingEvent(Category c, String message)
          throws IllegalAccessException, InvocationTargetException, InstantiationException {
    return new LoggingEvent(null, c, null, message, null);
  }

  /**
   * load properties
   *
   * @param file
   * @return
   */
  public static Properties loadProperties(String file) {
    try {
      Properties properties = new Properties();
      InputStream stream = new FileInputStream(file);
      properties.load(stream);
      return properties;
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    return null;
  }

  public static void clean(String url, String username, String password, String driver, long begin,
                           long end) throws ClassNotFoundException, SQLException {
    Class.forName(driver);
    Connection connect = DriverManager.getConnection(url, username, password);
    PreparedStatement deleteTaskAttemptPS = connect.prepareStatement("delete from "
            + MapReduceJobHistoryUpdater.TASKATTEMPT_TABLE + " where startTime between ? and ?"),
            deleteTaskPS = connect.prepareStatement("delete from "
                    + MapReduceJobHistoryUpdater.TASK_TABLE + " where startTime between ? and ?"),
            deleteJobPS = connect.prepareStatement("delete from "
                    + MapReduceJobHistoryUpdater.JOB_TABLE + " where submitTime between ? and ?"),
            deleteEtlWorkFlowPS = connect.prepareStatement("delete etlwf from "
                    + MapReduceJobHistoryUpdater.ETL_WORKFLOW_TABLE + " etlwf "
                    + " inner join " + MapReduceJobHistoryUpdater.WORKFLOW_TABLE + " wf "
                    + " on wf.workflowId = etlwf.workflowId "
                    + " where wf.startTime between ? and ?"),
            deleteWorkFlowPS = connect.prepareStatement("delete from "
                    + MapReduceJobHistoryUpdater.WORKFLOW_TABLE + " where startTime between ? and ?");
    deleteTaskAttemptPS.setLong(1, begin);
    deleteTaskAttemptPS.setLong(2, end);
    deleteTaskPS.setLong(1, begin);
    deleteTaskPS.setLong(2, end);
    deleteJobPS.setLong(1, begin);
    deleteJobPS.setLong(2, end);
    deleteEtlWorkFlowPS.setLong(1, begin);
    deleteEtlWorkFlowPS.setLong(2, end);
    deleteWorkFlowPS.setLong(1, begin);
    deleteWorkFlowPS.setLong(2, end);
    connect.setAutoCommit(false);
    LOG.info("clean data between " + begin + " " + end);
    LOG.info("delete " + MapReduceJobHistoryUpdater.TASKATTEMPT_TABLE + " " + deleteTaskAttemptPS.executeUpdate() + " rows");
    LOG.info("delete " + MapReduceJobHistoryUpdater.TASK_TABLE + " " + deleteTaskPS.executeUpdate() + " rows");
    LOG.info("delete " + MapReduceJobHistoryUpdater.JOB_TABLE + " " + deleteJobPS.executeUpdate() + " rows");
    LOG.info("delete " + MapReduceJobHistoryUpdater.ETL_WORKFLOW_TABLE + " " + deleteEtlWorkFlowPS.executeUpdate() + " rows");
    LOG.info("delete " + MapReduceJobHistoryUpdater.WORKFLOW_TABLE + " " + deleteWorkFlowPS.executeUpdate() + " rows");
    connect.commit();
    connect.close();
  }

  public static Date parse(String date) {
    try {
      return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    } catch (ParseException e) {
    }
    return null;
  }

  /**
   * return last minute of date
   */
  public static Date lastMin(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  /**
   * return first minute of date
   */
  public static Date firstMin(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

}
