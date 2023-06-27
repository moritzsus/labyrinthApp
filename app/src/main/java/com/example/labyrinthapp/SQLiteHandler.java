package com.example.labyrinthapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class SQLiteHandler extends SQLiteOpenHelper {

    public static final String LEADERBOARD_TABLE = "LEADERBOARD_TABLE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_PLAYER_NAME = "PLAYER_NAME";
    public static final String COLUMN_LEVEL = "LEVEL";
    public static final String COLUMN_TIME = "TIME";

    public SQLiteHandler(@Nullable Context context) {
        super(context, "leaderboard.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableStatement = "CREATE TABLE " + LEADERBOARD_TABLE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_PLAYER_NAME + " TEXT, " + COLUMN_LEVEL + " INT, " + COLUMN_TIME + " INT)";

        sqLiteDatabase.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }


    public boolean addPlayer(String name, int level, int time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_PLAYER_NAME, name);
        cv.put(COLUMN_LEVEL, level);
        cv.put(COLUMN_TIME, time);

        long insert = db.insert(LEADERBOARD_TABLE,  null, cv);
        db.close();

        if(insert == -1)
            return  false;

        return true;
    }

    public List<Player> getTop10Players() {
        List<Player> topPlayers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                LEADERBOARD_TABLE,
                new String[] {COLUMN_PLAYER_NAME, COLUMN_LEVEL, COLUMN_TIME},
                null,
                null,
                null,
                null,
                COLUMN_LEVEL + " DESC, " + COLUMN_TIME + " ASC",
                "10"
        );

        if(cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_NAME));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LEVEL));
                int time = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIME));

                Player player = new Player(name, level, time);
                topPlayers.add(player);
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return topPlayers;
    }

    public int getPlayerPosition(Player player) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Erstelle eine Abfrage, um die Position des Spielers zu ermitteln
        String query = "SELECT COUNT(*) + 1 AS position " +
                "FROM " + LEADERBOARD_TABLE + " " +
                "WHERE " + COLUMN_LEVEL + " > ? OR (" + COLUMN_LEVEL + " = ? AND " + COLUMN_TIME + " < ?)";

        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(player.getLevel()), String.valueOf(player.getLevel()), String.valueOf(player.getTime())});

        int position = 0;
        if (cursor.moveToFirst()) {
            position = cursor.getInt(cursor.getColumnIndexOrThrow("position"));
        }

        cursor.close();
        db.close();

        return position;
    }
}
