package com.rothsCode.litehdfs;

import com.rothsCode.litehdfs.client.ClientConfig;
import com.rothsCode.litehdfs.client.DefaultFileSystem;
import com.rothsCode.litehdfs.client.FileClientFactory;
import com.rothsCode.litehdfs.common.file.FileAppender;
import com.rothsCode.litehdfs.common.netty.response.ServerResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author rothscode
 * @Description:
 * @date 2021/11/12 15:37
 */
@Slf4j
public class FileTest {


  private DefaultFileSystem getClient() {
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.setUserName("abc");
    clientConfig.setPassword("1234");
    return FileClientFactory.createFileSystemClient(clientConfig);
  }


  @Test
  public void testUploadFile() {
    DefaultFileSystem fileSystem = getClient();
    File file = new File("D:\\logs\\custom55.properties");
    ServerResponse serverResponse = fileSystem.uploadFile(file);
    fileSystem.shutDown();
    Assert.assertTrue(serverResponse.getSuccess());

  }

  @Test
  public void testUploadFileManyBlock() {
    DefaultFileSystem fileSystem = getClient();
    File file = new File("D:\\tmp\\sync7.txt");
    ServerResponse serverResponse = fileSystem.uploadFile(file);
    fileSystem.shutDown();
    Assert.assertTrue(serverResponse.getSuccess());
  }

  @Test
  public void testDownFile() {
    DefaultFileSystem fileSystem = getClient();
    // sync6.txt
    ServerResponse serverResponse = fileSystem.downFile("sync7.txt");
    fileSystem.shutDown();
    Assert.assertTrue(serverResponse.getSuccess());

  }

  @Test
  public void testGetChildPath() {
    DefaultFileSystem fileSystem = getClient();
    ServerResponse serverResponse = fileSystem
        .getChildDirs("/usr/redis");
    Assert.assertTrue(serverResponse.getSuccess());
    fileSystem.shutDown();
  }

  @Test
  public void testMakeDir() {
    DefaultFileSystem fileSystem = getClient();
    //创建目录
    ServerResponse serverResponse = fileSystem.makeDir("/usr/redis/fefg/4533");
    fileSystem.shutDown();
    Assert.assertTrue(serverResponse.getSuccess());
  }

  @Test
  public void testManyDir() {
    DefaultFileSystem fileSystem = getClient();
    //创建目录
    for (int i = 1; i < 10; i++) {
      fileSystem.makeDir("usr/redis/t/" + i);
    }
    fileSystem.shutDown();
  }
  @Test
  public void testMultiDir() throws InterruptedException {
    DefaultFileSystem fileSystem = getClient();
    //创建目录
    ExecutorService executorService = Executors.newFixedThreadPool(50);
    CountDownLatch countDownLatch = new CountDownLatch(1000000);
    StopWatch s = new StopWatch();
    s.start();
    final String prePath = "admin/s/txt";
    for (int i = 1; i <= 1000000; i++) {
      int finalI = i;
      executorService.execute(() -> {
        fileSystem.makeDir(prePath + finalI);
        countDownLatch.countDown();
      });
    }
    countDownLatch.await();
    s.stop();
    executorService.shutdown();
    fileSystem.shutDown();
    log.info("耗时:" + s.getTime());

  }

  @Test
  public void fileName() throws IOException {
    RandomAccessFile raf = new RandomAccessFile("D:\\tmp\\333.txt", "rw");
    log.error("当前记录指针位置：" + raf.getFilePointer());
    byte[] buf = new byte[(int) raf.length()];
    int len = 0;
    while ((len = raf.read(buf)) != -1) {
      log.error(new String(buf));
      log.error("当前记录指针位置：" + buf.length);
    }
  }

  @Test
  public void checkFileCode() throws IOException {
    File endFile = new File(
        "D:\\tmp\\liteHDFS\\datanode\\8511\\usr\\redis\\BLK1-custom3.properties");
    FileAppender fileAppender = new FileAppender(endFile);
    String endCRC512 = fileAppender.getFileCheckCode();
    String endCRC32 = DigestUtils.sha512Hex(new FileInputStream(endFile));
    File startFile = new File("D:\\tmp\\lsdfs\\datanode\\34.txt");
    String startCRC32 = DigestUtils.sha512Hex(new FileInputStream(startFile));

    Assert.assertEquals(startCRC32, endCRC32);

  }

  @Test
  public void testFileChannel() {
    File file = new File("D:\\tmp\\liteHDFS\\datanode\\8511\\usr\\redis\\851111.txt");
    FileAppender fileAppender = new FileAppender(file);
    fileAppender.append("hello".getBytes());
  }


  @Test
  public void testReadFile() throws IOException {
    String filePath = "C:\\Users\\rothsCode\\Desktop\\efeg.txt";
    RandomAccessFile raf = null;
    File file = null;
    try {
      file = new File(filePath);
      raf = new RandomAccessFile(file, "r");
      // 获取 RandomAccessFile对象文件指针的位置，初始位置为0
      //移动文件记录指针的位置

      byte[] b = new byte[56];
      int hasRead = 0;
      //循环读取文件
      while ((hasRead = raf.read(b)) > 0) {
        //输出文件读取的内容
        log.info("长度" + hasRead);
        log.info("数据" + new String(b));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      raf.close();
    }

  }

  @Test
  public void reName() throws IOException {
    File startFile = new File("D:\\tmp\\lsdfs\\namenode\\fileImage\\current.txt");
    File endFile = new File("D:\\tmp\\lsdfs\\namenode\\fileImage\\previous.txt");
    boolean flag = startFile.renameTo(endFile);
    Assert.assertTrue(flag);

  }



}
