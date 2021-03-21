package it.sauronsoftware.jave.enumers;

public enum VideoMergeTypeEnum {
    INSERT("insert", 1),//视频中原本没有音频，插入音频
    REPLACE("replace", 2) ; //替换视频中的音频
    // 成员变量
    private String code;
    private int index;

    VideoMergeTypeEnum(String code, int index) {
        this.code = code;
        this.index = index;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
