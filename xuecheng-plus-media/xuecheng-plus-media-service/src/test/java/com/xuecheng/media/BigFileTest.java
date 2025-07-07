package com.xuecheng.media;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hyb
 * @version 1.0
 * @description 大文件处理测试
 * @date 2024/11/28
 */

public class BigFileTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    //测试文件分块方法
    @Test
    public void testChunk() throws IOException {
       /* 1、获取源文件长度
        2、根据设定的分块文件的大小计算出块数
        3、从源文件读数据依次向每一个块文件写数据。*/

        //要上传文件
        File sourceFile = new File("F:\\Java learning\\video\\xuecheng_27040550567.mp4");

        //文件上传后文件夹
        String chunkPath = new String("F:\\Java learning\\video\\chunk\\");
        File chunkFolder = new File(chunkPath);
        if(!chunkFolder.exists()){
            chunkFolder.mkdirs();
        }

        //文件块大小
        long chunkSize = 1024 * 1024 * 5;

        //文件块数量
        long chunkNumber = (long) Math.ceil((double) sourceFile.length() / chunkSize);
        System.out.println("分块总数：" + chunkNumber);

        //RandomAccessFile类
        RandomAccessFile fileRead = new RandomAccessFile(sourceFile,"r");

        //循环读取
        byte[] b = new byte[1024];

        for (int i = 0; i < chunkNumber; i++) {
            //创建分块文件
            File chunk = new File(chunkPath + i);
            if(chunk.exists()){
                chunk.delete();  //可能这个文件块上传了一半就终止了，所以要删除
            }
            boolean chunkNew = chunk.createNewFile();
            if(chunkNew){
                RandomAccessFile chunkWrite = new RandomAccessFile(chunk, "rw");
                int len = -1;
                while((len = fileRead.read(b)) != -1){
                    chunkWrite.write(b,0,len);
                    if(chunk.length() >= chunkSize)
                        break;
                }
                chunkWrite.close();
                System.out.println("完成分块" + i);
            }
        }

        fileRead.close();
    }

    //测试文件合并方法
    @Test
    public void testMerge() throws IOException {
        /*
        文件合并流程：
        1、找到要合并的文件并按文件合并的先后进行排序。
        2、创建合并文件
        3、依次从合并的文件中读取数据向合并文件写入数
        */
        File sourceFile = new File("F:\\Java learning\\video\\xuecheng_27040550567.mp4");
        File mergeFile = new File("F:\\Java learning\\video\\xuecheng_27040550567_1.mp4");
        if(mergeFile.exists()){
            mergeFile.delete();
        }
        boolean newFile = mergeFile.createNewFile();
        File chunkFolder = new File("F:\\Java learning\\video\\chunk\\");
        //用于写文件
        RandomAccessFile mergeWrite = new RandomAccessFile(mergeFile, "rw");
        //指针指向文件顶端
        mergeWrite.seek(0);

        File[] chunkArr = chunkFolder.listFiles();
        List<File> chunkList = null;
        if(chunkArr != null){
            chunkList = Arrays.asList(chunkArr);
            Collections.sort(chunkList, (File o1, File o2) -> Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName()));
        }else{
            System.out.println("分块文件夹为空");
            return;
        }
        //缓冲区
        byte[] b = new byte[1024];
        for (File file : chunkList) {
            RandomAccessFile fileRead = new RandomAccessFile(file, "r");
            int len = -1;
            while((len = fileRead.read(b)) != -1){
                mergeWrite.write(b,0,len);
            }
            fileRead.close();
        }
        mergeWrite.close();

        //校验文件的完整性,对文件的内容进行md5
        FileInputStream fileInputStream = new FileInputStream(sourceFile);
        String md5Hex = DigestUtils.md5Hex(fileInputStream);
        FileInputStream fileInputStream1 = new FileInputStream(mergeFile);
        String md5Hex1 = DigestUtils.md5Hex(fileInputStream1);
        if(md5Hex1.equals(md5Hex)){
            System.out.println("合并文件块成功");
        }

    }

    //将分块文件上传至minio
    @Test
    public void uploadChunk() throws IOException {
        String chunkPath = new String("F:\\Java learning\\video\\chunk\\");
        File chunkFolder = new File(chunkPath);
        File[] chunkArr = chunkFolder.listFiles();
        List<File> chunkList = null;
        if(chunkArr != null){
            chunkList = Arrays.asList(chunkArr);
            Collections.sort(chunkList, (File o1, File o2) -> Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName()));
        }
        for (int i=0; i<chunkList.size(); i++) {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs
                    .builder()
                    .bucket("testbucket")
                    .object("chunk/" + i)
                    .filename(chunkArr[i].getAbsolutePath())
                    .build();
            try {
                minioClient.uploadObject(uploadObjectArgs);
                System.out.println("上传分块成功"+i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }

    //合并文件，要求分块文件最小5M
    @Test
    public void testMinioMerge(){
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(24)
                .map(i -> ComposeSource.builder()
                            .bucket("testbucket")
                            .object("chunk/".concat(Integer.toString(i)))
                            .build())
                .collect(Collectors.toList());
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder().bucket("testbucket").object("merge01.mp4").sources(sources).build();
        try {
            minioClient.composeObject(composeObjectArgs);
            System.out.println("文件合并成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //清除分块文件
    @Test
    public void testRemoveObjects(){
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                .limit(24)
                .map(i -> new DeleteObject("chunk/".concat(Integer.toString(i))))
                .collect(Collectors.toList());
        RemoveObjectsArgs removeObjectArgs = RemoveObjectsArgs.builder()
                .bucket("testbucket")
                .objects(deleteObjects)
                .build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectArgs);
        results.forEach(r -> {
            DeleteError deleteError = null;
            try {
                deleteError = r.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    //删除文件
    @Test
    public void testDeleteFile() throws Exception {
        try {
            //RemoveObjectArgs
            RemoveObjectArgs objectArgs = RemoveObjectArgs.builder().bucket("testbucket").object("merge01.mp4").build();
            //删除文件
            minioClient.removeObject(objectArgs);
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除文件失败");
        }
    }


}
