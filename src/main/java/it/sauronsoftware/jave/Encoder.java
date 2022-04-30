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
package it.sauronsoftware.jave;

import it.sauronsoftware.jave.audio.AudioAttributes;
import it.sauronsoftware.jave.audio.AudioInfo;
import it.sauronsoftware.jave.enumers.AudioMergeTypeEnum;
import it.sauronsoftware.jave.video.VideoAttributes;
import it.sauronsoftware.jave.video.VideoInfo;
import it.sauronsoftware.jave.video.VideoSize;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class of the package. Instances can encode audio and video streams.
 *
 * @author Carlo Pelliccia
 */
public class Encoder {

    /**
     * This regexp is used to parse the ffmpeg output about the supported
     * formats.
     */
    private static final Pattern FORMAT_PATTERN = Pattern.compile("^\\s*([D ])([E ])\\s+([\\w,]+)\\s+.+$");

    /**
     * This regexp is used to parse the ffmpeg output about the included
     * encoders/decoders.
     */
    private static final Pattern ENCODER_DECODER_PATTERN = Pattern.compile("^\\s*([D ])([E ])([AVS]).{3}\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    /**
     * This regexp is used to parse the ffmpeg output about the ongoing encoding
     * process.
     */
    private static final Pattern PROGRESS_INFO_PATTERN = Pattern.compile("\\s*(\\w+)\\s*=\\s*(\\S+)\\s*", Pattern.CASE_INSENSITIVE);

    /**
     * This regexp is used to parse the ffmpeg output about the size of a video
     * stream.
     */
    private static final Pattern SIZE_PATTERN = Pattern.compile("(\\d+)x(\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * This regexp is used to parse the ffmpeg output about the frame rate value
     * of a video stream.
     */
    private static final Pattern FRAME_RATE_PATTERN = Pattern.compile("([\\d.]+)\\s+(?:fps|tb\\(r\\))", Pattern.CASE_INSENSITIVE);

    /**
     * This regexp is used to parse the ffmpeg output about the bit rate value
     * of a stream.
     */
    private static final Pattern BIT_RATE_PATTERN = Pattern.compile("(\\d+)\\s+kb/s", Pattern.CASE_INSENSITIVE);

    /**
     * This regexp is used to parse the ffmpeg output about the sampling rate of
     * an audio stream.
     */
    private static final Pattern SAMPLING_RATE_PATTERN = Pattern.compile("(\\d+)\\s+Hz", Pattern.CASE_INSENSITIVE);

    /**
     * This regexp is used to parse the ffmpeg output about the channels number
     * of an audio stream.
     */
    private static final Pattern CHANNELS_PATTERN = Pattern.compile("(mono|stereo)", Pattern.CASE_INSENSITIVE);

    /**
     * This regexp is used to parse the ffmpeg output about the success of an
     * encoding operation.
     */
    private static final Pattern SUCCESS_PATTERN = Pattern.compile("^\\s*video\\:\\S+\\s+audio\\:\\S+\\s+global headers\\:\\S+.*$", Pattern.CASE_INSENSITIVE);

    /**
     * The locator of the ffmpeg executable used by this encoder.
     */
    private FFMPEGLocator locator;

    /**
     * It builds an encoder using a {@link DefaultFFMPEGLocator} instance to
     * locate the ffmpeg executable to use.
     */
    public Encoder() {
        this.locator = new DefaultFFMPEGLocator();
    }

    /**
     * It builds an encoder with a custom {@link FFMPEGLocator}.
     *
     * @param locator The locator picking up the ffmpeg executable used by the
     *                encoder.
     */
    public Encoder(FFMPEGLocator locator) {
        this.locator = locator;
    }

    /**
     * Returns a list with the names of all the audio decoders bundled with the
     * ffmpeg distribution in use. An audio stream can be decoded only if a
     * decoder for its format is available.
     *
     * @return A list with the names of all the included audio decoders.
     * @throws EncoderException If a problem occurs calling the underlying ffmpeg executable.
     */
    public String[] getAudioDecoders() throws EncoderException {
        ArrayList res = new ArrayList();
        FFMPEGExecutor ffmpeg = locator.createExecutor();
        ffmpeg.addArgument("-formats");
        try {
            ffmpeg.execute();
            RBufferedReader reader = null;
            reader = new RBufferedReader(new InputStreamReader(ffmpeg.getInputStream()));
            String line;
            boolean evaluate = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                if (evaluate) {
                    Matcher matcher = ENCODER_DECODER_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String decoderFlag = matcher.group(1);
                        String audioVideoFlag = matcher.group(3);
                        if ("D".equals(decoderFlag) && "A".equals(audioVideoFlag)) {
                            String name = matcher.group(4);
                            res.add(name);
                        }
                    } else {
                        break;
                    }
                } else if (line.trim().equals("Codecs:")) {
                    evaluate = true;
                }
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            ffmpeg.destroy();
        }
        int size = res.size();
        String[] ret = new String[size];
        for (int i = 0; i < size; i++) {
            ret[i] = (String) res.get(i);
        }
        return ret;
    }

    /**
     * Returns a list with the names of all the audio encoders bundled with the
     * ffmpeg distribution in use. An audio stream can be encoded using one of
     * these encoders.
     *
     * @return A list with the names of all the included audio encoders.
     * @throws EncoderException If a problem occurs calling the underlying ffmpeg executable.
     */
    public String[] getAudioEncoders() throws EncoderException {
        ArrayList res = new ArrayList();
        FFMPEGExecutor ffmpeg = locator.createExecutor();
        ffmpeg.addArgument("-formats");
        try {
            ffmpeg.execute();
            RBufferedReader reader = null;
            reader = new RBufferedReader(new InputStreamReader(ffmpeg.getInputStream()));
            String line;
            boolean evaluate = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                if (evaluate) {
                    Matcher matcher = ENCODER_DECODER_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String encoderFlag = matcher.group(2);
                        String audioVideoFlag = matcher.group(3);
                        if ("E".equals(encoderFlag) && "A".equals(audioVideoFlag)) {
                            String name = matcher.group(4);
                            res.add(name);
                        }
                    } else {
                        break;
                    }
                } else if (line.trim().equals("Codecs:")) {
                    evaluate = true;
                }
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            ffmpeg.destroy();
        }
        int size = res.size();
        String[] ret = new String[size];
        for (int i = 0; i < size; i++) {
            ret[i] = (String) res.get(i);
        }
        return ret;
    }

