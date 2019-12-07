package com.yapily.codingchallenge.domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StatusReport {
    private Status status;
    private long uniqueFacts;
    private long totalFacts;
    private Throwable throwable;

    public enum Status{
        COMPLETED, REPOSITORY_NOT_FULL, ERROR
    }
}
