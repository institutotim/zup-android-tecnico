package com.particity.zuptecnico.entities;

import android.content.Context;
import android.widget.ImageView;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.particity.zuptecnico.R;
import com.particity.zuptecnico.ui.WebImageView;
import com.particity.zuptecnico.util.Utilities;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by igorlira on 7/18/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportCategory {
    public static class IconSet {
        public IconType retina;

        @JsonProperty("default")
        public IconType _default;
    }

    public static class IconType {
        public Icon web;
        public Icon mobile;
    }

    public static class Icon {
        public String active;
        public String disabled;
    }

    public static class MarkerSet {
        public Marker retina;
        @JsonProperty("default")
        public Marker _default;
    }

    public static class Marker {
        public String web;
        public String mobile;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomField {
        public int id;
        public String title;
        public boolean multiline;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private int id;
        private String title;
        private String color;
        private boolean initial;

        @JsonProperty("final")
        private boolean _final;
        private boolean active;

        @JsonProperty("private")
        private boolean _private;

        private Flow flow;

        @JsonProperty("responsible_group_id")
        private Integer responsibleGroupId;

        @JsonGetter("responsible_group_id")
        public Integer getResponsibleGroupId() {
            return responsibleGroupId;
        }

        @JsonSetter("responsible_group_id")
        public void setResponsibleGroupId(Integer responsibleGroupId) {
            this.responsibleGroupId = responsibleGroupId;
        }

        @JsonGetter("flow")
        public Flow getFlow() {
            return flow;
        }

        @JsonSetter("flow")
        public void setFlow(Flow flow) {
            this.flow = flow;
        }

        @JsonIgnore
        public int getUiColor() {
            return Utilities.getColorFromHex(getColor());
        }

        @JsonGetter("id")
        public int getId() {
            return id;
        }

        @JsonSetter("id")
        public void setId(int id) {
            this.id = id;
        }

        @JsonGetter("title")
        public String getTitle() {
            return title;
        }

        @JsonSetter("title")
        public void setTitle(String title) {
            this.title = title;
        }

        @JsonGetter("color")
        public String getColor() {
            return color;
        }

        @JsonSetter("color")
        public void setColor(String color) {
            this.color = color;
        }

        @JsonGetter("initial")
        public boolean isInitial() {
            return initial;
        }

        @JsonSetter("initial")
        public void setInitial(boolean initial) {
            this.initial = initial;
        }

        @JsonGetter("final")
        public boolean isFinal() {
            return _final;
        }

        @JsonSetter("final")
        public void setFinal(boolean _final) {
            this._final = _final;
        }

        @JsonGetter("active")
        public boolean isActive() {
            return active;
        }

        @JsonSetter("active")
        public void setActive(boolean active) {
            this.active = active;
        }

        @JsonGetter("private")
        public boolean isPrivate() {
            return _private;
        }

        @JsonSetter("private")
        public void setPrivate(boolean _private) {
            this._private = _private;
        }

        @Override
        public int hashCode() {
            return getId();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Status) {
                if (((Status) o).getId() == getId()) {
                    return true;
                }
                return false;
            }
            return false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InventoryCategory {
        public int id;
    }

    public int id;
    public String title;
    public IconSet icon;
    public MarkerSet marker;
    public String color;
    public boolean resolution_time_enabled;
    public Integer resolution_time;
    public boolean private_resolution_time;
    public Integer user_response_time;
    public boolean allows_arbitrary_position;
    public Integer parent_id;
    public int[] solver_groups_ids;
    public Status[] statuses;
    public boolean confidential;
    public boolean comment_required_when_updating_status;
    public boolean comment_required_when_forwarding;
    public boolean active;
    public CustomField[] custom_fields;

    public InventoryCategory[] inventory_categories;

    public ReportCategory[] subcategories;
    public Integer[] subcategories_ids;

    public Status getStatus(int id) {
        for (int i = 0; i < this.statuses.length; i++) {
            if (this.statuses[i].getId() == id)
                return this.statuses[i];
        }

        return null;
    }

    public String getMarkerURL() {
        if (marker._default != null && marker._default.mobile != null)
            return marker._default.mobile;
        else if (marker._default != null && marker._default.web != null)
            return marker._default.web;
        else if (marker.retina != null && marker.retina.mobile != null)
            return marker.retina.mobile;
        else
            return marker.retina.web;
    }

    private String getIconUrl() {
        return icon.retina.mobile.active;
    }

    public void saveImageIntoCache(Context context) {
        Picasso.with(context)
                .load(getIconUrl())
                .fetch();
    }

    public void loadImageInto(ImageView imageView) {
        Picasso.with(imageView.getContext())
                .load(getIconUrl())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.report_cat_placeholder)
                .into(imageView);
    }

    public void saveMarkerIntoCache(Context context) {
        Picasso.with(context)
                .load(getMarkerURL())
                .fetch();
    }

    public void loadMarkerInto(WebImageView imageView) {
        Picasso.with(imageView.getContext())
                .load(getMarkerURL())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.report_cat_placeholder)
                .into(imageView);
    }

    @JsonIgnore
    public int getUiColor() {
        return Utilities.getColorFromHex(color);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ReportCategory) {
            if (((ReportCategory) o).id == id) {
                return true;
            }
            return false;
        }
        return false;
    }
}
