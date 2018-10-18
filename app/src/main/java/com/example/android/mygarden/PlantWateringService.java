package com.example.android.mygarden;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;

//COMPLETED (2): Create a plant watering service that extends IntentService and supports the
//action ACTION_WATER_PLANTS which updates last_watered timestamp for all plants still alive
public class PlantWateringService extends IntentService {
    public static final String ACTION_WATER_PLANTS =
            "com.example.android.mygarden.action_water_plants";

    public PlantWateringService() {
        super(PlantWateringService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_WATER_PLANTS.equals(action)) {
                handleActionWaterPlants();
            }
        }
    }

    private void handleActionWaterPlants() {
        Uri PLANTS_URI = PlantContract.PlantEntry.CONTENT_URI;

        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);

        // Update only plants that are still alive
        String where = PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME + ">?";
        String[] whereArgs = {String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)};

        getContentResolver().update(
                PLANTS_URI,
                contentValues,
                where,
                whereArgs);
    }

    public static void startActionWaterPlants(Context context) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
        context.startService(intent);
    }
}
