package com.sjsu.obdreader;

import com.github.pires.obd.commands.ObdCommand;

/**
 * Created by pavanibaradi on 4/4/17.
 */
public class ObdCommandJob {
    private Long id;
    private ObdCommand command;
    private ObdCommandJobState state;

    public ObdCommandJob(ObdCommand c) {
        command = c;
        state = ObdCommandJobState.NEW;
    }

    public Long getId() {
        return id;
    }

    public ObdCommandJobState getState() {
        return state;
    }

    public ObdCommand getCommand() {
        return command;
    }

    public void setState(ObdCommandJobState state) {
        this.state = state;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
