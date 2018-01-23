package com.lfdb.zuptecnico.entities.collections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lfdb.zuptecnico.entities.Flow;

/**
 * Created by igorlira on 7/26/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleFlowCollection
{
    public Flow flow;
}
