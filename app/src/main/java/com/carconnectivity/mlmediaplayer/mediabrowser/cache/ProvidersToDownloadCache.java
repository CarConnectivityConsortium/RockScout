/*
 * Copyright Car Connectivity Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You may decide to give the Car Connectivity Consortium input, suggestions
 * or feedback of a technical nature which may be implemented on the
 * Car Connectivity Consortium products (“Feedback”).
 *
 * You agrees that any such Feedback is given on non-confidential
 * basis and Licensee hereby waives any confidentiality restrictions
 * for such Feedback. In addition, Licensee grants to the Car Connectivity Consortium
 * and its affiliates a worldwide, non-exclusive, perpetual, irrevocable,
 * sub-licensable, royalty-free right and license under Licensee’s copyrights to copy,
 * reproduce, modify, create derivative works and directly or indirectly
 * distribute, make available and communicate to public the Feedback
 * in or in connection to any CCC products, software and/or services.
 */

package com.carconnectivity.mlmediaplayer.mediabrowser.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.carconnectivity.mlmediaplayer.mediabrowser.retrofit.model.ProviderToDownloadModel;

import java.util.ArrayList;
import java.util.List;

import static com.carconnectivity.mlmediaplayer.mediabrowser.cache.ProvidersToDownloadCache.ProviderReaderContract.ProviderEntry.TABLE_NAME;

/**
 * @author Comarch S.A.
 */
public class ProvidersToDownloadCache extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "providersCache.db";

    public final class ProviderReaderContract {
        private ProviderReaderContract() {
        }

        public class ProviderEntry implements BaseColumns {
            public static final String TABLE_NAME = "provider";
            public static final String COLUMN_NAME_ID = "id";
            public static final String COLUMN_NAME_LABEL = "label";
            public static final String COLUMN_NAME_ICON_URL = "iconUrl";
        }
    }


    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    ProviderReaderContract.ProviderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    ProviderReaderContract.ProviderEntry.COLUMN_NAME_ID + " TEXT," +
                    ProviderReaderContract.ProviderEntry.COLUMN_NAME_LABEL + " TEXT," +
                    ProviderReaderContract.ProviderEntry.COLUMN_NAME_ICON_URL + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public ProvidersToDownloadCache(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void deleteAllRows() {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_NAME, null, null);

        db.close();
    }

    public void insert(List<ProviderToDownloadModel> providerToDownloads) {
        SQLiteDatabase db = getWritableDatabase();

        for (ProviderToDownloadModel providerToDownload : providerToDownloads
                ) {
            ContentValues values = new ContentValues();

            values.put(ProviderReaderContract.ProviderEntry.COLUMN_NAME_ID, providerToDownload.getId());
            values.put(ProviderReaderContract.ProviderEntry.COLUMN_NAME_LABEL, providerToDownload.getLabel());
            values.put(ProviderReaderContract.ProviderEntry.COLUMN_NAME_ICON_URL, providerToDownload.getIconUrl());

            db.insert(TABLE_NAME, null, values);
        }

        db.close();
    }

    public List<ProviderToDownloadModel> getCachedProviders() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                ProviderReaderContract.ProviderEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<ProviderToDownloadModel> cachedProviders = new ArrayList<>();

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(ProviderReaderContract.ProviderEntry.COLUMN_NAME_ID));
            String label = cursor.getString(cursor.getColumnIndexOrThrow(ProviderReaderContract.ProviderEntry.COLUMN_NAME_LABEL));
            String iconUrl = cursor.getString(cursor.getColumnIndexOrThrow(ProviderReaderContract.ProviderEntry.COLUMN_NAME_ICON_URL));

            ProviderToDownloadModel providerToDownloadModel = new ProviderToDownloadModel();
            providerToDownloadModel.setId(id);
            providerToDownloadModel.setLabel(label);
            providerToDownloadModel.setIconUrl(iconUrl);

            cachedProviders.add(providerToDownloadModel);
        }

        cursor.close();
        db.close();

        return cachedProviders;
    }


}
