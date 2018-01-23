package com.lfdb.zuptecnico.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.ReportHistoryItem;
import com.lfdb.zuptecnico.entities.ReportItem;
import com.lfdb.zuptecnico.entities.collections.ReportHistoryItemCollection;
import com.lfdb.zuptecnico.entities.collections.SingleReportItemCollection;
import com.squareup.picasso.Picasso;

/**
 * Created by Igor on 8/3/2015.
 */
public class ReportItemDownloader extends AsyncTask<Void, Void, Boolean> {
    public interface Listener {
        void onProgress(float progress);
        void onFinished();
        void onError();
    }

    private static final float PROGRESS_ITEM = 0.25f;
    private static final float PROGRESS_HISTORY = 0.25f;
    private static final float PROGRESS_IMAGES = 0.5f;

    private int itemId;
    private Listener listener;

    private boolean itemDownloaded;
    private boolean historyDownloaded;
    private int imagesDownloaded;
    private int totalImageCount;

    private Context mContext;

    private ReportItem mItem;
    private ReportHistoryItem[] mHistory;

    public ReportItemDownloader(Context context, int id, Listener listener) {
        this.mContext = context;
        this.itemId = id;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        // Get item info
        SingleReportItemCollection result = Zup.getInstance().getService()
                .retrieveReportItem(this.itemId);

        if(result == null || result.report == null) {
            return false;
        }

        this.itemDownloaded = true;
        this.totalImageCount = result.report.images.length;
        this.imagesDownloaded = 0;
        this.publishProgress();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Get history
        ReportHistoryItemCollection history = Zup.getInstance().getService()
                .retrieveReportItemHistory(this.itemId);

        if(history == null || history.histories == null) {
            return false;
        }

        this.historyDownloaded = true;
        this.publishProgress();

        // Download images
        for(int i = 0; i < result.report.images.length; i++) {
            ReportItem.Image image = result.report.images[i];
            Picasso.with(mContext).load(image.thumb).fetch();
            Picasso.with(mContext).load(image.high).fetch();
            Picasso.with(mContext).load(image.original).fetch();

            this.imagesDownloaded++;
            this.publishProgress();
        }

        // Store downloaded data
        mItem = result.report;
        mHistory = history.histories;

        return true;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

        float progress = 0;
        if(this.itemDownloaded)
            progress += PROGRESS_ITEM;

        if(this.historyDownloaded)
            progress += PROGRESS_HISTORY;

        if(this.totalImageCount > 0) {
            progress += PROGRESS_IMAGES * (float)this.imagesDownloaded / (float)this.totalImageCount;
        }

        if(this.listener != null)
            this.listener.onProgress(progress);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if(success) {
            Zup.getInstance().getReportItemService().addReportItem(mItem);
            Zup.getInstance().getReportItemService().setReportItemHistory(itemId, mHistory);

            if(listener != null) {
                listener.onProgress(1.0f);
                listener.onFinished();
            }
        }
        else {
            if(listener != null) {
                listener.onError();
            }
        }
    }
}
