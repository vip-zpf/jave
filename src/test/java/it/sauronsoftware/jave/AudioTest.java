package it.sauronsoftware.jave;

import it.sauronsoftware.jave.audio.AudioAttributes;
import it.sauronsoftware.jave.audio.AudioInfo;
import it.sauronsoftware.jave.audio.AudioUtils;
import it.sauronsoftware.jave.audio.VolumedetectInfo;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

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
        File mp3Target = new File("target/test-classes/material/longAudio.mp3");
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


    @Test
    public void getVolumedetect() {
        //获取音频分贝
        File source = new File("target/test-classes/material/diaochan.mp3");
        File target = new File("/dev/null");

        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setFilterComplex("volumedetect");

        Encoder encoder = new Encoder();
        MultimediaInfo multimediaInfo = null;
        try {
            multimediaInfo = encoder.getInfo(source, target, null, audioAttributes, "null");
        } catch (EncoderException e) {
            throw new RuntimeException(e);
        }
        AudioInfo audio = multimediaInfo.getAudio();
        VolumedetectInfo volumedetect = audio.getVolumedetect();

        //平均分贝
        String meanVolume = volumedetect.getMeanVolume();
        //最大分贝
        String maxVolume = volumedetect.getMaxVolume();
        //音频分贝分布情况
        Map<String, String> histogramMap = volumedetect.getHistogramMap();
    }

    @Test
    public void getAudioImg() {
        //ffmpeg -i diaochan.mp3 -filter_complex "showwavespic=s=640x120" output.png
        //获取音频 音波图
        File source = new File("target/test-classes/material/diaochan.mp3");
        File target = new File("target/test-classes/material/diaochan.png");

        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setFilterComplex("showwavespic=s=640x120");

        EncodingAttributes encodingAttributes = new EncodingAttributes();
        encodingAttributes.setAudioAttributes(audioAttributes);

        Encoder encoder = new Encoder();
        AudioUtils.operate(source, target, encodingAttributes);
    }





}

