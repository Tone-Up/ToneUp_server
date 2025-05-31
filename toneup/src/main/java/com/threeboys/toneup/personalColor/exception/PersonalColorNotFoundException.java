package com.threeboys.toneup.personalColor.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;
import com.threeboys.toneup.personalColor.domain.PersonalColorType;

public class PersonalColorNotFoundException extends RuntimeException{
    public PersonalColorNotFoundException(PersonalColorType colorType) {
        super(String.format(ErrorMessages.PERSONAL_COLOR_NOT_FOUND, colorType.name()));
    }
}
