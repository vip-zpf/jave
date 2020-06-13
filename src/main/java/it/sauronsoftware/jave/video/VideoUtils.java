package it.sauronsoftware.jave.video;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import it.sauronsoftware.jave.IgnoreErrorEncoder;
import it.sauronsoftware.jave.audio.AudioAttributes;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * 视频相关工具
 */
public class VideoUtils {

    /**
     * 获取视频缩略图
     *
     * @param source
     * @param imageTargetPath
     * @param attrs
     */
    public static void thumbnail(File source, File imageTargetPath, EncodingAttributes attrs) {
        if (attrs == null) {
            thumbnail(source, imageTargetPath);
            return;
        }
        Encoder encoder = new IgnoreErrorEncoder();
        try {
            encoder.encode(source, imageTargetPath, attrs);
        } catch (Exception e) {
            throw new IllegalStateException("error: ", e);
        }
    }

    /**
     * 获取视频缩略图
     *
     * @param source          视频来源
     * @param imageTargetPath 缩略图存放目标文件
     */
    public static void thumbnail(File source, File imageTargetPath) {
        Encoder encoder = new IgnoreErrorEncoder();
        VideoAttributes video = new VideoAttributes();
        video.setFrameRate(1);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("image2");
        attrs.setVideoAttributes(video);
        try {
            encoder.encode(source, imageTargetPath, attrs);
        } catch (Exception e) {
            throw new IllegalStateException("error: ", e);
        }
    }

    /**
     * 抽取视频中的音频
     *
     * @param source 视频来源
     * @param target 目标文件
     * @param attrs  扩展操作
     */
    public static void getVoideoAudio(File source, File target, EncodingAttributes attrs) {
        if (attrs == null) {
            getVoideoAudioToWav(source, target);
            return;
        }
        Encoder encoder = new IgnoreErrorEncoder();
        try {
            encoder.encode(source, target, attrs);
        } catch (EncoderException e) {
            throw new IllegalStateException("error: ", e);
        }
    }

    /**
     * 抽取视频中的音频并转为wav格式
     *
     * @param source
     * @param target
     */
    public static void getVoideoAudioToWav(File source, File target) {
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("wav");
        attrs.setAudioAttributes(audio);
        attrs.setVideoAttributes(new VideoAttributes());
        Encoder encoder = new IgnoreErrorEncoder();
        try {
            encoder.encode(source, target, attrs);
        } catch (EncoderException e) {
            throw new IllegalStateException("error: ", e);
        }
    }


    /**
     * 合并多个视频
     *
     * @param source
     * @param target
     * @param format
     */
    public static void mergeVoideo(File source, File target, String format) {
        String fileName = source.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (StringUtils.isEmpty(suffix) || !"txt".equalsIgnoreCase(suffix)) {
            throw new RuntimeException("请将文件名按格式保存到txt文件中");
        }
        Encoder encoder = new IgnoreErrorEncoder();
        EncodingAttributes attrs = new EncodingAttributes();
        if (StringUtils.isNoneEmpty(format)) {
            attrs.setFormat(format);
        } else {
            attrs.setFormat(format);
        }
        VideoAttributes video = new VideoAttributes();
        video.setCodec(VideoAttributes.DIRECT_STREAM_COPY);

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(AudioAttributes.DIRECT_STREAM_COPY);

        attrs.setVideoAttributes(video);
        attrs.setAudioAttributes(audio);
        try {
            encoder.encodeMergeVideo(source, target, attrs);
        } catch (Exception e) {
            throw new IllegalStateException("error: ", e);
        }
    }
}