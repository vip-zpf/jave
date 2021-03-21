/*
 * JAVE - A Java Audio/Video Encoder (based on FFMPEG)
 *
 * Copyright (C) 2008-2009 Carlo Pelliccia (www.sauronsoftware.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.sauronsoftware.jave.video;

import it.sauronsoftware.jave.enumers.VideoMergeTypeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class VideoAttributes implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * This value can be setted in the codec field to perform a direct stream
     * copy, without re-encoding of the audio stream.
     */
    public static final String DIRECT_STREAM_COPY = "copy";

    /**
     * The codec name for the encoding process. If null or not specified the
     * encoder will perform a direct stream copy.
     */
    private String codec = null;

    /**
     * The the forced tag/fourcc value for the video stream.
     */
    private String tag = null;

    /**
     * The bitrate value for the encoding process. If null or not specified a
     * default value will be picked.
     */
    private Integer bitRate = null;

    /**
     * The frame rate value for the encoding process. If null or not specified a
     * default value will be picked.
     */
    private Integer frameRate = null;

    /**
     * The video size for the encoding process. If null or not specified the
     * source video size will not be modified.
     */
    private VideoSize size = null;

    /**
     * 开始剪辑时间 格式：00:00:00
     */
    private String startTime;
    /**
     * 截取时长
     */
    private String duration;

    /**
     * filter_graph 过滤视图
     * set video filters 用来设置视频过滤器 简称：vf
     *
     * ps：fps=1/20 每隔20秒截取一张
     * ps: "transpose=1" 顺时针旋转画面90度
     * ps: "transpose=2" 逆时针旋转画面90度
     * ps: "transpose=3" 顺时针旋转画面90度再水平翻转
     * ps: "transpose=0" 逆时针旋转画面90度再水平翻转
     * ps: hflip 水平翻转视频画面
     * ps: vflip 垂直翻转视频画面
     */
    private String vf;

    /**
     * 指定视频截取质量 ps：2
     * to control output quality. Full range is a linear scale of 1-31 where a lower value results in a higher quality. 2-5 is a good range to try.
     */
    private String qv;

    /**
     * 合并类型
     */
    private VideoMergeTypeEnum mergeType;

}
