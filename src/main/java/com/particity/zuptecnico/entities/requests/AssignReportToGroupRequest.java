package com.particity.zuptecnico.entities.requests;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Created by Renan on 21/10/2015.
 */
public class AssignReportToGroupRequest {
    @JsonProperty("group_id")
    private int group_id;

    private String comment;
    
    @JsonGetter("group_id")
    public int getGroupId() {
        return group_id;
    }

    @JsonSetter("group_id")
    public void setGroupId(int groupId) {
        this.group_id = groupId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
