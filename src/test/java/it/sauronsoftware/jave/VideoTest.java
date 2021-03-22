package it.sauronsoftware.jave;

import it.sauronsoftware.jave.audio.AudioAttributes;
import it.sauronsoftware.jave.video.VideoAttributes;
import it.sauronsoftware.jave.video.VideoUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * 视频转换测试
 */
public class VideoTest {

    @Test
    public void thumbnail() {
        File source = new File("target/test-classes/material/man.mp4");
        File target = new File("target/test-classes/material/image/image-%3d.jpeg");
        VideoUtils.thumbnailByOneFramePerSecond(source, target);
    }

    @Test
    public void thumbnail2() {
        File source = new File("target/test-classes/material/man.mp4");
        File target = new File("target/test-classes/material/image/image-%3d.jpeg");
        //每隔5秒抽1帧图片
        VideoUtils.thumbnailByOneFrameEveryFiveSeconds(source, target);
    }

    @Test
    public void thumbnail3() {
        File source = new File("target/test-classes/material/man.mp4");
        File target = new File("target/test-classes/material/image/image-%3d.jpeg");
        //每隔5秒抽1帧图片,从视频的第10秒开始
        String startTime = "00:00:10";
        VideoUtils.thumbnailByOneFrameEveryFiveSecondsAndStartTime(source, target, startTime, "5");
    }

    @Test
    public void getVideo() throws EncoderException {
        File source = new File("target/test-classes/material/girl.mp4");
        File target = new File("target/test-classes/material/target.wav");
        VideoUtils.getVoideoAudio(source, target, null);
    }

    //无损合并
    @Test
    public void mergeVideo() throws EncoderException, IOException {
        File source1 = new File("target/test-classes/material/girl.mp4");
        File source2 = new File("target/test-classes/material/man.mp4");
        File source3 = new File("target/test-classes/material/face.mp4");
        File target = new File("target/test-classes/material/aaa.mp4");

        String data = new StringBuffer().
                append("file '").append(source1.getAbsolutePath()).append("'").append(System.getProperty("line.separator")).
                append("file '").append(source2.getAbsolutePath()).append("'").append(System.getProperty("line.separator")).
                append("file '").append(source3.getAbsolutePath()).append("'").append(System.getProperty("line.separator")).
                toString();
        File mergeVideoTxt = new File("target/test-classes/material/", "mergeVideo.txt");
        FileUtils.writeStringToFile(mergeVideoTxt, data, "UTF-8", false);
        VideoUtils.mergeVideoByLossless(mergeVideoTxt, target, "mp4");
    }

    //有损合并视频
    @Test
    public void mergeAudioVideo() throws EncoderException, IOException {
        File source1 = new File("target/test-classes/material/girl.mp4");
        File source2 = new File("target/test-classes/material/man.mp4");
        File source3 = new File("target/test-classes/material/girl.mp4");
        File target = new File("target/test-classes/material/bbb.mkv");

        LinkedList<File> files = new LinkedList<>();
        files.add(source1);
        files.add(source2);
        files.add(source3);
        VideoUtils.mergeVideoByDamaging(files, target, "mp4");
    }


    //视频中插入音频（视频原本无音频）
    @Test
    public void mergeVideoAndVideoByInsert() throws EncoderException, IOException {
        File source1 = new File("target/test-classes/material/girl.mp4");
        File source2 = new File("target/test-classes/material/wangzherongyao.wav");
        File target = new File("target/test-classes/material/videoAndAudio.mp4");

        LinkedList<File> files = new LinkedList<>();
        files.add(source1);
        files.add(source2);
        VideoUtils.mergeVoideoAndAudioByInsert(files, target, null);
    }

    //替换视频中的音频
    @Test
    public void mergeVideoAndVideoByRe() throws EncoderException, IOException {
        File source1 = new File("target/test-classes/material/girl.mp4");
        File source2 = new File("target/test-classes/material/wangzherongyao.wav");
        File target = new File("target/test-classes/material/videoAndAudio2.mp4");

        LinkedList<File> files = new LinkedList<>();
        files.add(source1);
        files.add(source2);
        VideoUtils.mergeVoideoAndAudioByReplace(files, target, "mp4");
    }

    //旋转视频
    @Test
    public void roateVideo() {
        File source = new File("target/test-classes/material/girl.mp4");
        File target = new File("target/test-classes/material/girlRoate.mp4");
        VideoUtils.roateVideo(source, target, "transpose=1");
    }


    @Test
    public void webm2MP4One() {
        File source = new File("target/test-classes/material/abc.webm");
        File target = new File("target/test-classes/material/webm2MP4.mp4");
        VideoUtils.webm2mp4(source, target, "2000k", "2000k", "2500k");
    }

    @Test
    public void webm2MP4Two() {
        File source = new File("target/test-classes/material/abc.webm");
        File target = new File("target/test-classes/material/webm2MP4.mp4");
        VideoUtils.webm2mp4(source, target, null, null);
    }

    @Test
    public void fomart() {
        File source = new File("target/test-classes/material/123.mp4");
        File target = new File("target/test-classes/material/123.avi");
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("avi");
        attrs.setVideoAttributes(new VideoAttributes());
        attrs.setAudioAttributes(new AudioAttributes());
        VideoUtils.getVoideoAudio(source, target, attrs);
    }
}