/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.ambari.log4j.hadoop.mapreduce.jobhistory.JobHistoryAppender;
import org.apache.log4j.Category;
import org.apache.log4j.spi.LoggingEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 */
public class TestJobHistoryAppender extends TestCase {

  public void testEventQueueCapacity() throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
    JobHistoryAppender jobHistoryAppender = new JobHistoryAppender();
    Constructor<?> constructor = Category.class.getDeclaredConstructor(String.class);
    constructor.setAccessible(true);
    Category c = (Category) constructor.newInstance("test");
    for (int i = 0; i < JobHistoryAppender.QUEUE_CAPACITY; i++) {
      LoggingEvent event = newLoggingEvent(c);
      jobHistoryAppender.append(event);
    }
    Assert.assertTrue(jobHistoryAppender.getEvents().size() == JobHistoryAppender.QUEUE_CAPACITY);
    LoggingEvent event = newLoggingEvent(c);
    try {
      jobHistoryAppender.append(event);
    } catch (Exception e) {
      fail("jobHistoryAppender should not throw an exception \n" + e);
    }
    Assert.assertTrue(jobHistoryAppender.getEvents().size() == JobHistoryAppender.QUEUE_CAPACITY);
  }

  private LoggingEvent newLoggingEvent(Category c) throws IllegalAccessException, InvocationTargetException, InstantiationException {
    return new LoggingEvent(null, c, null, null, null);
  }

}
