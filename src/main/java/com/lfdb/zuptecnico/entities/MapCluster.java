package com.lfdb.zuptecnico.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by igorlira on 5/8/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapCluster
{
    public float[] position;
    public Integer category_id;
    public int count;
}
