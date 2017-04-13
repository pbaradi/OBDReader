package com.sjsu.obdreader;

/**
 * Created by pavanibaradi on 4/4/17.
 */
public enum ObdCommandJobState {
    NEW,
    RUNNING,
    FINISHED,
    EXECUTION_ERROR,
    QUEUE_ERROR,
    NOT_SUPPORTED
}