    /**
     * Returns a list with the names of all the video decoders bundled with the
     * ffmpeg distribution in use. A video stream can be decoded only if a
     * decoder for its format is available.
     *
     * @return A list with the names of all the included video decoders.
     * @throws EncoderException If a problem occurs calling the underlying ffmpeg executable.
     */
    public String[] getVideoDecoders() throws EncoderException {
        ArrayList res = new ArrayList();
        FFMPEGExecutor ffmpeg = locator.createExecutor();
        ffmpeg.addArgument("-formats");
        try {
            ffmpeg.execute();
            RBufferedReader reader = null;
            reader = new RBufferedReader(new InputStreamReader(ffmpeg.getInputStream()));
            String line;
            boolean evaluate = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                if (evaluate) {
                    Matcher matcher = ENCODER_DECODER_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String decoderFlag = matcher.group(1);
                        String audioVideoFlag = matcher.group(3);
                        if ("D".equals(decoderFlag) && "V".equals(audioVideoFlag)) {
                            String name = matcher.group(4);
                            res.add(name);
                        }
                    } else {
                        break;
                    }
                } else if (line.trim().equals("Codecs:")) {
                    evaluate = true;
                }
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            ffmpeg.destroy();
        }
        int size = res.size();
        String[] ret = new String[size];
        for (int i = 0; i < size; i++) {
            ret[i] = (String) res.get(i);
        }
        return ret;
    }

    /**
     * Returns a list with the names of all the video encoders bundled with the
     * ffmpeg distribution in use. A video stream can be encoded using one of
     * these encoders.
     *
     * @return A list with the names of all the included video encoders.
     * @throws EncoderException If a problem occurs calling the underlying ffmpeg executable.
     */
    public String[] getVideoEncoders() throws EncoderException {
        ArrayList res = new ArrayList();
        FFMPEGExecutor ffmpeg = locator.createExecutor();
        ffmpeg.addArgument("-formats");
        try {
            ffmpeg.execute();
            RBufferedReader reader = null;
            reader = new RBufferedReader(new InputStreamReader(ffmpeg.getInputStream()));
            String line;
            boolean evaluate = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                if (evaluate) {
                    Matcher matcher = ENCODER_DECODER_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String encoderFlag = matcher.group(2);
                        String audioVideoFlag = matcher.group(3);
                        if ("E".equals(encoderFlag) && "V".equals(audioVideoFlag)) {
                            String name = matcher.group(4);
                            res.add(name);
                        }
                    } else {
                        break;
                    }
                } else if (line.trim().equals("Codecs:")) {
                    evaluate = true;
                }
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            ffmpeg.destroy();
        }
        int size = res.size();
        String[] ret = new String[size];
        for (int i = 0; i < size; i++) {
            ret[i] = (String) res.get(i);
        }
        return ret;
    }

    /**
     * Returns a list with the names of all the file formats supported at
     * encoding time by the underlying ffmpeg distribution. A multimedia file
     * could be encoded and generated only if the specified format is in this
     * list.
     *
     * @return A list with the names of all the supported file formats at
     * encoding time.
     * @throws EncoderException If a problem occurs calling the underlying ffmpeg executable.
     */
    public String[] getSupportedEncodingFormats() throws EncoderException {
        ArrayList res = new ArrayList();
        FFMPEGExecutor ffmpeg = locator.createExecutor();
        ffmpeg.addArgument("-formats");
        try {
            ffmpeg.execute();
            RBufferedReader reader = null;
            reader = new RBufferedReader(new InputStreamReader(ffmpeg.getInputStream()));
            String line;
            boolean evaluate = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                if (evaluate) {
                    Matcher matcher = FORMAT_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String encoderFlag = matcher.group(2);
                        if ("E".equals(encoderFlag)) {
                            String aux = matcher.group(3);
                            StringTokenizer st = new StringTokenizer(aux, ",");
                            while (st.hasMoreTokens()) {
                                String token = st.nextToken().trim();
                                if (!res.contains(token)) {
                                    res.add(token);
                                }
                            }
                        }
                    } else {
                        break;
                    }
                } else if (line.trim().equals("File formats:")) {
                    evaluate = true;
                }
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            ffmpeg.destroy();
        }
        int size = res.size();
        String[] ret = new String[size];
        for (int i = 0; i < size; i++) {
            ret[i] = (String) res.get(i);
        }
        return ret;
    }

    /**
     * Returns a list with the names of all the file formats supported at
     * decoding time by the underlying ffmpeg distribution. A multimedia file
     * could be open and decoded only if its format is in this list.
     *
     * @return A list with the names of all the supported file formats at
     * decoding time.
     * @throws EncoderException If a problem occurs calling the underlying ffmpeg executable.
     */
    public String[] getSupportedDecodingFormats() throws EncoderException {
        ArrayList res = new ArrayList();
        FFMPEGExecutor ffmpeg = locator.createExecutor();
        ffmpeg.addArgument("-formats");
        try {
            ffmpeg.execute();
            RBufferedReader reader = null;
            reader = new RBufferedReader(new InputStreamReader(ffmpeg.getInputStream()));
            String line;
            boolean evaluate = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                if (evaluate) {
                    Matcher matcher = FORMAT_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String decoderFlag = matcher.group(1);
                        if ("D".equals(decoderFlag)) {
                            String aux = matcher.group(3);
                            StringTokenizer st = new StringTokenizer(aux, ",");
                            while (st.hasMoreTokens()) {
                                String token = st.nextToken().trim();
                                if (!res.contains(token)) {
                                    res.add(token);
                                }
                            }
                        }
                    } else {
                        break;
                    }
                } else if (line.trim().equals("File formats:")) {
                    evaluate = true;
                }
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            ffmpeg.destroy();
        }
        int size = res.size();
        String[] ret = new String[size];
        for (int i = 0; i < size; i++) {
            ret[i] = (String) res.get(i);
        }
        return ret;
    }

    /**
     * Returns a set informations about a multimedia file, if its format is
     * supported for decoding.
     *
     * @param source The source multimedia file.
     * @return A set of informations about the file and its contents.
     * @throws InputFormatException If the format of the source file cannot be recognized and
     *                              decoded.
     * @throws EncoderException     If a problem occurs calling the underlying ffmpeg executable.
     */
    public MultimediaInfo getInfo(File source) throws InputFormatException, EncoderException {
        FFMPEGExecutor ffmpeg = locator.createExecutor();
        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(source.getAbsolutePath());
        try {
            ffmpeg.execute();
        } catch (IOException e) {
            throw new EncoderException(e);
        }
        try {
            RBufferedReader reader = null;
            reader = new RBufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            return parseMultimediaInfo(source, reader);
        } finally {
            ffmpeg.destroy();
        }
    }

    /**
     * Private utility. It parses the ffmpeg output, extracting informations
     * about a source multimedia file.
     *
     * @param source The source multimedia file.
     * @param reader The ffmpeg output channel.
     * @return A set of informations about the source multimedia file and its
     * contents.
     * @throws InputFormatException If the format of the source file cannot be recognized and
     *                              decoded.
     * @throws EncoderException     If a problem occurs calling the underlying ffmpeg executable.
     */
    private MultimediaInfo parseMultimediaInfo(File source, RBufferedReader reader) throws InputFormatException, EncoderException {
        Pattern p1 = Pattern.compile("^\\s*Input #0, (\\w+).+$\\s*", Pattern.CASE_INSENSITIVE);
        Pattern p2 = Pattern.compile("^\\s*Duration: (\\d\\d):(\\d\\d):(\\d\\d)\\.(\\d).*$", Pattern.CASE_INSENSITIVE);
        Pattern p3 = Pattern.compile("^\\s*Stream #\\S+: ((?:Audio)|(?:Video)|(?:Data)): (.*)\\s*$", Pattern.CASE_INSENSITIVE);
        MultimediaInfo info = null;
        boolean videoInfo = false;
        boolean audioInfo = false;
        int creationflag = 0;
        LinkedList<String> creations = new LinkedList<String>();
        VideoInfo video = new VideoInfo();
        AudioInfo audio = new AudioInfo();
        try {
            int step = 0;
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (step == 0) {
                    String token = source.getAbsolutePath() + ": ";
                    if (line.startsWith(token)) {
                        String message = line.substring(token.length());
                        throw new InputFormatException(message);
                    }
                    Matcher m = p1.matcher(line);
                    boolean flag = false;
                    if (m.matches()) {
                        String format = m.group(1);
                        info = new MultimediaInfo();
                        info.setFormat(format);
                        step++;
                        creations.add("metadata@");
                    }
                }
                if (step == 1) {
                    Matcher m = p2.matcher(line);
                    if (m.matches()) {
                        long hours = Integer.parseInt(m.group(1));
                        long minutes = Integer.parseInt(m.group(2));
                        long seconds = Integer.parseInt(m.group(3));
                        long dec = Integer.parseInt(m.group(4));
                        long duration = (dec * 100L) + (seconds * 1000L) + (minutes * 60L * 1000L) + (hours * 60L * 60L * 1000L);
                        info.setDuration(duration);
                        step++;
                    }
                }
                if (step == 2) {
                    Matcher m = p3.matcher(line);
                    if (m.matches()) {
                        String type = m.group(1);
                        String specs = m.group(2);
                        if ("Video".equalsIgnoreCase(type)) {
                            StringTokenizer st = new StringTokenizer(specs, ",");
                            for (int i = 0; st.hasMoreTokens(); i++) {
                                String token = st.nextToken().trim();
                                if (i == 0) {
                                    video.setDecoder(token);
                                } else {
                                    boolean parsed = false;
                                    // Video size.
                                    Matcher m2 = SIZE_PATTERN.matcher(token);
                                    if (!parsed && m2.find()) {
                                        int width = Integer.parseInt(m2.group(1));
                                        int height = Integer.parseInt(m2.group(2));
                                        video.setSize(new VideoSize(width, height));
                                        parsed = true;
                                    }
                                    // Frame rate.
                                    m2 = FRAME_RATE_PATTERN.matcher(token);
                                    if (!parsed && m2.find()) {
                                        try {
                                            float frameRate = Float.parseFloat(m2.group(1));
                                            video.setFrameRate(frameRate);
                                        } catch (NumberFormatException e) {
                                            ;
                                        }
                                        parsed = true;
                                    }
                                    // Bit rate.
                                    m2 = BIT_RATE_PATTERN.matcher(token);
                                    if (!parsed && m2.find()) {
                                        int bitRate = Integer.parseInt(m2.group(1));
                                        video.setBitRate(bitRate);
                                        parsed = true;
                                    }
                                }
                            }
                            info.setVideo(video);
                            videoInfo = true;
                            creations.add("video@");
                        }
                        if ("Audio".equalsIgnoreCase(type)) {
                            StringTokenizer st = new StringTokenizer(specs, ",");
                            for (int i = 0; st.hasMoreTokens(); i++) {
                                String token = st.nextToken().trim();
                                if (i == 0) {
                                    audio.setDecoder(token);
                                } else {
                                    boolean parsed = false;
                                    // Sampling rate.
                                    Matcher m2 = SAMPLING_RATE_PATTERN.matcher(token);
                                    if (!parsed && m2.find()) {
                                        int samplingRate = Integer.parseInt(m2.group(1));
                                        audio.setSamplingRate(samplingRate);
                                        parsed = true;
                                    }
                                    // Channels.
                                    m2 = CHANNELS_PATTERN.matcher(token);
                                    if (!parsed && m2.find()) {
                                        String ms = m2.group(1);
                                        if ("mono".equalsIgnoreCase(ms)) {
                                            audio.setChannels(1);
                                        } else if ("stereo".equalsIgnoreCase(ms)) {
                                            audio.setChannels(2);
                                        }
                                        parsed = true;
                                    }
                                    // Bit rate.
                                    m2 = BIT_RATE_PATTERN.matcher(token);
                                    if (!parsed && m2.find()) {
                                        int bitRate = Integer.parseInt(m2.group(1));
                                        audio.setBitRate(bitRate);
                                        parsed = true;
                                    }
                                }
                            }
                            info.setAudio(audio);
                            audioInfo = true;
                            creations.add("audio@");
                        }
                    }
                    if (audioInfo == true && videoInfo == true && creationflag == 3) {
                        step = 3;
                        for (String creation : creations) {
                            if (creation == null || creation == "") continue;
                            String[] split = creation.split("@");
                            if (creation.contains("metadata")) {
                                info.setCreationTime(DateUtil.UTCGMT2Date(split[1]));
                                continue;
                            }
                            if (creation.contains("video")) {
                                video.setCreationTime(DateUtil.UTCGMT2Date(split[1]));
                                info.setVideo(video);
                                continue;
                            }
                            if (creation.contains("audio")) {
                                audio.setCreationTime(DateUtil.UTCGMT2Date(split[1]));
                                info.setAudio(audio);
                            }
                        }
                    }
                }
                if (line.contains("creation_time")) {
                    String[] split = line.split(" :");
                    if (split.length == 2) {
                        creationflag++;
                        creations.set(creations.size() - 1, creations.getLast() + split[1].trim());
                    }
                }
                if (step == 3) {
                    reader.reinsertLine(line);
                    break;
                }
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        }
        if (info == null) {
            throw new InputFormatException();
        }
        return info;
    }

    /**
     * Private utility. Parse a line and try to match its contents against the
     * {@link Encoder#PROGRESS_INFO_PATTERN} pattern. It the line can be parsed,
     * it returns a hashtable with progress informations, otherwise it returns
     * null.
     *
     * @param line The line from the ffmpeg output.
     * @return A hashtable with the value reported in the line, or null if the
     * given line can not be parsed.
     */
    private Hashtable parseProgressInfoLine(String line) {
        Hashtable table = null;
        Matcher m = PROGRESS_INFO_PATTERN.matcher(line);
        while (m.find()) {
            if (table == null) {
                table = new Hashtable();
            }
            String key = m.group(1);
            String value = m.group(2);
            table.put(key, value);
        }
        return table;
    }

    /**
     * Re-encode a multimedia file.
     *
     * @param source     The source multimedia file. It cannot be null. Be sure this
     *                   file can be decoded (see
     *                   {@link Encoder#getSupportedDecodingFormats()},
     *                   {@link Encoder#getAudioDecoders()} and
     *                   {@link Encoder#getVideoDecoders()}).
     * @param target     The target multimedia re-encoded file. It cannot be null. If
     *                   this file already exists, it will be overwrited.
     * @param attributes A set of attributes for the encoding process.
     * @throws IllegalArgumentException If both audio and video parameters are null.
     * @throws InputFormatException     If the source multimedia file cannot be decoded.
     * @throws EncoderException         If a problems occurs during the encoding process.
     */
    public void encode(File source, File target, EncodingAttributes attributes) throws IllegalArgumentException, InputFormatException, EncoderException {
        encode(source, target, attributes, null);
    }

    /**
     * Re-encode a multimedia file.
     *
     * @param source     The source multimedia file. It cannot be null. Be sure this
     *                   file can be decoded (see
     *                   {@link Encoder#getSupportedDecodingFormats()},
     *                   {@link Encoder#getAudioDecoders()} and
     *                   {@link Encoder#getVideoDecoders()}).
     * @param target     The target multimedia re-encoded file. It cannot be null. If
     *                   this file already exists, it will be overwrited.
     * @param attributes A set of attributes for the encoding process.
     * @param listener   An optional progress listener for the encoding process. It can
     *                   be null.
     * @throws IllegalArgumentException If both audio and video parameters are null.
     * @throws InputFormatException     If the source multimedia file cannot be decoded.
     * @throws EncoderException         If a problems occurs during the encoding process.
     */
    public void encode(File source, File target, EncodingAttributes attributes, EncoderProgressListener listener) throws IllegalArgumentException, InputFormatException, EncoderException {
        String formatAttribute = attributes.getFormat();
        Float offsetAttribute = attributes.getOffset();
        Float durationAttribute = attributes.getDuration();
        AudioAttributes audioAttributes = attributes.getAudioAttributes();
        VideoAttributes videoAttributes = attributes.getVideoAttributes();
        if (audioAttributes == null && videoAttributes == null) {
            throw new IllegalArgumentException("Both audio and video attributes are null");
        }
        target = target.getAbsoluteFile();
        target.getParentFile().mkdirs();
        FFMPEGExecutor ffmpeg = locator.createExecutor();

        if (offsetAttribute != null) {
            ffmpeg.addArgument("-ss");
            ffmpeg.addArgument(String.valueOf(offsetAttribute.floatValue()));
        }
        if (attributes.getFflags() != null && attributes.getFflags().length() > 0) {
            ffmpeg.addArgument("-fflags");
            ffmpeg.addArgument(attributes.getFflags());
        }

        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(source.getAbsolutePath());

        if (attributes.getMetadataSv() != null && attributes.getMetadataSv().length() > 0) {
            ffmpeg.addArgument("-metadata:s:v");
            ffmpeg.addArgument(attributes.getMetadataSv());
        }
        if (durationAttribute != null) {
            ffmpeg.addArgument("-t");
            ffmpeg.addArgument(String.valueOf(durationAttribute.floatValue()));
        }
        if (videoAttributes == null) {
            ffmpeg.addArgument("-vn");
        } else {
            String codec = videoAttributes.getCodec();
            if (codec != null) {
                ffmpeg.addArgument("-vcodec");
                ffmpeg.addArgument(codec);
            }
            String tag = videoAttributes.getTag();
            if (tag != null) {
                ffmpeg.addArgument("-vtag");
                ffmpeg.addArgument(tag);
            }
            Integer bitRate = videoAttributes.getBitRate();
            if (bitRate != null) {
                ffmpeg.addArgument("-b");
                ffmpeg.addArgument(String.valueOf(bitRate.intValue()));
            }
            Integer frameRate = videoAttributes.getFrameRate();
            if (frameRate != null) {
                ffmpeg.addArgument("-r");
                ffmpeg.addArgument(String.valueOf(frameRate.intValue()));
            }

            //ffmpeg -i input.mkv -an -filter:v "setpts=0.5*PTS" output.mkv
            if (audioAttributes == null || audioAttributes.getAf_Atempo() == null) {
                String setpts = videoAttributes.getSetpts();
                if (setpts != null && setpts != "") {
                    ffmpeg.addArgument("-filter:v");
                    String arg = "setpts=%s*PTS";
                    arg = String.format(arg, setpts);
                    ffmpeg.addArgument(arg);
                }
            }

            VideoSize size = videoAttributes.getSize();
            if (size != null) {
                ffmpeg.addArgument("-s");
                ffmpeg.addArgument(String.valueOf(size.getWidth()) + "x" + String.valueOf(size.getHeight()));
            }

            String startTime = videoAttributes.getStartTime();
            if (startTime != null) {
                ffmpeg.addArgument("-ss");
                ffmpeg.addArgument(startTime);
            }

            String duration = videoAttributes.getDuration();
            if (duration != null) {
                ffmpeg.addArgument("-t");
                ffmpeg.addArgument(duration);
            }

            if (videoAttributes.getQv() != null && videoAttributes.getQv().length() > 0) {
                ffmpeg.addArgument("-q:v");
                ffmpeg.addArgument(videoAttributes.getQv());
            }

            if (videoAttributes.getVf() != null && videoAttributes.getVf().length() > 0) {
                ffmpeg.addArgument("-vf");
                ffmpeg.addArgument(videoAttributes.getVf());
            }

            if (videoAttributes.getBv() != null && videoAttributes.getBv().length() > 0) {
                ffmpeg.addArgument("-b:v");
                ffmpeg.addArgument(videoAttributes.getBv());
            }

            if (videoAttributes.getBufsize() != null && videoAttributes.getBufsize().length() > 0) {
                ffmpeg.addArgument("-bufsize");
                ffmpeg.addArgument(videoAttributes.getBufsize());
            }

            if (videoAttributes.getMaxrate() != null && videoAttributes.getMaxrate().length() > 0) {
                ffmpeg.addArgument("-maxrate");
                ffmpeg.addArgument(videoAttributes.getMaxrate());
            }
        }
        if (audioAttributes == null) {
            ffmpeg.addArgument("-an");
        } else {
            String codec = audioAttributes.getCodec();
            if (codec != null) {
                ffmpeg.addArgument("-acodec");
                ffmpeg.addArgument(codec);
            }
            Integer bitRate = audioAttributes.getBitRate();
            if (bitRate != null) {
                ffmpeg.addArgument("-ab");
                ffmpeg.addArgument(String.valueOf(bitRate.intValue()));
            }
            Integer channels = audioAttributes.getChannels();
            if (channels != null) {
                ffmpeg.addArgument("-ac");
                ffmpeg.addArgument(String.valueOf(channels.intValue()));
            }
            Integer samplingRate = audioAttributes.getSamplingRate();
            if (samplingRate != null) {
                ffmpeg.addArgument("-ar");
                ffmpeg.addArgument(String.valueOf(samplingRate.intValue()));
            }

            Integer vol = audioAttributes.getVol();
            if (vol != null) {
                ffmpeg.addArgument("-vol");
                ffmpeg.addArgument(String.valueOf(vol.intValue()));
            }
            String startTime = audioAttributes.getStartTime();
            if (startTime != null) {
                ffmpeg.addArgument("-ss");
                ffmpeg.addArgument(startTime);
            }
            String duration = audioAttributes.getDuration();
            if (duration != null) {
                ffmpeg.addArgument("-t");
                ffmpeg.addArgument(duration);
            }

            if (audioAttributes.getAf() != null && audioAttributes.getAf() != "") {
                ffmpeg.addArgument("-af");
                ffmpeg.addArgument(audioAttributes.getAf());
            }

            if (videoAttributes == null || videoAttributes.getSetpts() == null || videoAttributes.getSetpts() == "") {
                if (audioAttributes.getAf_Atempo() != null && audioAttributes.getAf_Atempo() != "") {
                    String af_atempo = audioAttributes.getAf_Atempo();
                    String videoArg = String.format("atempo=%s", af_atempo);

                    ffmpeg.addArgument("-af");
                    ffmpeg.addArgument(videoArg);
                }
            }

            if (audioAttributes.getAf_volume() != null && audioAttributes.getAf_volume() != "") {
                String af_volume = audioAttributes.getAf_volume();
                String audioArg = String.format("volume=%s", af_volume);

                ffmpeg.addArgument("-af");
                ffmpeg.addArgument(audioArg);
            }
        }

        if (videoAttributes != null && audioAttributes != null) {
            //音视频同时调整倍速
            if (videoAttributes.getSetpts() != null && videoAttributes.getSetpts() != "" && audioAttributes.getAf_Atempo() != null && audioAttributes.getAf_Atempo() != "") {
                //ffmpeg -i input.mkv -filter_complex "[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]" -map "[v]" -map "[a]" output.mkv
                String setptsValue = videoAttributes.getSetpts();
                String videoArg = "[0:v]setpts=%s*PTS[v];";
                videoArg = String.format(videoArg, setptsValue);

                String afValue = audioAttributes.getAf_Atempo();
                String audioArg = "[0:a]atempo=%s[a]";
                audioArg = String.format(audioArg, afValue);

                ffmpeg.addArgument("-filter_complex");
                ffmpeg.addArgument(videoArg + audioArg);

                ffmpeg.addArgument("-map");
                ffmpeg.addArgument("[v]");

                ffmpeg.addArgument("-map");
                ffmpeg.addArgument("[a]");
            }
        }


        if (formatAttribute != null && formatAttribute.length() > 0) {
            ffmpeg.addArgument("-f");
            ffmpeg.addArgument(formatAttribute);
        }

        ffmpeg.addArgument("-y");
        ffmpeg.addArgument(target.getAbsolutePath());

        try {
            ffmpeg.execute();
        } catch (IOException e) {
            throw new EncoderException(e);
        }
        try {
            RBufferedReader reader = new RBufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            processErrorOutput(attributes, reader, source, listener);
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            ffmpeg.destroy();
        }
    }


    protected void processErrorOutput(EncodingAttributes attributes, BufferedReader errorReader, File source, EncoderProgressListener listener) throws EncoderException, IOException {
        String lastWarning = null;
        long progress = 0L;
        Float offsetAttribute = attributes.getOffset();
        MultimediaInfo info = parseMultimediaInfo(source, (RBufferedReader) errorReader);
        Float durationAttribute = attributes.getDuration();
        long duration;
        if (durationAttribute != null) {
            duration = Math.round(durationAttribute.floatValue() * 1000.0F);
        } else {
            duration = info.getDuration();
            if (offsetAttribute != null) {
                duration -= Math.round(offsetAttribute.floatValue() * 1000.0F);
            }
        }

        if (listener != null) {
            listener.sourceInfo(info);
        }
        int step = 0;
        String line;
        while ((line = errorReader.readLine()) != null) {
            System.out.println(line);
            if (step == 0) {
                if (line.startsWith("WARNING: ")) {
                    if (listener != null) listener.message(line);
                } else {
                    if (!line.startsWith("Output #0")) {
                        throw new EncoderException(line);
                    }
                    step++;
                }
            } else if ((step == 1) && (!line.startsWith("  "))) {
                step++;
            }

            if (step == 2) {
                if (!line.startsWith("Stream mapping:")) {
                    throw new EncoderException(line);
                }
                step++;
            } else if ((step == 3) && (!line.startsWith("  "))) {
                step++;
            }

            if (step == 4) {
                line = line.trim();
                if (line.length() > 0) {
                    Hashtable table = parseProgressInfoLine(line);
                    if (table == null) {
                        if (listener != null) {
                            listener.message(line);
                        }
                        lastWarning = line;
                    } else {
                        if (listener != null) {
                            String time = (String) table.get("time");
                            if (time != null) {
                                int dot = time.indexOf(46);
                                if ((dot > 0) && (dot == time.length() - 2) && (duration > 0L)) {
                                    String p1 = time.substring(0, dot);
                                    String p2 = time.substring(dot + 1);
                                    try {
                                        long i1 = Long.parseLong(p1);
                                        long i2 = Long.parseLong(p2);
                                        progress = i1 * 1000L + i2 * 100L;

                                        int perm = (int) Math.round(progress * 1000L / duration);

                                        if (perm > 1000) {
                                            perm = 1000;
                                        }
                                        listener.progress(perm);
                                    } catch (NumberFormatException e) {
                                    }
                                }
                            }
                        }
                        lastWarning = null;
                    }
                }
            }
        }
        if ((lastWarning != null) && (!SUCCESS_PATTERN.matcher(lastWarning).matches()))
            throw new EncoderException(lastWarning);
    }


    /**
     * 主要用于合并 多个 音频文件
     *
     * @param sourceList 传入文件list
     * @param target     转换后的文件
     * @param attributes 属性
     * @throws IllegalArgumentException 异常
     * @throws InputFormatException     异常
     * @throws EncoderException         异常
     */
    public void encodeMergeAudio(List<File> sourceList, File target, EncodingAttributes attributes) throws IllegalArgumentException, InputFormatException, EncoderException {
        encodeMergeAudio(sourceList, target, attributes, null);
    }

    public void encodeMergeAudio(List<File> sourceList, File target, EncodingAttributes attributes, EncoderProgressListener listener) throws IllegalArgumentException, InputFormatException, EncoderException {
        String formatAttribute = attributes.getFormat();
        Float offsetAttribute = attributes.getOffset();
        Float durationAttribute = attributes.getDuration();

        AudioAttributes audioAttributes = attributes.getAudioAttributes();
        AudioMergeTypeEnum mergeType = audioAttributes.getMergeType();

        VideoAttributes videoAttributes = attributes.getVideoAttributes();

        if (audioAttributes == null && videoAttributes == null) {
            throw new IllegalArgumentException("Both audio and video attributes are null");
        }
        target = target.getAbsoluteFile();
        target.getParentFile().mkdirs();
        FFMPEGExecutor ffmpeg = locator.createExecutor();


        if (offsetAttribute != null) {
            ffmpeg.addArgument("-ss");
            ffmpeg.addArgument(String.valueOf(offsetAttribute.floatValue()));
        }

        for (File source : sourceList) {
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(source.getAbsolutePath());

        }

        if (durationAttribute != null) {
            ffmpeg.addArgument("-t");
            ffmpeg.addArgument(String.valueOf(durationAttribute.floatValue()));
        }
        // ps: ffmpeg -i aaa.mp3 -i bbb.mp3 -filter_complex '[0:0][1:0]concat=n=2:v=0:a=1[out]' -map '[out]' output2.wav
        if (mergeType != null && AudioMergeTypeEnum.SPLIT_JOINT == mergeType) {
            ffmpeg.addArgument("-filter_complex");
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < sourceList.size(); i++) {
                sb.append("[").append(i).append(":0]");
            }
            sb.append("concat=n=").append(sourceList.size());
            sb.append(":v=0:a=1[out]");
            ffmpeg.addArgument(sb.toString());

            ffmpeg.addArgument("-map");
            ffmpeg.addArgument("[out]");
        }

        //ps: ffmpeg -i aaa.mp3  -i bbb.mp3 -filter_complex amix=inputs=2 c.wav
        if (mergeType != null && AudioMergeTypeEnum.ADMIX == mergeType) {
            ffmpeg.addArgument("-filter_complex");
            ffmpeg.addArgument("amix=inputs=" + sourceList.size());
        }

        if (audioAttributes != null && audioAttributes.getAb() != null) {
            ffmpeg.addArgument("-ab");
            ffmpeg.addArgument(audioAttributes.getAb());
        }

        if (formatAttribute != null) {
            ffmpeg.addArgument("-f");
            ffmpeg.addArgument(formatAttribute);
        }

        ffmpeg.addArgument("-y");
        ffmpeg.addArgument(target.getAbsolutePath());
        try {
            ffmpeg.execute();
        } catch (IOException e) {
            throw new EncoderException(e);
        }
        try {
            RBufferedReader reader = new RBufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            processErrorOutput(attributes, reader, null, listener);
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            ffmpeg.destroy();
        }
    }

    /**
     * 音频视频 合并
     * <p>
     * -- 视频音频合并 视频中没有音频
     * ffmpeg -i face.mp4 -i wangzherongyao.wav -c:v copy -c:a aac -strict experimental a12345.mp4
     * <p>
     * -- 视频音频合并 用audio音频替换video中的音频
     * ffmpeg -i girl.mp4 -i wangzherongyao.wav -c:v copy -c:a aac -strict experimental -map 0:v:0 -map 1:a:0 a123456.mp4
     *
     * @param files      传入文件list
     * @param target     转换后的文件
     * @param attributes 属性
     * @throws IllegalArgumentException 异常
     * @throws InputFormatException     异常
     * @throws EncoderException         异常
     */
    public void encodeMergeVideoAndAudio(List<File> files, File target, EncodingAttributes attributes) throws IllegalArgumentException, InputFormatException, EncoderException {
        encodeMergeVideoAndAudio(files, target, attributes, null);
    }

    public void encodeMergeVideoAndAudio(List<File> files, File target, EncodingAttributes attributes, EncoderProgressListener listener) throws IllegalArgumentException, InputFormatException, EncoderException {
        String formatAttribute = attributes.getFormat();
        Float offsetAttribute = attributes.getOffset();
        Float durationAttribute = attributes.getDuration();
        AudioAttributes audioAttributes = attributes.getAudioAttributes();
        VideoAttributes videoAttributes = attributes.getVideoAttributes();
        if (audioAttributes == null && videoAttributes == null) {
            throw new IllegalArgumentException("Both audio and video attributes are null");
        }
        target = target.getAbsoluteFile();
        target.getParentFile().mkdirs();
        FFMPEGExecutor ffmpeg = locator.createExecutor();

        if (offsetAttribute != null) {
            ffmpeg.addArgument("-ss");
            ffmpeg.addArgument(String.valueOf(offsetAttribute.floatValue()));
        }

        for (File file : files) {
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(file.getAbsolutePath());
        }

        if (durationAttribute != null) {
            ffmpeg.addArgument("-t");
            ffmpeg.addArgument(String.valueOf(durationAttribute.floatValue()));
        }
        if (videoAttributes == null) {
            ffmpeg.addArgument("-vn");
        } else {
            String codec = videoAttributes.getCodec();
            if (codec != null) {
                ffmpeg.addArgument("-vcodec");
                ffmpeg.addArgument(codec);
            }
            String tag = videoAttributes.getTag();
            if (tag != null) {
                ffmpeg.addArgument("-vtag");
                ffmpeg.addArgument(tag);
            }
            Integer bitRate = videoAttributes.getBitRate();
            if (bitRate != null) {
                ffmpeg.addArgument("-b");
                ffmpeg.addArgument(String.valueOf(bitRate.intValue()));
            }
            Integer frameRate = videoAttributes.getFrameRate();
            if (frameRate != null) {
                ffmpeg.addArgument("-r");
                ffmpeg.addArgument(String.valueOf(frameRate.intValue()));
            }
            VideoSize size = videoAttributes.getSize();
            if (size != null) {
                ffmpeg.addArgument("-s");
                ffmpeg.addArgument(String.valueOf(size.getWidth()) + "x" + String.valueOf(size.getHeight()));
            }

            String startTime = videoAttributes.getStartTime();
            if (startTime != null) {
                ffmpeg.addArgument("-ss");
                ffmpeg.addArgument(startTime);
            }
            String duration = videoAttributes.getDuration();
            if (duration != null) {
                ffmpeg.addArgument("-t");
                ffmpeg.addArgument(duration);
            }
        }
        if (audioAttributes == null) {
            ffmpeg.addArgument("-an");
        } else {
            String codec = audioAttributes.getCodec();
            if (codec != null) {
                ffmpeg.addArgument("-acodec");
                ffmpeg.addArgument(codec);
            }
            Integer bitRate = audioAttributes.getBitRate();
            if (bitRate != null) {
                ffmpeg.addArgument("-ab");
                ffmpeg.addArgument(String.valueOf(bitRate.intValue()));
            }
            Integer channels = audioAttributes.getChannels();
            if (channels != null) {
                ffmpeg.addArgument("-ac");
                ffmpeg.addArgument(String.valueOf(channels.intValue()));
            }
            Integer samplingRate = audioAttributes.getSamplingRate();
            if (samplingRate != null) {
                ffmpeg.addArgument("-ar");
                ffmpeg.addArgument(String.valueOf(samplingRate.intValue()));
            }

            Integer vol = audioAttributes.getVol();
            if (vol != null) {
                ffmpeg.addArgument("-vol");
                ffmpeg.addArgument(String.valueOf(vol.intValue()));
            }
            String startTime = audioAttributes.getStartTime();
            if (startTime != null) {
                ffmpeg.addArgument("-ss");
                ffmpeg.addArgument(startTime);
            }
            String duration = audioAttributes.getDuration();
            if (duration != null) {
                ffmpeg.addArgument("-t");
                ffmpeg.addArgument(duration);
            }

        }
        ffmpeg.addArgument("-strict");
        ffmpeg.addArgument("experimental");

        if (videoAttributes != null && videoAttributes.getMergeType().getIndex() == 2) {
            ffmpeg.addArgument("-map");
            ffmpeg.addArgument("0:v:0");

            ffmpeg.addArgument("-map");
            ffmpeg.addArgument("1:a:0");
        }

        if (formatAttribute != null) {
            ffmpeg.addArgument("-f");
            ffmpeg.addArgument(formatAttribute);
        }
        ffmpeg.addArgument("-y");
        ffmpeg.addArgument(target.getAbsolutePath());
        try {
            ffmpeg.execute();
        } catch (IOException e) {
            throw new EncoderException(e);
        }
        try {
            RBufferedReader reader = new RBufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            processErrorOutput(attributes, reader, null, listener);
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            ffmpeg.destroy();
        }
    }


    /**
     * 有损合并视频
     * 注意：
     * 1、输出格式为mkv
     * ps：ffmpeg -i girl.mp4 -i girl.mp4 -i man.mp4  -filter_complex '[0:0] [0:1] [1:0] [1:1] [2:0] [2:1] concat=n=3:v=1:a=1 [v] [a]' -map '[v]' -map '[a]' -vcodec h264  -acodec libmp3lame -f mp4 -y output.mkv
     *
     * @param files      传入文件list
     * @param target     转换后的文件
     * @param attributes 属性
     * @throws IllegalArgumentException 异常
     * @throws InputFormatException     异常
     * @throws EncoderException         异常
     */
    public void encodeMergeVideoByDamaging(List<File> files, File target, EncodingAttributes attributes) throws IllegalArgumentException, InputFormatException, EncoderException {
        encodeMergeVideoByDamaging(files, target, attributes, null);
    }


    public void encodeMergeVideoByDamaging(List<File> files, File target, EncodingAttributes attributes, EncoderProgressListener listener) throws IllegalArgumentException, InputFormatException, EncoderException {
        String formatAttribute = attributes.getFormat();
        Float offsetAttribute = attributes.getOffset();
        Float durationAttribute = attributes.getDuration();
        AudioAttributes audioAttributes = attributes.getAudioAttributes();
        VideoAttributes videoAttributes = attributes.getVideoAttributes();
        if (audioAttributes == null && videoAttributes == null) {
            throw new IllegalArgumentException("Both audio and video attributes are null");
        }
        target = target.getAbsoluteFile();
        target.getParentFile().mkdirs();
        FFMPEGExecutor ffmpeg = locator.createExecutor();

        if (offsetAttribute != null) {
            ffmpeg.addArgument("-ss");
            ffmpeg.addArgument(String.valueOf(offsetAttribute.floatValue()));
        }
        if (files == null || files.size() == 0) {
            throw new IllegalArgumentException("请传入要合并的文件");
        }
        for (File file : files) {
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(file.getAbsolutePath());
        }

        ffmpeg.addArgument("-filter_complex");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < files.size(); i++) {
            sb.append("[").append(i).append(":0] ");
            sb.append("[").append(i).append(":1] ");
        }
        sb.append("concat=n=").append(files.size()).append(":v=1:a=1 [v] [a]");
        ffmpeg.addArgument(sb.toString());

        ffmpeg.addArgument("-map");
        ffmpeg.addArgument("[v]");

        ffmpeg.addArgument("-map");
        ffmpeg.addArgument("[a]");

        if (durationAttribute != null) {
            ffmpeg.addArgument("-t");
            ffmpeg.addArgument(String.valueOf(durationAttribute.floatValue()));
        }
        if (videoAttributes == null) {
            ffmpeg.addArgument("-vn");
        } else {
            String codec = videoAttributes.getCodec();
            if (codec != null) {
                ffmpeg.addArgument("-vcodec");
                ffmpeg.addArgument(codec);
            }
            String tag = videoAttributes.getTag();
            if (tag != null) {
                ffmpeg.addArgument("-vtag");
                ffmpeg.addArgument(tag);
            }
            Integer bitRate = videoAttributes.getBitRate();
            if (bitRate != null) {
                ffmpeg.addArgument("-b");
                ffmpeg.addArgument(String.valueOf(bitRate.intValue()));
            }
            Integer frameRate = videoAttributes.getFrameRate();
            if (frameRate != null) {
                ffmpeg.addArgument("-r");
                ffmpeg.addArgument(String.valueOf(frameRate.intValue()));
            }
            VideoSize size = videoAttributes.getSize();
            if (size != null) {
                ffmpeg.addArgument("-s");
                ffmpeg.addArgument(String.valueOf(size.getWidth()) + "x" + String.valueOf(size.getHeight()));
            }

            String startTime = videoAttributes.getStartTime();
            if (startTime != null) {
                ffmpeg.addArgument("-ss");
                ffmpeg.addArgument(startTime);
            }
            String duration = videoAttributes.getDuration();
            if (duration != null) {
                ffmpeg.addArgument("-t");
                ffmpeg.addArgument(duration);
            }
        }
        if (audioAttributes == null) {
            ffmpeg.addArgument("-an");
        } else {
            String codec = audioAttributes.getCodec();
            if (codec != null) {
                ffmpeg.addArgument("-acodec");
                ffmpeg.addArgument(codec);
            }
            Integer bitRate = audioAttributes.getBitRate();
            if (bitRate != null) {
                ffmpeg.addArgument("-ab");
                ffmpeg.addArgument(String.valueOf(bitRate.intValue()));
            }
            Integer channels = audioAttributes.getChannels();
            if (channels != null) {
                ffmpeg.addArgument("-ac");
                ffmpeg.addArgument(String.valueOf(channels.intValue()));
            }
            Integer samplingRate = audioAttributes.getSamplingRate();
            if (samplingRate != null) {
                ffmpeg.addArgument("-ar");
                ffmpeg.addArgument(String.valueOf(samplingRate.intValue()));
            }

            Integer vol = audioAttributes.getVol();
            if (vol != null) {
                ffmpeg.addArgument("-vol");
                ffmpeg.addArgument(String.valueOf(vol.intValue()));
            }
            String startTime = audioAttributes.getStartTime();
            if (startTime != null) {
                ffmpeg.addArgument("-ss");
                ffmpeg.addArgument(startTime);
            }
            String duration = audioAttributes.getDuration();
            if (duration != null) {
                ffmpeg.addArgument("-t");
                ffmpeg.addArgument(duration);
            }

        }
        if (formatAttribute != null) {
            ffmpeg.addArgument("-f");
            ffmpeg.addArgument(formatAttribute);
        }
        ffmpeg.addArgument("-y");
        ffmpeg.addArgument(target.getAbsolutePath());
        try {
            ffmpeg.execute();
        } catch (IOException e) {
            throw new EncoderException(e);
        }
        try {
            RBufferedReader reader = new RBufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            processErrorOutput(attributes, reader, null, listener);
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            ffmpeg.destroy();
        }
    }


    /**
     * 无损合并视频
     * 注意：
     * 1、如果第一个视频没有声音，那么合并后的视频也是没有声音的
     * 2、必须保证所有视频的格式，分辨率都一样，不然结果不可控
     *
     * @param txtFile    传入文本文件
     * @param target     转换后的文件
     * @param attributes 属性
     * @throws IllegalArgumentException 异常
     * @throws InputFormatException     异常
     * @throws EncoderException         异常
     */
    public void encodeMergeVideoByLossless(File txtFile, File target, EncodingAttributes attributes) throws IllegalArgumentException, InputFormatException, EncoderException {
        encodeMergeVideoByLossless(txtFile, target, attributes, null);
    }


    public void encodeMergeVideoByLossless(File txtFile, File target, EncodingAttributes attributes, EncoderProgressListener listener) throws IllegalArgumentException, InputFormatException, EncoderException {
        String formatAttribute = attributes.getFormat();
        Float offsetAttribute = attributes.getOffset();
        Float durationAttribute = attributes.getDuration();
        AudioAttributes audioAttributes = attributes.getAudioAttributes();
        VideoAttributes videoAttributes = attributes.getVideoAttributes();
        if (audioAttributes == null && videoAttributes == null) {
            throw new IllegalArgumentException("Both audio and video attributes are null");
        }
        target = target.getAbsoluteFile();
        target.getParentFile().mkdirs();
        FFMPEGExecutor ffmpeg = locator.createExecutor();

        if (offsetAttribute != null) {
            ffmpeg.addArgument("-ss");
            ffmpeg.addArgument(String.valueOf(offsetAttribute.floatValue()));
        }

        ffmpeg.addArgument("-f");
        ffmpeg.addArgument("concat");

        ffmpeg.addArgument("-safe");
        ffmpeg.addArgument("0");

        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(txtFile.getAbsolutePath());
        if (durationAttribute != null) {
            ffmpeg.addArgument("-t");
            ffmpeg.addArgument(String.valueOf(durationAttribute.floatValue()));
        }
        if (videoAttributes == null) {
            ffmpeg.addArgument("-vn");
        } else {
            String codec = videoAttributes.getCodec();
            if (codec != null) {
                ffmpeg.addArgument("-vcodec");
                ffmpeg.addArgument(codec);
            }
            String tag = videoAttributes.getTag();
            if (tag != null) {
                ffmpeg.addArgument("-vtag");
                ffmpeg.addArgument(tag);
            }
            Integer bitRate = videoAttributes.getBitRate();
            if (bitRate != null) {
                ffmpeg.addArgument("-b");
                ffmpeg.addArgument(String.valueOf(bitRate.intValue()));
            }
            Integer frameRate = videoAttributes.getFrameRate();
            if (frameRate != null) {
                ffmpeg.addArgument("-r");
                ffmpeg.addArgument(String.valueOf(frameRate.intValue()));
            }
            VideoSize size = videoAttributes.getSize();
            if (size != null) {
                ffmpeg.addArgument("-s");
                ffmpeg.addArgument(String.valueOf(size.getWidth()) + "x" + String.valueOf(size.getHeight()));
            }

            String startTime = videoAttributes.getStartTime();
            if (startTime != null) {
                ffmpeg.addArgument("-ss");
                ffmpeg.addArgument(startTime);
            }
            String duration = videoAttributes.getDuration();
            if (duration != null) {
                ffmpeg.addArgument("-t");
                ffmpeg.addArgument(duration);
            }
        }
        if (audioAttributes == null) {
            ffmpeg.addArgument("-an");
        } else {
            String codec = audioAttributes.getCodec();
            if (codec != null) {
                ffmpeg.addArgument("-acodec");
                ffmpeg.addArgument(codec);
            }
            Integer bitRate = audioAttributes.getBitRate();
            if (bitRate != null) {
                ffmpeg.addArgument("-ab");
                ffmpeg.addArgument(String.valueOf(bitRate.intValue()));
            }
            Integer channels = audioAttributes.getChannels();
            if (channels != null) {
                ffmpeg.addArgument("-ac");
                ffmpeg.addArgument(String.valueOf(channels.intValue()));
            }
            Integer samplingRate = audioAttributes.getSamplingRate();
            if (samplingRate != null) {
                ffmpeg.addArgument("-ar");
                ffmpeg.addArgument(String.valueOf(samplingRate.intValue()));
            }

            Integer vol = audioAttributes.getVol();
            if (vol != null) {
                ffmpeg.addArgument("-vol");
                ffmpeg.addArgument(String.valueOf(vol.intValue()));
            }
            String startTime = audioAttributes.getStartTime();
            if (startTime != null) {
                ffmpeg.addArgument("-ss");
                ffmpeg.addArgument(startTime);
            }
            String duration = audioAttributes.getDuration();
            if (duration != null) {
                ffmpeg.addArgument("-t");
                ffmpeg.addArgument(duration);
            }

        }
        if (formatAttribute != null) {
            ffmpeg.addArgument("-f");
            ffmpeg.addArgument(formatAttribute);
        }
        ffmpeg.addArgument("-y");
        ffmpeg.addArgument(target.getAbsolutePath());
        try {
            ffmpeg.execute();
        } catch (IOException e) {
            throw new EncoderException(e);
        }
        try {
            RBufferedReader reader = new RBufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            processErrorOutput(attributes, reader, txtFile, listener);
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            ffmpeg.destroy();
        }
    }


}
