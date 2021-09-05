package it.sauronsoftware.jave;

import it.sauronsoftware.jave.audio.AudioAttributes;
import it.sauronsoftware.jave.audio.AudioInfo;
import it.sauronsoftware.jave.audio.AudioUtils;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

/**
 * jave 音频转换测试
 */
public class AudioTest {


    @Test
    public void amrToMp3() {
        File source = new File("target/test-classes/material/longAudio.mp3");
        File mp3Target = new File("target/test-classes/material/testAudio12.wav");
        AudioUtils.amrToMp3(source, mp3Target);
    }

    @Test
    public void getInfo() {
        File mp3Target = new File("/Users/qaazz/Documents/testAudio14.wav");
        Encoder encoder = new Encoder();
        try {
            MultimediaInfo info = encoder.getInfo(mp3Target);
            AudioInfo audio = info.getAudio();
            int samplingRate = audio.getSamplingRate();
            System.out.println(samplingRate);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void cutMp3() {
        File source = new File("target/test-classes/material/longAudio.mp3");
        File mp3Target = new File("target/test-classes/material/testAudio14.mp3");
        AudioUtils.cutAndConvert(source, mp3Target, "mp3", "00:00:08", "00:00:10");
    }

    @Test
    public void mp3ToWav() {
        File source = new File("target/test-classes/material/longAudio.mp3");
        File wavTarget = new File("testAudio.wav");
        AudioUtils.amrToWav(source, wavTarget);
    }


    @Test
    public void mergeAudio() {
        File source1 = new File("target/test-classes/material/sunwukong.mp3");
        File source2 = new File("target/test-classes/material/luban.mp3");
        File source3 = new File("target/test-classes/material/lvbu.wav");
        File source4 = new File("target/test-classes/material/diaochan.mp3");
        File source5 = new File("target/test-classes/material/direnjie.mp3");
        File targetSource = new File("target/test-classes/material/wangzherongyao.wav");
        AudioUtils.mergeAudio(Arrays.asList(source1, source2, source3, source4, source5), targetSource, null);
    }

    @Test
    public void audioAtempo() {
        //倍速
        File source = new File("target/test-classes/material/longAudio.mp3");
        File target = new File("target/test-classes/material/longAudio-atempo.mp3");
        AudioUtils.audioAtempo(source, target, 2.0);
    }


    @Test
    public void audioVol() {
        //增加音量
        File source = new File("target/test-classes/material/longAudio.mp3");
        File target = new File("target/test-classes/material/longAudio-vol.mp3");

        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setVol(1000);

        EncodingAttributes encodingAttributes = new EncodingAttributes();
        encodingAttributes.setAudioAttributes(audioAttributes);

        AudioUtils.operate(source, target, encodingAttributes);
    }

    @Test
    public void audioAfVolume() {
        //有损 - 增加音量
        File source = new File("target/test-classes/material/longAudio.mp3");
        File target = new File("target/test-classes/material/longAudio-volume.mp3");

        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setAf_volume("2");

        EncodingAttributes encodingAttributes = new EncodingAttributes();
        encodingAttributes.setAudioAttributes(audioAttributes);

        AudioUtils.operate(source, target, encodingAttributes);
    }

    @Test
    public void audioAfVolume2() {
        //无损 - 增加音量
        File source = new File("target/test-classes/material/longAudio.mp3");
        File target = new File("target/test-classes/material/longAudio-volume-db.mp3");

        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setAf_volume("5dB");

        EncodingAttributes encodingAttributes = new EncodingAttributes();
        encodingAttributes.setAudioAttributes(audioAttributes);

        AudioUtils.operate(source, target, encodingAttributes);
    }


}

