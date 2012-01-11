/*******************************************************************************
 * Copyright 2009 Robot Media SL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.robotmedia.acv.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "acv";
    private static final int DATABASE_VERSION = 1;
    public static final String FILES_TABLE = "files";
    public static final String PATH_COLUMN = "path";
    public static final String BOOKMARK_COLUMN = "bookmark";
    public static final String READ_COLUMN = "read";
    public static final String OPENED_COLUMN = "opened";
    public static final String VIEWS_COLUMN = "views";
    public static final String FAVORITE_COLUMN = "favorite";

    private static final String FILES_TABLE_CREATE =
    		"CREATE TABLE " + FILES_TABLE +" ( " +
    		"id INTEGER PRIMARY KEY, " +
    		PATH_COLUMN + " TEXT NOT NULL, " +
    		BOOKMARK_COLUMN + " INTEGER NOT NULL, " +
    		READ_COLUMN + " BOOLEAN NOT NULL, " +
    		OPENED_COLUMN + " DATE NOT NULL," +
    		VIEWS_COLUMN + " INTEGER NOT NULL, " +
    		FAVORITE_COLUMN + " INTEGER NOT NULL)";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FILES_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase  db, int oldVersion, int newVersion) {}

}
