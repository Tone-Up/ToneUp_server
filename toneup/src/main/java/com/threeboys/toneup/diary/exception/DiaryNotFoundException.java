package com.threeboys.toneup.diary.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;

public class DiaryNotFoundException extends RuntimeException{
    public DiaryNotFoundException() {
        super(ErrorMessages.DIARY_NOT_FOUND);
    }

}
