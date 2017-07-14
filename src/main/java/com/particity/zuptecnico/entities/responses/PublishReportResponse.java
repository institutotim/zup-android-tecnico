package com.ntxdev.zuptecnico.entities.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ntxdev.zuptecnico.entities.ReportItem;

/**
 * Created by Renan on 06/01/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishReportResponse {
    public String message;
    public ReportItem item;
    public Object error;
}
