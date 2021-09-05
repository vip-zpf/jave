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
package it.sauronsoftware.jave.audio;

import it.sauronsoftware.jave.enumers.AudioMergeTypeEnum;
import lombok.Data;

@Data
public class AudioAttributes {

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
     * The bitrate value for the encoding process. If null or not specified a
     * default value will be picked.
     */
    private Integer bitRate = null;

    /**
     * The samplingRate value for the encoding process. If null or not specified
     * a default value will be picked.
     */
    private Integer samplingRate = null;

    /**
     * The channels value (1=mono, 2=stereo) for the encoding process. If null
     * or not specified a default value will be picked.
     */
    private Integer channels = null;

    /**
     * The volume value for the encoding process. If null or not specified a
     * default value will be picked. If 256 no volume change will be performed.
     * 不通过滤镜调整音量：ffmpeg -i input.mp3  -vol 2000  -y  output.mp3
     */
    private Integer vol = null;

    /**
     * 开始剪辑时间 格式：00:00:00
     */
    private String startTime;
    /**
     * 截取时长
     */
    private String duration;

    /**
     * 合并类型
     */
    private AudioMergeTypeEnum mergeType;

    /**
     * filter_graph 过滤视图
     * set video filters 用来设置音频过滤器 简称：af
     * 音量平滑处理 af = loudnorm
     */
    private String af;

    /**
     * 加快音频播放速度：数字越大播放速度越快
     * atempo只支持0.5到2.0的数值，如果想要调整只四倍速，可以填写两组atempo（atempo=2.0,atempo=2.0）
     * ps:
     * atempo=0.5
     * atempo=atempo=2.0,atempo=2.0
     */
    private String af_Atempo;

    /**
     * 通过滤镜的方式处理音量
     * 无损-基于分贝数值的处理（提升5分贝，降低分贝使用负号）：ffmpeg -i input.mp3  -af volume=5dB  -y  output.mp3
     * 有损音-暴力扩大2倍：ffmpeg -i input.mp3  -af volume=2  -y  output.mp3
     */
    private String af_volume;

    /**
     * 音频比特率
     * 音频数据流量，一般选择32k、64k、96k、128k
     */
    private String ab;

}
