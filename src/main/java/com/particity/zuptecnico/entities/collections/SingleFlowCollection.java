package com.particity.zuptecnico.entities.collections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.particity.zuptecnico.entities.Flow;

/**
 * Created by igorlira on 7/26/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleFlowCollection
{
    public Flow flow;
}
