package com.lfdb.zuptecnico.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by igorlira on 3/16/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Session {
    public User user;
    public String token;
    public String error;
}
