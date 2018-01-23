package com.lfdb.zuptecnico.entities.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lfdb.zuptecnico.entities.Case;

/**
 * Created by igorlira on 8/8/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCaseStepResponse
{
    public String message;
    public String error;

    @JsonProperty("case")
    public Case _case;
}
