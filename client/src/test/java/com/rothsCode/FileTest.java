package com.rothsCode;

import com.rothsCode.client.ClientConfig;
import com.rothsCode.client.DefaultFileSystem;
import com.rothsCode.client.FileClient;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/12 15:37
 */

public class FileTest {


    public DefaultFileSystem getClient() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setUserName("abc");
        clientConfig.setPassword("1234");
        return FileClient.getFileSystem(clientConfig);
    }

    @Test
    public void testDir() {
        DefaultFileSystem fileSystem = getClient();
        //创建目录
        fileSystem.makeDir("admin/tem/pic");
        System.out.println("添加完成");
        fileSystem.shutDown();
    }

    @Test
    public void testManyDir() {
        DefaultFileSystem fileSystem = getClient();
        //创建目录
        for (int i = 10; i < 50; i++) {
            fileSystem.makeDir("admin/tem/pic" + i);
            System.out.println("添加完成");
        }
        fileSystem.shutDown();
    }


    @Test
    public void testMultiDir() throws InterruptedException {
        DefaultFileSystem fileSystem = getClient();
        //创建目录
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        CountDownLatch countDownLatch = new CountDownLatch(6000);
        StopWatch s = new StopWatch();
        s.start();
        for (int i = 6000; i <= 10000; i++) {
            int finalI = i;
            executorService.execute(() -> {
                fileSystem.makeDir("admin/tem/pic" + finalI);
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        s.stop();
        fileSystem.shutDown();
        System.out.println("耗时:" + s.getTime());

    }

    @Test
    public void fileName() throws IOException {
        RandomAccessFile raf = new RandomAccessFile("D:\\tmp\\333.txt", "rw");
        System.out.println("当前记录指针位置：" + raf.getFilePointer());
        byte[] buf = new byte[(int) raf.length()];
        int len = 0;
        while ((len = raf.read(buf)) != -1) {
            System.out.println(new String(buf));
            System.out.println("当前记录指针位置：" + buf.length);
        }
    }

    @Test
    public void checkCRC32() throws IOException {
        File startFile = new File("D:\\tmp\\lsdfs\\datanode\\333.txt");
        long startCRC32 = FileUtils.checksumCRC32(startFile);
        File endFile = new File("D:\\tmp\\333.txt");
        long endCRC32 = FileUtils.checksumCRC32(endFile);
        Assert.assertEquals(startCRC32, endCRC32);

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
                System.out.println("长度" + hasRead);
                System.out.println("数据" + new String(b));
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

    @Test
    public void testChildPath() {
        DefaultFileSystem fileSystem = getClient();
        List<String> paths = fileSystem.getChildDirs("admin/tem");
        fileSystem.shutDown();
    }

    @Test
    public void testDownFile() {
        DefaultFileSystem fileSystem = getClient();
        fileSystem.downFile("333.txt", "D:\\tmp\\lsdfs\\client\\downFile\\");
        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fileSystem.shutDown();


    }
}
