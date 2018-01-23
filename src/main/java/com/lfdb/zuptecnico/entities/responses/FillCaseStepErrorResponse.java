package com.lfdb.zuptecnico.entities.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by Renan on 30/01/2016.
 */
public class FillCaseStepErrorResponse {
    public String type;
    public Object error;

    public FillCaseStepErrorResponse(){ }

    @JsonIgnore
    public Object getFields(){
        return error;
    }
}
