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

import java.util.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class DBHelper {

	   private Context mContext;
	   private SQLiteDatabase mDB;

	   private SQLiteStatement mInsertFile;
	   private SQLiteStatement mUpdateBookmark;
	   private SQLiteStatement mIncreaseViews;
	   private SQLiteStatement mUpdateLastOpened;
	   
	   private static final String INSERT_FILE = 
		   "INSERT INTO " + 
		   DBOpenHelper.FILES_TABLE + " (" +
		   DBOpenHelper.PATH_COLUMN + ", " +
		   DBOpenHelper.OPENED_COLUMN + ", " +
		   DBOpenHelper.READ_COLUMN + ", " +
		   DBOpenHelper.BOOKMARK_COLUMN + ", " +
		   DBOpenHelper.VIEWS_COLUMN + ", " +
		   DBOpenHelper.FAVORITE_COLUMN + ") values (?, ?, 1, 0, 0, 0)";
	   
	   private static final String UPDATE_BOOKMARK = 
		   "UPDATE " + 
		   DBOpenHelper.FILES_TABLE + " SET " +
		   DBOpenHelper.BOOKMARK_COLUMN + " = ? WHERE " +
		   DBOpenHelper.PATH_COLUMN + " = ?";
	   
	   private static final String INCREASE_VIEWS = 
		   "UPDATE " + 
		   DBOpenHelper.FILES_TABLE + " SET " +
		   DBOpenHelper.VIEWS_COLUMN + " = " + 
		   DBOpenHelper.VIEWS_COLUMN + " + 1 WHERE " +
		   DBOpenHelper.PATH_COLUMN + " = ?";
	   
	   private static final String UPDATE_OPENED =
		"UPDATE " +
			DBOpenHelper.FILES_TABLE + " SET " +
			DBOpenHelper.OPENED_COLUMN + " = ? " +
			"WHERE " +
			DBOpenHelper.PATH_COLUMN + " = ?";
	   
	   public DBHelper(Context context) {
	      mContext = context;
	      final DBOpenHelper openHelper = new DBOpenHelper(mContext);
	      mDB = openHelper.getWritableDatabase();
	      mInsertFile = mDB.compileStatement(INSERT_FILE);
	      mUpdateBookmark = mDB.compileStatement(UPDATE_BOOKMARK);
	      mIncreaseViews = mDB.compileStatement(INCREASE_VIEWS);
	      mUpdateLastOpened = mDB.compileStatement(UPDATE_OPENED);
	   }

	   public long insertFile(String path) {
		   mInsertFile.bindString(1, path);
		   mInsertFile.bindLong(2, new Date().getTime());
	      return mInsertFile.executeInsert();
	   }

	   public void updateFileBookmark(String path, int bookmark) {
		   mUpdateBookmark.bindLong(1, bookmark);
		   mUpdateBookmark.bindString(2, path);
		   mUpdateBookmark.execute();
	   }
	   
	   public void increaseFileViews(String path) {
		   mIncreaseViews.bindString(1, path);
		   mIncreaseViews.execute();
	   }
	   
	   public void updateLastOpened(String path) {
		   mUpdateLastOpened.bindLong(1, new Date().getTime());
		   mUpdateLastOpened.bindString(2, path);
		   mUpdateLastOpened.execute();
	   }
	   
	   public boolean existsFile(String path) {
		   final Cursor cursor = mDB.query(DBOpenHelper.FILES_TABLE, new String[] { DBOpenHelper.PATH_COLUMN }, DBOpenHelper.PATH_COLUMN + " = ? ", new String[] { path }, null, null, null);
		   final int count = cursor.getCount();
		   cursor.close();
		   return count > 0;
	   }
	   
	   public int selectFileBookmark(final String path) {
		   final Cursor cursor = mDB.query(DBOpenHelper.FILES_TABLE, new String[] { DBOpenHelper.BOOKMARK_COLUMN }, DBOpenHelper.PATH_COLUMN + " = ? ", new String[] { path }, null, null, null);
		   if (cursor.moveToFirst()) {
			   int bookmark = cursor.getInt(0);
			   cursor.close();
			   return bookmark;
		   } else {
			   cursor.close();
			   return 0;
		   }
	   }
	   
	   public void deleteFiles() {
		   mDB.delete(DBOpenHelper.FILES_TABLE, null, null);
	   }
	   
	   public String selectMostRecentFile() {
		   Cursor cursor = mDB.query(DBOpenHelper.FILES_TABLE, new String[] { DBOpenHelper.PATH_COLUMN }, 
		  	        null, null, null, null, DBOpenHelper.OPENED_COLUMN + " desc", "1");
		   if (cursor.moveToFirst()) {
			   String file = cursor.getString(0);
			   cursor.close();
			   return file;
		   } else {
			   cursor.close();
			   return null;
		   }
	   }

	   public List<String> getRecentFiles() {
		   ArrayList<String> files = new ArrayList<String>();

		   Cursor cursor = mDB.query(DBOpenHelper.FILES_TABLE, new String[] { DBOpenHelper.PATH_COLUMN }, 
		  	        null, null, null, null, DBOpenHelper.OPENED_COLUMN + " desc", "10");
		   if (cursor.moveToFirst()) {
			   do {
				   String path = cursor.getString(0);
				   files.add(path);
			   } while(cursor.moveToNext());

		   }

		   cursor.close();
		   return files;
	   }
	
}
