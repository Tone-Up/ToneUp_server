package com.threeboys.toneup.feed.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;

public class FeedNotFoundException extends RuntimeException {
    public FeedNotFoundException() {
        super(ErrorMessages.FEED_NOT_FOUND);
    }
}
