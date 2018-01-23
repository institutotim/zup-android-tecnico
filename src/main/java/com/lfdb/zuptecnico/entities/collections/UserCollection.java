package com.lfdb.zuptecnico.entities.collections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lfdb.zuptecnico.entities.User;

/**
 * Created by igorlira on 8/8/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCollection
{
    public User[] users;
}
