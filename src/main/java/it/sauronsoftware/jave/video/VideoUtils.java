package it.sauronsoftware.jave.video;

import it.sauronsoftware.jave.*;
import it.sauronsoftware.jave.audio.AudioAttributes;
import it.sauronsoftware.jave.enumers.VideoMergeTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.LinkedList;

/**
 * 视频相关工具
 */
public class VideoUtils {


    /**
     * 获取视频缩略图，每秒抽一帧
     *
     * @param source          视频来源
     * @param imageTargetPath 缩略图存放目标文件
     */
    public static void thumbnailByOneFramePerSecond(File source, File imageTargetPath) {
        thumbnail(source, imageTargetPath, 1, null, null, null,"2");
    }

    /**
     * 获取视频缩略图，每5秒抽一帧
     *
     * @param source          视频来源
     * @param imageTargetPath 缩略图存放目标文件
     */
    public static void thumbnailByOneFrameEveryFiveSeconds(File source, File imageTargetPath) {
        thumbnail(source, imageTargetPath, 1, 5d, null, null,null);
    }

    /**
     * 获取视频缩略图，每5秒抽一帧,从startTime开始抽，持续duration秒
     *
     * @param source          视频来源
     * @param imageTargetPath 缩略图存放目标文件
     */
    public static void thumbnailByOneFrameEveryFiveSecondsAndStartTime(File source, File imageTargetPath, String startTime, String duration) {
        thumbnail(source, imageTargetPath, 1, 5d, startTime, duration,null);
    }

    /**
     * 获取视频缩略图
     *
     * @param source          视频来源
     * @param imageTargetPath 缩略图存放目标文件
     * @param frameRate       每秒抽几帧
     * @param intervalTime    间隔时间（每隔多少秒抽取几帧）
     * @param startTime       开始时间（从什么时间开始操作）
     * @param duration        持续时长
     * @param qv               设置图片质量
     */
    public static void thumbnail(File source, File imageTargetPath, Integer frameRate, Double intervalTime, String startTime, String duration, String qv) {
        Encoder encoder = new IgnoreErrorEncoder();
        VideoAttributes video = new VideoAttributes();
        if (frameRate != null && intervalTime == null) {
            video.setFrameRate(frameRate);
        }
        if (frameRate != null && intervalTime != null) {
            String fps = "fps=" + frameRate + "/" + intervalTime;
            video.setVf(fps);
        }
        if (startTime != null) {
            video.setStartTime(startTime);
        }
        if (duration != null) {
            video.setDuration(duration);
        }
        if (qv != null && qv.length()>0){
            video.setQv(qv);
        }
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
     * @param source 输入文件
     * @param target 输出文件
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
     * 无损 合并多个视频
     * 注意：
     * 1、如果第一个视频没有声音，那么合并后的视频也是没有声音的
     * 2、必须保证所有视频的格式，分辨率都一样，不然结果不可控
     *
     * @param source 输入文件
     * @param target 输出文件
     * @param format 格式
     */
    public static void mergeVideoByLossless(File source, File target, String format) {
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
            encoder.encodeMergeVideoByLossless(source, target, attrs);
        } catch (Exception e) {
            throw new IllegalStateException("error: ", e);
        }
    }

    /**
     * 有损 合并多个视频
     * 注意：
     * 1、输出格式为mkv
     *
     * @param source 输入文件
     * @param target 输出文件
     * @param format 格式
     */
    public static void mergeVideoByDamaging(LinkedList<File> source, File target, String format) {
        Encoder encoder = new IgnoreErrorEncoder();
        EncodingAttributes attrs = new EncodingAttributes();
        if (StringUtils.isNoneEmpty(format)) {
            attrs.setFormat(format);
        } else {
            attrs.setFormat(format);
        }
        VideoAttributes video = new VideoAttributes();
        video.setCodec("h264");

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");

        attrs.setVideoAttributes(video);
        attrs.setAudioAttributes(audio);
        try {
            encoder.encodeMergeVideoByDamaging(source, target, attrs);
        } catch (Exception e) {
            throw new IllegalStateException("error: ", e);
        }
    }


    //合并 视频音频 插入音频的方式
    public static void mergeVoideoAndAudioByInsert(LinkedList<File> source, File target, String format) {
        if (source == null || source.size()!=2) {
            throw new RuntimeException("请传入要合并的文件");
        }
        Encoder encoder = new IgnoreErrorEncoder();
        EncodingAttributes attrs = new EncodingAttributes();
        if (StringUtils.isNoneEmpty(format)) {
            attrs.setFormat(format);
        } else {
            attrs.setFormat(format);
        }
        VideoAttributes video = new VideoAttributes();
        video.setMergeType(VideoMergeTypeEnum.INSERT);
        video.setCodec(VideoAttributes.DIRECT_STREAM_COPY);

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("aac");

        attrs.setVideoAttributes(video);
        attrs.setAudioAttributes(audio);
        try {
            encoder.encodeMergeVideoAndAudio(source, target, attrs);
        } catch (Exception e) {
            throw new IllegalStateException("error: ", e);
        }
    }

    //合并 视频音频 替换音频的方式
    public static void mergeVoideoAndAudioByReplace(LinkedList<File> source, File target, String format) {
        if (source == null || source.size()!=2) {
            throw new RuntimeException("请传入要合并的文件");
        }
        Encoder encoder = new IgnoreErrorEncoder();
        EncodingAttributes attrs = new EncodingAttributes();
        if (StringUtils.isNoneEmpty(format)) {
            attrs.setFormat(format);
        } else {
            attrs.setFormat(format);
        }
        VideoAttributes video = new VideoAttributes();
        video.setMergeType(VideoMergeTypeEnum.REPLACE);
        video.setCodec(VideoAttributes.DIRECT_STREAM_COPY);

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("aac");

        attrs.setVideoAttributes(video);
        attrs.setAudioAttributes(audio);
        try {
            encoder.encodeMergeVideoAndAudio(source, target, attrs);
        } catch (Exception e) {
            throw new IllegalStateException("error: ", e);
        }
    }

    //旋转视频
    public static void roateVideo(File source, File target, String vf) {
        Encoder encoder = new IgnoreErrorEncoder();
        VideoAttributes video = new VideoAttributes();
        if (vf != null && vf.length()>0){
            video.setVf(vf);
        }
        AudioAttributes audio = new AudioAttributes();
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setVideoAttributes(video);
        attrs.setAudioAttributes(audio);

        try {
            encoder.encode(source, target, attrs);
        } catch (Exception e) {
            throw new IllegalStateException("error: ", e);
        }
    }

    public static MultimediaInfo getVideoInfo(File source) {
        Encoder encoder = new IgnoreErrorEncoder();
        MultimediaInfo info = null;
        try {
            info = encoder.getInfo(source);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        return info;
    }
}