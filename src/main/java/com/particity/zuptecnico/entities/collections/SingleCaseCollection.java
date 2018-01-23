package com.particity.zuptecnico.entities.collections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.particity.zuptecnico.entities.Case;

/**
 * Created by igorlira on 7/26/14.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleCaseCollection
{
    public String message;
    @JsonProperty("case")
    public Case flowCase;
}
