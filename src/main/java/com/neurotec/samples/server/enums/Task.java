package com.neurotec.samples.server.enums;

public enum Task {
    DEDUPLICATION(0),
    ENROLL(1),
    SPEED_TEST(2),
    SETTINGS(3);

    private int value;

    Task(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
