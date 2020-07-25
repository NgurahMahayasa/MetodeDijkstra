package com.example.metodedjikstra;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class DataGetFirebase {
    FirebaseDatabase database;
    DatabaseReference dNode;

    String namaSession;
    ModelData modelData;

    DataFirebase dataFirebase;
    GoogleMap mMap;
    SQLiteDatabase db;

    private int jmlNode = 0;

    private ArrayList<ModelTutup> id_tutup;
    private ArrayList<String> route;


    public DataGetFirebase(final SQLiteDatabase db, String nama, String idSession, GoogleMap mMap) {
        this.namaSession = nama;
        database = FirebaseDatabase.getInstance();
        dNode = database.getReference(namaSession);

        dataFirebase = new DataFirebase(idSession);
        this.mMap = mMap;
        this.db = db;

        id_tutup = new ArrayList<>();
        route = new ArrayList<>();
    }

    public void getDataFirebase(){
//        id_tutup.clear();
//        route.clear();
        dNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    switch (dataSnapshot.getKey()){
                        case "m_route":
                            for (DataSnapshot r: dataSnapshot.getChildren()){
                                ModelRoute modelRoute = r.getValue(ModelRoute.class);

                                String id = r.getKey();

                                db.execSQL("INSERT INTO m_route (id,route) " + "VALUES ('"+id+"','"+modelRoute.getRoute()+"')");
                                dataFirebase.InsertRoute(id,modelRoute);
                            }
                            break;
                        case "m_node":
                            for (DataSnapshot r: dataSnapshot.getChildren()){
                                ModelNode modelNode = r.getValue(ModelNode.class);

                                String id = r.getKey();

                                db.execSQL("INSERT INTO m_node (id,nama_node,lat,lng) " + "VALUES ('"+id+"','"+modelNode.getNama_node()+"','"+modelNode.getLat()+"','"+modelNode.getLng()+"')");
                                dataFirebase.InsertNode(id,modelNode);
                            }
                            break;

                        case "m_line":
                            for (DataSnapshot r: dataSnapshot.getChildren()){
                                ModelLine modelLine = r.getValue(ModelLine.class);

                                String id = r.getKey();

                                db.execSQL("INSERT INTO m_line (id,id_node_awal,id_node_akhir,p_latlng,jarak) " + "VALUES ('"+id+"','"+modelLine.getId_node_awal()+"','"+modelLine.getId_node_akhir()+"','"+modelLine.getP_latlng()+"','"+modelLine.getJarak()+"')");
                                dataFirebase.InsertLine(id,modelLine);
                            }
                            break;

                        case "m_point":
                            for (DataSnapshot r: dataSnapshot.getChildren()){
                                ModelPoint modelPoint = r.getValue(ModelPoint.class);

                                String id = r.getKey();

                                dataFirebase.InsertPoint(id,modelPoint);
                                route.add(modelPoint.getData());
                            }
                            break;
                        case "m_tutup":
                            for (DataSnapshot r: dataSnapshot.getChildren()){
                                ModelTutup modelTutup = r.getValue(ModelTutup.class);

                                String id = r.getKey();

                                dataFirebase.InsertTutup(id,modelTutup);
                                id_tutup.add(modelTutup);
                            }
                            break;

                    }
                }

                getData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getData(){
        mMap.clear();

        //Node ====================
        Cursor cursor_n = db.rawQuery("SELECT * FROM m_node",null);
        cursor_n.moveToFirst();

        for (int i = 0; i < cursor_n.getCount(); i++){
            cursor_n.moveToPosition(i);
            LatLng data_lat = new LatLng(cursor_n.getDouble(2),cursor_n.getDouble(3));

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(data_lat);

            markerOptions.title(cursor_n.getString(1));
            markerOptions.draggable(true);

            mMap.addMarker(markerOptions);
        }
        jmlNode = cursor_n.getCount();

        //Line ====================
        Cursor cursor_l = db.rawQuery("SELECT * FROM m_line",null);
        cursor_l.moveToFirst();

        List<LatLng> getLine = new ArrayList<>();

        for (int i = 0; i < cursor_l.getCount(); i++){
            cursor_l.moveToPosition(i);
            getLine = PolyUtil.decode(cursor_l.getString(3));

            PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true).zIndex(1);

            options.addAll(getLine);

            mMap.addPolyline(options);

            Log.d("Line", cursor_l.getString(4));
        }

        //Route ====================
        cursor_l = db.rawQuery("SELECT * FROM m_route",null);
        cursor_l.moveToFirst();

        for (int i = 0; i < cursor_l.getCount(); i++){
            cursor_l.moveToPosition(i);
            Log.d("Route", cursor_l.getString(1));
        }

        for (ModelTutup r:id_tutup){
            PolylineOptions options = new PolylineOptions().width(15).color(Color.RED).geodesic(true).zIndex(2);
            List<LatLng> p_listline = new ArrayList<LatLng>();

            p_listline = PolyUtil.decode(r.getLine());
            options.addAll(p_listline);

            mMap.addPolyline(options);
        }

        for (String r:route){
            PolylineOptions optionsGreen = new PolylineOptions().width(15).color(Color.GREEN).geodesic(true).zIndex(5);
            List<LatLng> p_listline = new ArrayList<LatLng>();

            p_listline = PolyUtil.decode(r);
            optionsGreen.addAll(p_listline);

            mMap.addPolyline(optionsGreen);
        }

    }

    public ArrayList<ModelTutup> getId_tutup() {
        return id_tutup;
    }

    public ArrayList<String> getRoute() {
        return route;
    }
}
