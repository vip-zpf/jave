package it.sauronsoftware.jave;

import it.sauronsoftware.jave.video.VideoUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * 视频转换测试
 */
public class VideoTest {

    @Test
    public void thumbnail() {
        File source = new File("target/test-classes/material/testVideo.avi");
        File target = new File("target/test-classes/material/image/image-%3d.jpeg");
        VideoUtils.thumbnail(source, target, null);
    }

    @Test
    public void getVideo() throws EncoderException {
        File source = new File("target/test-classes/material/testVideo.avi");
        File target = new File("target/test-classes/material/target.wav");
        VideoUtils.getVoideoAudio(source, target, null);
    }

    @Test
    public void mergeVideo() throws EncoderException, IOException {
        File source1 = new File("target/test-classes/material/face.mp4");
        File source2 = new File("target/test-classes/material/girl.mp4");
        File source3 = new File("target/test-classes/material/man.mp4");
        File target = new File("target/test-classes/material/aaa.mp4");
        String data = new StringBuffer().
                append("file '").append(source1.getAbsolutePath()).append("'").append(System.getProperty("line.separator")).
                append("file '").append(source2.getAbsolutePath()).append("'").append(System.getProperty("line.separator")).
                append("file '").append(source3.getAbsolutePath()).append("'").append(System.getProperty("line.separator")).
                toString();
        File mergeVideoTxt = new File("target/test-classes/material/", "mergeVideo.txt");
        FileUtils.writeStringToFile(mergeVideoTxt, data, "UTF-8", false);
        VideoUtils.mergeVoideo(mergeVideoTxt, target, "mp4");
    }


    @Test
    public void mergeAudioVideo() throws EncoderException, IOException {
        File source1 = new File("target/test-classes/material/aaa.mp4");
        File source2 = new File("target/test-classes/material/wangzherongyao.wav");
        File source3 = new File("target/test-classes/material/wangzherongyao.wav");
        File target = new File("target/test-classes/material/bbb.mp4");
        String data = new StringBuffer().
                append("file '").append(source1.getAbsolutePath()).append("'").append(System.getProperty("line.separator")).
                append("file '").append(source2.getAbsolutePath()).append("'").append(System.getProperty("line.separator")).
                append("file '").append(source3.getAbsolutePath()).append("'").append(System.getProperty("line.separator")).
                toString();
        File mergeVideoTxt = new File("target/test-classes/material/", "mergeVideo.txt");
        FileUtils.writeStringToFile(mergeVideoTxt, data, "UTF-8", false);
        VideoUtils.mergeVoideo(mergeVideoTxt, target, "mp4");
    }


}