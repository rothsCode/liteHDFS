package namenode.test;

import com.rothsCode.litehdfs.common.netty.request.FileInfo;
import com.rothsCode.litehdfs.namenode.filetree.DirNode;
import com.rothsCode.litehdfs.namenode.filetree.FileDirectoryTree;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author roths
 * @Description:
 * @date 2023/10/31 16:48
 */
public class FileDirectoryTest {


  @Test
  public void testMakeDir() {
    FileDirectoryTree fileDirectoryTree = new FileDirectoryTree();
    String parentDirPath = "adb/fegg/";
    String dirPath = "geg";
    fileDirectoryTree.makeDir(parentDirPath + dirPath);
    List<DirNode> dirNodeList = fileDirectoryTree.listFiles(parentDirPath);
    Assert.assertTrue(dirNodeList.get(0).getPath().equals(dirPath));
  }

  @Test
  public void testDeleteDir() {
    FileDirectoryTree fileDirectoryTree = new FileDirectoryTree();
    String parentDirPath = "adb/fegg/";
    String dirPath = "geg";
    fileDirectoryTree.makeDir(parentDirPath + dirPath);
    List<DirNode> dirNodeList = fileDirectoryTree.listFiles(parentDirPath);
    Assert.assertTrue(CollectionUtils.isNotEmpty(dirNodeList));
    Assert.assertTrue(fileDirectoryTree.deleteDir(parentDirPath));
  }

  @Test
  public void testCreateFile() {
    FileDirectoryTree fileDirectoryTree = new FileDirectoryTree();
    String dirPath = "adb/fegg/xxx.jpg";
    FileInfo fileInfoParam = FileInfo.builder().absolutePath(dirPath).build();
    fileDirectoryTree.makeFileNode(dirPath, fileInfoParam);
    FileInfo fileInfo = fileDirectoryTree.getFileInfoByPath(dirPath);
    Assert.assertTrue(
        fileInfo != null && fileInfo.getAbsolutePath().equals(fileInfoParam.getAbsolutePath()));
  }

  @Test
  public void testDeleteFile() {
    FileDirectoryTree fileDirectoryTree = new FileDirectoryTree();
    String dirPath = "adb/fegg/xxx.jpg";
    FileInfo fileInfoParam = FileInfo.builder().absolutePath(dirPath).build();
    fileDirectoryTree.makeFileNode(dirPath, fileInfoParam);
    Assert.assertTrue(fileDirectoryTree.deleteFileNode(dirPath));
  }
}
