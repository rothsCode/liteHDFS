package com.rothsCode.litehdfs;

import com.rothsCode.litehdfs.common.file.FileAppender;
import java.io.File;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author roths
 * @Description:
 * @date 2023/11/28 11:09
 */
@Slf4j
public class FileAppenderTest {


  @Test
  public void testFileChannelSyncDisk() {
    File file = new File("D:\\tmp\\hdfs\\namenode\\sync.txt");
    FileAppender fileAppender = new FileAppender(file);
    String word = "gewgggggggggggggggggggggggggggggggggggggggggggeggwhaaaaaaaaaaahahahaaaaaaaaaa";
    byte[] body = word.getBytes();
    int i = 0;
    long startTime = System.currentTimeMillis();
    while (i++ < 1000000) {
      ByteBuffer byteBuffer = ByteBuffer.allocate(body.length + 4);
      byteBuffer.putInt(body.length);
      byteBuffer.put(body);
      fileAppender.syncDisk(byteBuffer);
    }
    log.info("sync cost:{}ms", System.currentTimeMillis() - startTime);

  }

}
