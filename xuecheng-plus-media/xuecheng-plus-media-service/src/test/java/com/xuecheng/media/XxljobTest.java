package com.xuecheng.media;

import com.xuecheng.base.utils.Mp4VideoUtil;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author hyb
 * @version 1.0
 * @description TODO
 * @date 2024/12/3
 */
public class XxljobTest {

    @Test
    public void ffmpegStartTest(){
//        String outstring = null;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("D:\\Program Files (x86)\\NetEase\\MailMaster\\Application\\mailmaster.exe");
//            "D:\Program Files (x86)\NetEase\MailMaster\Application\mailmaster.exe"
            //将标准输入流和错误输入流合并，通过标准输入流程读取信息
            builder.redirectErrorStream(true);
            Process p = builder.start();
//            outstring = waitFor(p);

        } catch (Exception ex) {

            ex.printStackTrace();

        }
    }

    @Test
    public void ffmpegTest(){
        //ffmpeg的路径
        String ffmpeg_path = "F:\\ffmpeg\\ffmpeg.exe";//ffmpeg的安装位置
        //源avi视频的路径
        String video_path = "F:\\Java learning\\video\\oc0ll-460t1.avi";
        //转换后mp4文件的名称
        String mp4_name = "Waves_Slow.mp4";
        //转换后mp4文件的路径
        String mp4_path = "F:\\Java learning\\video\\Waves_Slow1.mp4";
        //创建工具类对象
        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4_path);
        //开始视频转换，成功将返回success
        String s = videoUtil.generateMp4();
        System.out.println("视频转换成功");

    }

    @Test
    public void TestFFmpeg() {
        try {
            ProcessBuilder pb = new ProcessBuilder("F:/ffmpeg/ffmpeg.exe");
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = p.waitFor();
            System.out.println("FFmpeg exited with code " + exitCode);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: IOException - Possible incorrect path or file not accessible.");
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Error: InterruptedException - Process was interrupted.");
        }

    }
}
