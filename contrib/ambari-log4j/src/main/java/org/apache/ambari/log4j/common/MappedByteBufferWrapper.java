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
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MappedByteBufferWrapper {

  private static final Log LOG = LogFactory.getLog(MappedByteBufferWrapper.class);

  private MappedByteBuffer mappedByteBuffer;
  private RandomAccessFile randomAccessFile;

  public MappedByteBufferWrapper(String name, String mode, long position, FileChannel.MapMode mapMode) throws IOException {
    randomAccessFile = new RandomAccessFile(name, mode);
    FileChannel fileChannel = randomAccessFile.getChannel();
    LOG.info("position=" + position + " mappedByteSize=" + (fileChannel.size() - position));
    mappedByteBuffer = fileChannel.map(mapMode, position, fileChannel.size() - position);
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
    mappedByteBuffer = null;
    if (randomAccessFile != null) {
      randomAccessFile.close();
      randomAccessFile = null;
    }
  }

  public long size() throws IOException {
    return randomAccessFile.getChannel().size();
  }

}
