package com.example.metodedjikstra;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tranking.db";
    private static final int DATABASE_VERSION = 1;

    public DataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE table m_node (id integer primary key autoincrement, nama_node text null, lat text null, lng text null);";
        db.execSQL(sql);

        sql = "create table m_line (id integer primary key autoincrement, id_node_awal integer, id_node_akhir integer, p_latlng text, jarak integer);";
        db.execSQL(sql);

        sql = "create table m_route (id integer primary key autoincrement, route text);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
