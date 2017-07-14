package com.ntxdev.zuptecnico.entities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.ui.WebImageView;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by igorlira on 3/3/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryCategory implements Serializable {
    public int id;
    public String title;
    public String description;
    public String plot_format;
    public Section[] sections;
    public String created_at;
    public Pins pin;
    public Pins marker;
    public boolean require_item_status;
    public String color;
    public InventoryCategoryStatus[] statuses;

    //@JsonIgnore(true)
    //public int defaultPinResourceId;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section implements Serializable, Comparable<Section> {
        public int id;
        public String title;
        public Boolean required;
        public Field[] fields;
        public Integer position = null;
        public boolean disabled;

        @Override
        public int compareTo(Section section) {
            if (position == null && section.position == null) {
                return id - section.id;
            }
            if (section == null || section.position == null) {
                return -1;
            }
            if (position == null) {
                return 1;
            }
            return position.compareTo(section.position);
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Field implements Serializable {
            public int id;
            public String title;
            public String kind;
            public Integer position;
            public String label;
            public String size;
            public boolean disabled;
            public Boolean required;
            public Boolean location;
            //public String[] available_values;
            public Option[] field_options;
            public Integer minimum;
            public Integer maximum;

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Option implements Serializable {
                public int id;
                public String value;
                public boolean disabled;

                @Override
                public String toString() {
                    return value;
                }
            }

            public Option getOption(int id) {
                if (field_options == null)
                    return null;

                for (Option opt : field_options) {
                    if (opt.id == id)
                        return opt;
                }

                return null;
            }

            public Option getOptionWithValue(String value) {
                if (field_options == null)
                    return null;

                for (Option opt : field_options) {
                    if (opt.value.equals(value))
                        return opt;
                }

                return null;
            }
        }

        public boolean isLocationSection() {
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].location != null && fields[i].location == true || fields[i].title.equals("latitude"))
                    return true;
            }

            return false;
        }

        public void sortFields() {
            Arrays.sort(fields, new Comparator<InventoryCategory.Section.Field>() {
                @Override
                public int compare(InventoryCategory.Section.Field section, InventoryCategory.Section.Field section2) {
                    int pos1 = 0;
                    int pos2 = 0;

                    if (section.position != null) {
                        pos1 = section.position;
                    }
                    if (section2.position != null) {
                        pos2 = section2.position;
                    }

                    if (section.position == null)
                        pos1 = pos2;

                    if (section2.position == null)
                        pos2 = pos1;

                    return pos1 - pos2;
                }
            });
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pins implements Serializable {
        public static class Pin implements Serializable {
            public String web;
            public String mobile;
        }

        public Pin retina;
        @JsonProperty("default")
        public Pin _default;
    }

    public Section.Field getNthFieldOfKind(String kind, int number) {
        int foundNumber = 0;

        for (int i = 0; i < sections.length; i++) {
            for (int j = 0; j < sections[i].fields.length; j++) {
                if (sections[i].fields[j].kind != null && sections[i].fields[j].kind.equals(kind)) {
                    foundNumber++;
                    if (foundNumber == number)
                        return sections[i].fields[j];
                }
            }
        }

        return null;
    }

    public Section.Field getField(int id) {
        for (int i = 0; i < sections.length; i++) {
            for (int j = 0; j < sections[i].fields.length; j++) {
                if (sections[i].fields[j].id == id)
                    return sections[i].fields[j];
            }
        }

        return null;
    }

    public Section.Field getField(String name) {
        for (int i = 0; i < sections.length; i++) {
            for (int j = 0; j < sections[i].fields.length; j++) {
                if (sections[i].fields[j].title.equals(name))
                    return sections[i].fields[j];
            }
        }

        return null;
    }

    public InventoryCategoryStatus getStatus(Integer statusId) {
        if (statuses == null || statusId == null) {
            return null;
        }

        int length = statuses.length;
        for (int i = 0; i < length; i++) {
            if (statuses[i].id == statusId) {
                return statuses[i];
            }
        }
        return null;
    }

    public String getMarkerUrl() {
        String icon = getIconUrl();
        if (marker == null || marker._default == null || marker._default.mobile == null) {
            return icon;
        }
        return marker._default.mobile;
    }

    private String getIconUrl() {
        if (pin == null || pin._default == null) {
            return null;
        }
        return pin._default.mobile;
    }

    public void saveImageIntoCache(Context context) {
        String icon = getIconUrl();
        if (icon == null) {
            return;
        }
        Picasso.with(context)
                .load(icon)
                .fetch();
    }

    public void loadImageInto(ImageView imageView) {
        String icon = getIconUrl();
        Picasso.with(imageView.getContext())
                .load(icon)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imageView);
    }

    public void saveMarkerIntoCache(Context context) {
        Picasso.with(context)
                .load(getMarkerUrl())
                .fetch();
    }

    public void loadMarkerInto(WebImageView imageView) {
        Drawable myIcon = ContextCompat.getDrawable(imageView.getContext(),R.drawable.report_cat_placeholder);
        myIcon.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP);

        Picasso.with(imageView.getContext())
                .load(getMarkerUrl())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(myIcon)
                .into(imageView);
    }
}
