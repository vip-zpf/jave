# 音频转码工具

因为是基于 JAVE 项目的修改，而 JAVE 是依赖 [ffmpeg](http://ffmpeg.org/) 所以可以适用于所有 ffmpeg
所支持的文件格式的转换。具体可以查看 [JAVE 官方文档](http://www.sauronsoftware.it/projects/jave/manual.php)

# 使用示例

## 引入 maven 依赖

```xml

<dependency>
    <groupId>com.github.vip-zpf</groupId>
    <artifactId>jave</artifactId>
    <version>1.0.6</version>
</dependency>
```

# 原理

1. 初始化时判断当前运行环境，将bin目录中对应的 ffmpeg 可执行文件拷贝到临时目录中
2. 根据文件类型及配置通过 Runtime.getRuntime().exec(cmd) 执行 ffmpeg 对应的转码命令

# JAVE 项目的问题

ffmpeg 是依赖运行环境的，JAVE 项目封装了ffmpeg，它通过上述的原理使 java 可以调用ffmpeg而且支持跨平台。

1. 项目老旧没再维护。官网最近版本是2009年发布的，其依赖的ffmpeg早已过时，很多情况下用不了。
2. 转码一直报异常 EncoderException: Stream mapping
3. 没有发布maven仓库，而且 JAVE 本身也不是一个maven项目
4. 不支持mac

# 本项目特点

本项目为解决上述问题而生。

* 这是一个maven项目
* 项目依赖的 ffmpeg 可执行文件经过验证可以使用（单元测试中提供了一个简单的检验方法）
* 支持 Linux/Windows/Mac 平台

# 扩展

如果程序无法通过拷贝资源文件的方式获取到 ffmpeg 的可执行文件或者内置的 ffmpeg 不支持你所使用的操作系统

你可以通过环境变量或者在 java 中设置 `System.setProperty("ffmpeg.home", "ffmpeg可执行文件所在的目录")` 的方式指定你的系统中安装的可用的 ffmpeg 文件的目录

如 `System.setProperty("ffmpeg.home", "/usr/local/bin/")`

# 例子

* 获取音视频信息

```
  File mp3Target = new File("target/test-classes/material/testAudio14.wav");
  Encoder encoder = new Encoder();
  try {
      MultimediaInfo info = encoder.getInfo(mp3Target);
      AudioInfo audio = info.getAudio();
      int samplingRate = audio.getSamplingRate();
      System.out.println(samplingRate);
  } catch (EncoderException e) {
      e.printStackTrace();
  }
```

* 剪切音频

```
  File source = new File("target/test-classes/material/longAudio.mp3");
  File mp3Target = new File("target/test-classes/material/testAudio14.mp3");
  AudioUtils.cutAndConvert(source, mp3Target, "mp3", "00:00:08", "00:00:10");
```

* 音频转换格式

```
  File source = new File("target/test-classes/material/longAudio.mp3");
  File wavTarget = new File("testAudio.wav");
  AudioUtils.amrToWav(source, wavTarget);
```

* 多段音频合并

```
  File source1 = new File("target/test-classes/material/sunwukong.mp3");
  File source2 = new File("target/test-classes/material/luban.mp3");
  File source3 = new File("target/test-classes/material/lvbu.wav");
  File source4 = new File("target/test-classes/material/diaochan.mp3");
  File source5 = new File("target/test-classes/material/direnjie.mp3");
  File targetSource = new File("target/test-classes/material/wangzherongyao.wav");
  AudioUtils.mergeAudio(Arrays.asList(source1, source2,source3,source4,source5), targetSource, null);
```

* 视频转图片

```
  File source = new File("target/test-classes/material/testVideo.avi");
  File target = new File("target/test-classes/material/image/image-%3d.jpeg");
  VideoUtils.thumbnail(source, target, null);
```

* 抽取视频中的音频

```
  File source = new File("target/test-classes/material/testVideo.avi");
  File target = new File("target/test-classes/material/target.wav");
  VideoUtils.getVoideoAudio(source, target, null);
```

* 无损-合并多段视频

```
  //1、如果第一个视频没有声音，那么合并后的视频也是没有声音的
  //2、必须保证所有视频的格式，分辨率都一样，不然结果不可控
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
```

* 有损-合并多段视频 （注意：合并后的文件格式是mkv）

```
  File source1 = new File("target/test-classes/material/girl.mp4");
  File source2 = new File("target/test-classes/material/man.mp4");
  File source3 = new File("target/test-classes/material/girl.mp4");
  File target = new File("target/test-classes/material/bbb.mkv");

  LinkedList<File> files = new LinkedList<>();
  files.add(source1);
  files.add(source2);
  files.add(source3);
  VideoUtils.mergeVideoByDamaging(files, target, "mp4");
```

* 视频中插入音频（视频原本无音频）

```
  File source1 = new File("target/test-classes/material/face.mp4");
  File source2 = new File("target/test-classes/material/wangzherongyao.wav");
  File target = new File("target/test-classes/material/videoAndAudio.mp4");

  LinkedList<File> files = new LinkedList<>();
  files.add(source1);
  files.add(source2);
  VideoUtils.mergeVoideoAndAudioByInsert(files, target, null);
```

* 替换视频中的音频

```
  File source1 = new File("target/test-classes/material/girl.mp4");
  File source2 = new File("target/test-classes/material/wangzherongyao.wav");
  File target = new File("target/test-classes/material/videoAndAudio2.mp4");

  LinkedList<File> files = new LinkedList<>();
  files.add(source1);
  files.add(source2);
  VideoUtils.mergeVoideoAndAudioByReplace(files, target, "mp4");
```

* 旋转视频

```
//ps: "transpose=1" 顺时针旋转画面90度
//ps: "transpose=2" 逆时针旋转画面90度
//ps: "transpose=3" 顺时针旋转画面90度再水平翻转
//ps: "transpose=0" 逆时针旋转画面90度再水平翻转
//ps: hflip 水平翻转视频画面
//ps: vflip 垂直翻转视频画面
File source = new File("target/test-classes/material/girl.mp4");
File target = new File("target/test-classes/material/girlRoate.mp4");
VideoUtils.roateVideo(source, target, "transpose=1");
  
```

* webm转mp4

```
File source = new File("target/test-classes/material/abc.webm");
File target = new File("target/test-classes/material/webm2MP4.mp4");
VideoUtils.webm2mp4(source, target, "2000k","2000k","2500k");
```



# 参考

借鉴 [JAVE](http://www.sauronsoftware.it/projects/jave/download.php) 的代码

本工具使用 [dadiyang/jave](https://github.com/dadiyang/jave) 源码改造而来

# LICENSE

JAVE 项目是基于 GPL 协议的开源项目，本项目是在 JAVE 的基础上进行修改和增强，因此也采用 GPL 协议开源。

> [JAVE]((http://www.sauronsoftware.it/projects/jave/)) is Free Software and it is licensed under GPL.