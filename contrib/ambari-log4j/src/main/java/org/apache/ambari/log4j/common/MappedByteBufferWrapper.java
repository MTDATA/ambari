/*
 * Copyright (c) 2010-2012 meituan.com
 * All rights reserved.
 * 
 */
package org.apache.ambari.log4j.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class MappedByteBufferWrapper {

  private static final Log LOG = LogFactory.getLog(MappedByteBufferWrapper.class);

  private MappedByteBuffer mappedByteBuffer;
  private RandomAccessFile randomAccessFile;
  private long nextPosition;
  private final static long DEFAULT_SIZE = 1024 * 1024 * 100;

  public MappedByteBufferWrapper(RandomAccessFile file, long position,
                                 FileChannel.MapMode mapMode) throws IOException {
    randomAccessFile = file;
    FileChannel fileChannel = randomAccessFile.getChannel();
    long size = Math.min(DEFAULT_SIZE, fileChannel.size() - position);
    nextPosition = position + size;
    LOG.info("position=" + position + " mappedByteSize=" + size + " nextPosition=" + nextPosition);
    mappedByteBuffer = fileChannel.map(mapMode, position, size);
  }

  public String readLine() {
    StringBuilder input = new StringBuilder();
    byte c = -1;
    boolean eol = false;

    while (!eol) {
      try {
        switch (c = mappedByteBuffer.get()) {
          case -1:
          case '\n':
            eol = true;
            break;
          default:
            input.append((char) c);
            break;
        }
      } catch (BufferUnderflowException e) {
        eol = true;
      }
    }
    if ((c == -1) && (input.length() == 0)) {
      return null;
    }
    return input.toString();
  }

  public void close() throws IOException {
    clean();
    mappedByteBuffer = null;
    if (randomAccessFile != null) {
      randomAccessFile.close();
      randomAccessFile = null;
    }
  }

  private void clean() {
    AccessController.doPrivileged(new PrivilegedAction<Object>() {
      public Object run() {
        try {
          if (mappedByteBuffer != null) {
            Method cleanerMethod = mappedByteBuffer.getClass().getMethod("cleaner");
            cleanerMethod.setAccessible(true);
            sun.misc.Cleaner cleaner = (sun.misc.Cleaner) cleanerMethod.invoke(mappedByteBuffer);
            cleaner.clean();
          }
        } catch (Exception e) {
          LOG.error(e.getMessage(), e);
        }
        return null;
      }
    });
  }

  public long nextPosition() throws IOException {
    return nextPosition;
  }

}
