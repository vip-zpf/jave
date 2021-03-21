package it.sauronsoftware.jave.audio;

import it.sauronsoftware.jave.*;
import it.sauronsoftware.jave.enumers.AudioMergeTypeEnum;

import java.io.File;
import java.util.List;

/**
 * 音频转换工具
 */
public class AudioUtils {

    private static final String LIBMP_3_LAME = "libmp3lame";

    /**
     * amr转mp3
     *
     * @param sourcePath 音频来源目录
     * @param targetPath 目标存放地址
     */
    public static void amrToMp3(String sourcePath, String targetPath) {
        File source = new File(sourcePath);
        File target = new File(targetPath);
        amrToMp3(source, target);
    }

    /**
     * amr转mp3
     *
     * @param source 音频来源
     * @param target 目标存放地址
     */
    public static void amrToMp3(File source, File target) {
        convert(source, target, "mp3");
    }

    /**
     * s
     * amr转wav
     *
     * @param source 音频来源
     * @param target 目标存放地址
     */
    public static void cutAndonvertToWav(File source, File target, String startTime, String duration) {
        cutAndConvert(source, target, "wav", startTime, duration);
    }


    /**
     * s
     * amr转wav
     *
     * @param source 音频来源
     * @param target 目标存放地址
     */
    public static void amrToWav(File source, File target) {
        convert(source, target, "wav");
    }

    public static void convert(File source, File target, String format) {
        if (!source.exists()) {
            throw new IllegalArgumentException("source file does not exists: " + source.getAbsoluteFile());
        }
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(LIBMP_3_LAME);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat(format);
        attrs.setAudioAttributes(audio);

        Encoder encoder = new IgnoreErrorEncoder();
        try {
            encoder.encode(source, target, attrs);
        } catch (Exception e) {
            throw new IllegalStateException("convert amr to " + format + " error: ", e);
        }
    }

    public static void cutAndConvert(File source, File target, String format, String startTime, String duration) {
        if (!source.exists()) {
            throw new IllegalArgumentException("source file does not exists: " + source.getAbsoluteFile());
        }
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(LIBMP_3_LAME);
        audio.setStartTime(startTime);
        audio.setDuration(duration);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat(format);
        attrs.setAudioAttributes(audio);

        Encoder encoder = new IgnoreErrorEncoder();
        try {
            encoder.encode(source, target, attrs);
        } catch (Exception e) {
            throw new IllegalStateException("cutAndConvert" + format + " error: ", e);
        }
    }

    public static void operate(File source, File target, EncodingAttributes attrs) {
        if (!source.exists()) {
            throw new IllegalArgumentException("source file does not exists: " + source.getAbsoluteFile());
        }
        Encoder encoder = new IgnoreErrorEncoder();
        try {
            encoder.encode(source, target, attrs);
        } catch (Exception e) {
            throw new IllegalStateException("operate error: ", e);
        }
    }

    public static void mergeAudio(List<File> sourceList, File target, EncodingAttributes attrs) {
        if (attrs == null) {
            defultMergeAudio(sourceList, target);
            return;
        }
        Encoder encoder = new IgnoreErrorEncoder();
        try {
            encoder.encodeMergeAudio(sourceList, target, attrs);
        } catch (EncoderException e) {
            throw new IllegalStateException("operate error: ", e);
        }
    }

    public static void defultMergeAudio(List<File> sourceList, File target) {
        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setMergeType(AudioMergeTypeEnum.SPLIT_JOINT);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("wav");
        attrs.setAudioAttributes(audioAttributes);

        Encoder encoder = new IgnoreErrorEncoder();
        try {
            encoder.encodeMergeAudio(sourceList, target, attrs);
        } catch (EncoderException e) {
            throw new IllegalStateException("operate error: ", e);
        }
    }

    public static MultimediaInfo getAudioInfo(File source) {
        Encoder encoder = new IgnoreErrorEncoder();
        MultimediaInfo info = null;
        try {
            info = encoder.getInfo(source);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * 音频倍速播放
     * @param source
     * @param target
     * @param atempo
     */
    public static void audioAtempo(File source, File target, Double atempo) {
        AudioAttributes audioAttributes = new AudioAttributes();
        String af = "atempo=" + atempo;
        audioAttributes.setAf(af);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setAudioAttributes(audioAttributes);

        Encoder encoder = new IgnoreErrorEncoder();
        try {
            encoder.encode(source, target, attrs);
        } catch (EncoderException e) {
            throw new IllegalStateException("operate error: ", e);
        }
    }
}
