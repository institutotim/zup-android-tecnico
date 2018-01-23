package com.particity.zuptecnico.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.particity.zuptecnico.R;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.Case;
import com.particity.zuptecnico.entities.Flow;

import java.util.ArrayList;
import java.util.List;

public class CaseStepsAdapter extends BaseAdapter {
    Case flowCase;
    Context context;
    List<Flow.Step> items;
    SparseArray<View> viewCache;
    View loadingView;
    ObjectMapper mapper;
    OnCaseStepClickListener listener;

    boolean areMoreItemsAvailable;

    public interface OnCaseStepClickListener {
        void onCaseStepClickListener(Case theCase, Flow.Step step);
    }

    public CaseStepsAdapter(Context context, Case item, Flow.Step[] steps) {
        flowCase = item;
        this.context = context;
        this.viewCache = new SparseArray<>();
        this.items = new ArrayList<>();
        this.mapper = new ObjectMapper();
        if (steps != null) {
            for (int i = 0; i < steps.length; i++) {
                this.items.add(steps[i]);
            }
            this.areMoreItemsAvailable = false;
        }
    }

    public void setListener(OnCaseStepClickListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        if (this.areMoreItemsAvailable) {
            return items.size() + 1;
        } else {
            return items.size();
        }
    }

    @Override
    public Flow.Step getItem(int i) {
        if (areMoreItemsAvailable && getLoadingItemIndex() == i)
            return null;
        else
            return items.get(i);
    }

    int getLoadingItemIndex() {
        return this.items.size();
    }

    @Override
    public long getItemId(int i) {
        if (getItem(i) == null)
            return -1;

        return getItem(i).id;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        if (areMoreItemsAvailable && i == getLoadingItemIndex()) {
            if (loadingView != null)
                return loadingView;
            else {
                LayoutInflater inflater = LayoutInflater.from(context);
                loadingView = inflater.inflate(R.layout.listview_loadingmore_small, viewGroup, false);

                return loadingView;
            }
        } else {
            Flow.Step item = getItem(i);

            if (viewCache.get(item.id) != null)
                return viewCache.get(item.id);
            else {
                LayoutInflater inflater = LayoutInflater.from(context);
                View view = inflater.inflate(R.layout.case_step_item, viewGroup, false);
                fillData(view, flowCase, item);
                return view;
            }
        }
    }

    void fillData(View view, final Case item, final Flow.Step step) {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.initialFlow != null && Zup.getInstance().getAccess().canViewStep(step.id, item.initialFlow.id)) {
                    if (listener != null) {
                        listener.onCaseStepClickListener(item, step);
                    }
                }
            }
        };
        ImageView icon = (ImageView) view.findViewById(R.id.case_step_item_icon);
        TextView name = (TextView) view.findViewById(R.id.case_step_item_name);
        TextView owner = (TextView) view.findViewById(R.id.case_step_item_owner);
        TextView status = (TextView) view.findViewById(R.id.case_step_item_status);

        String nameText = step.title;
        if (step.stepType.equals("flow")) {
            icon.setImageResource(R.drawable.ic_casos_fluxo);
            nameText = nameText.concat(" (" + context.getString(R.string.flow) + ")");
        } else {
            icon.setImageResource(R.drawable.ic_casos_formulario);
            nameText = nameText.concat(" (" + context.getString(R.string.form) + ")");
        }
        name.setText(nameText);
        Case.Step caseStep = item.getStep(step.id);
        if (caseStep != null && caseStep.hasResponsableUser()) {
            String text = context.getString(R.string.case_step_responsable_title) + " " + caseStep.responsableUser.name;
            owner.setText(text);
        } else if (caseStep != null && caseStep.hasResponsibleGroup()) {
            String text = context.getString(R.string.case_step_responsable_title) + " " + caseStep.responsibleGroup.getName();
            owner.setText(text);
        } else {
            owner.setText(R.string.case_step_no_responsable);
        }

        String statusT;
        if (item.getStatus().equals("finished")) {
            statusT = context.getString(R.string.done);
            view.setAlpha(0.3f);
            view.setOnClickListener(clickListener);
        } else if (item.isAfterCurrentStep(step.id)) {
            statusT = context.getString(R.string.not_started);
            view.setAlpha(0.6f);
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.case_item_cell_notselected));
        } else if (item.isCurrentStep(step.id)) {
            statusT = context.getString(R.string.in_execution);
            view.setAlpha(1.0f);
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.case_item_cell_notselected_new));
            view.setOnClickListener(clickListener);
        } else { //item.getStatus().equals("finished")
            statusT = context.getString(R.string.done);
            view.setAlpha(0.3f);
            view.setOnClickListener(clickListener);
        }
        status.setText(statusT);
    }
}