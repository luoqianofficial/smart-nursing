package com.smartnursing.common.enums;

import lombok.Getter;

@Getter
public enum FileTypeEnum {
    VIDEO(1, "视频"),
    AUDIO(2, "音频"),
    IMAGE(3, "图片"),
    DOCUMENT(4, "文档");

    private final int code;
    private final String desc;
    FileTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
