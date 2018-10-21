package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

//    COMPLETED (2): Create a RemoteViewsService class and a RemoteViewsFactory class with:
//        - onDataSetChanged querying the list of all plants in the database
//        - getViewAt creating a RemoteView using the plant_widget layout
//        - getViewAt setting the fillInIntent for widget_plant_image with the plant ID as extras
public class GridWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    Context context;
    Cursor cursor;

    public GridRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        // Get all plant info ordered by creation time
        Uri PLANT_URI = PlantContract.PlantEntry.CONTENT_URI;
        if (cursor != null) {
            cursor.close();
        }
        cursor = context.getContentResolver().query(
                PLANT_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_CREATION_TIME
        );

    }

    @Override
    public void onDestroy() {
        cursor.close();
    }

    @Override
    public int getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        if (cursor == null || cursor.getCount() == 0) return null;

        cursor.moveToPosition(i);
        int idIndex = cursor.getColumnIndex(PlantContract.PlantEntry._ID);
        int plantTypeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
        int createTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        int waterTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);

        long plantId = cursor.getLong(idIndex);
        int plantType = cursor.getInt(plantTypeIndex);
        long createdAt = cursor.getLong(createTimeIndex);
        long wateredAt = cursor.getLong(waterTimeIndex);
        long timeNow = System.currentTimeMillis();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);

        // Update the plant image
        int imageRes = PlantUtils.getPlantImageRes(
                context, timeNow - createdAt, timeNow - wateredAt, plantType);
        views.setImageViewResource(R.id.widget_plant_image, imageRes);
        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));
        // Always hide the water drop in GridView mode
        views.setViewVisibility(R.id.widget_water_button, View.GONE);

        // Fill in the onClick PendingIntent Template using
        // the specific plant Id for each item individually
        Bundle extras = new Bundle();
        extras.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.widget_plant_image, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; // Treat all items in GridView the same
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
