package com.example.labyrinthapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The SQLiteHandler class is responsible for handling database operations for the leaderboard.
 */
public class SQLiteHandler extends SQLiteOpenHelper {

    public static final String LEADERBOARD_TABLE = "LEADERBOARD_TABLE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_PLAYER_NAME = "PLAYER_NAME";
    public static final String COLUMN_LEVEL = "LEVEL";
    public static final String COLUMN_TIME = "TIME";

    /**
     * Constructs a new instance of the SQLiteHandler class.
     *
     * @param context The context of the application.
     */
    public SQLiteHandler(@Nullable Context context) {
        super(context, "leaderboard.db", null, 1);
    }

    /**
     * Called when the database is created for the first time.
     *
     * @param sqLiteDatabase The SQLite database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableStatement = "CREATE TABLE " + LEADERBOARD_TABLE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_PLAYER_NAME + " TEXT, " + COLUMN_LEVEL + " INT, " + COLUMN_TIME + " INT)";

        sqLiteDatabase.execSQL(createTableStatement);
    }

    /**
     * Called when the database needs to be upgraded.
     *
     * @param sqLiteDatabase The SQLite database.
     * @param oldVersion     The old version of the database.
     * @param newVersion     The new version of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }

    /**
     * Adds a player to the leaderboard table.
     *
     * @param name  The name of the player.
     * @param level The level reached by the player.
     * @param time  The completion time of the player.
     * @return true if the player was successfully added, false otherwise.
     */
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

    /**
     * Retrieves the top 10 players from the leaderboard table.
     *
     * @return A list of Player objects representing the top 10 players in order.
     */
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

    /**
     * Gets the position of a given player in the leaderboard.
     *
     * @param player The player object.
     * @return The position of the player in the leaderboard.
     */
    public int getPlayerPosition(Player player) {
        SQLiteDatabase db = this.getReadableDatabase();

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
