package com.particity.zuptecnico.entities.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by igorlira on 8/8/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferCaseStepResponse
{
    public String message;
}
