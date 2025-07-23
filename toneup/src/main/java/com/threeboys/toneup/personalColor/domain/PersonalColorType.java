package com.threeboys.toneup.personalColor.domain;

public enum PersonalColorType {
    SPRING(1),
    SUMMER(2),
    AUTUMN(3),
    WINTER(4);

    private final int code;

    PersonalColorType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // 역으로 숫자 → enum
    public static PersonalColorType fromCode(int code) {
        for (PersonalColorType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}

