package ru.radmirfar.numgame;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "score.db"; // название БД
    private static final int SCHEMA = 1; // версия базы данных
    static final String TABLE = "score"; // название таблицы
    public static final String COLUMN_THEME = "theme";
    public static final String COLUMN_ALL_NUMS = "all_nums";
    public static final String COLUMN_CORRECT_NUMS = "correct_nums";
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE +
                " ( " + COLUMN_THEME + " TEXT PRIMARY KEY, " + COLUMN_ALL_NUMS +
                " INTEGER, " + COLUMN_CORRECT_NUMS + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(sqLiteDatabase);
    }
}
