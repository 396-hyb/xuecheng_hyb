package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;

/**
 * 测试minio的sdk
 */
public class MinioTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();


    @Test
    public void test_upload() throws Exception {

        //通过扩展名得到媒体资源类型 mimeType
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".png");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch != null){
            mimeType = extensionMatch.getMimeType();
        }

        try {
            //上传文件的参数信息
            UploadObjectArgs objectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("001/test001.png") //子目录
                    .filename("E:\\photo\\1.png")
                    .contentType(mimeType)
                    .build();

            //上传文件
            minioClient.uploadObject(objectArgs);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }


    }
    //删除文件
    @Test
    public void test_delete() throws Exception {

        try {
            //RemoveObjectArgs
            RemoveObjectArgs objectArgs = RemoveObjectArgs.builder().bucket("testbucket").object("001/test001.png").build();

            //删除文件
            minioClient.removeObject(objectArgs);
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除文件失败");
        }


    }

    //查询文件 从minio中下载
    @Test
    public void test_getFile() throws Exception {

        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket("testbucket").object("001/test001.png").build();

        //查询远程服务获取到一个流对象
        FilterInputStream inputStream = minioClient.getObject(objectArgs);
        //指定输出流
        FileOutputStream outputStream = new FileOutputStream(new File("E:\\photo\\1a.png"));
        IOUtils.copy(inputStream, outputStream);

        //校验文件的完整性对文件的内容进行md5
        FileInputStream fileInputStream = new FileInputStream(new File("E:\\photo\\1.png"));
        String md5Hex = DigestUtils.md5Hex(fileInputStream);
        FileInputStream fileInputStream1 = new FileInputStream(new File("E:\\photo\\1a.png"));
        String md5Hex1 = DigestUtils.md5Hex(fileInputStream1);
        if(md5Hex1.equals(md5Hex)){
            System.out.println("下载成功");
        }
    }


}
