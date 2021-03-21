package it.sauronsoftware.jave.enumers;

public enum AudioMergeTypeEnum {
    SPLIT_JOINT("splitoint", 1),//拼接
    ADMIX("admix", 2) ; //混合
    // 成员变量
    private String code;
    private int index;

    AudioMergeTypeEnum(String code, int index) {
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
