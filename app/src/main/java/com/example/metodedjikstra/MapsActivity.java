package com.example.metodedjikstra;

import androidx.fragment.app.FragmentActivity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.*;
import android.widget.Button;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Polyline currentPolyline;
    private DataHelper dataHelper;

    //Session untuk menyimpan data
    final String NAMASESSION = "NamaMap";

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    Button btnNode, btnLine, btnRoute, btnClear, btnLineTutup, btnTracking, btnRefresh;
    TextView tvProses;
    String tipe = "";

    ArrayList<LatLng> listLine;
    ArrayList storeLine;

    boolean lineDraw = false, routeDraw = false;

    int jmlNode = 0;
    int node_awal, node_akhir, route_awal, route_akhir, tutup_awal = 0, tutup_akhir = 0;

    Double loc_lat = -8.6551843;
    Double loc_long = 115.2159034;

    ArrayList<String> route;
    ArrayList<String> id_tutup;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            // JSON here
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        setContentView(R.layout.activity_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        sp = getSharedPreferences("MySession",MODE_PRIVATE);
        Log.d("MySession", sp.getString(NAMASESSION,""));

        if(sp.getString(NAMASESSION,"") == ""){
            String currentDateTimeString = android.text.format.DateFormat.format("yyyMMddhhmmss",new Date()).toString();
            spEditor = sp.edit();
            spEditor.putString(NAMASESSION,currentDateTimeString);
            spEditor.commit();

            Log.d("Session", currentDateTimeString);
        }
        else{
            Log.d("Session Ada", sp.getString(NAMASESSION,""));
        }

        btnNode = (Button) findViewById(R.id.btn_node);
        btnLine = (Button) findViewById(R.id.btn_line);
        btnRoute = (Button) findViewById(R.id.btn_route);
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnLineTutup = (Button) findViewById(R.id.btn_tutup);
        btnTracking = (Button) findViewById(R.id.btn_tracking);
        btnRefresh = (Button) findViewById(R.id.btn_refresh);
        tvProses = (TextView) findViewById(R.id.tv_proses);

        listLine = new ArrayList<LatLng>();
        storeLine = new ArrayList();
        route = new ArrayList<String>();
        id_tutup = new ArrayList<String>();

        dataHelper = new DataHelper(getApplicationContext());

        btnNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tipe = "node";
                tvProses.setText("Add Node");
            }
        });

        btnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tipe = "line";
                tvProses.setText("Add Line");
            }
        });

        btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tipe = "route";
                tvProses.setText("Add Route");

                if(routeDraw){
                    routeDraw = false;
                    btnRoute.setText("Add Route");

                    Log.d("Route", String.valueOf(route.size()));
                    addRoute();
                    route = new ArrayList<String>();
                }
                else{
                    routeDraw = true;
                    btnRoute.setText("Save Route");
                    Toast toast = Toast.makeText(getApplicationContext(), "Pilih Route", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllData();
                mMap.clear();
                jmlNode = 0;
            }
        });

        btnLineTutup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tipe = "tutup";
                tvProses.setText("None");

                Toast toast = Toast.makeText(getApplicationContext(), "Pilih Node Line Tutup", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        btnTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTracking();
            }
        });

        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        LatLng lok_awal = new LatLng(loc_lat,loc_long);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lok_awal , 17.0f));

        getData();

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener(){

            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.d("Testing", marker.getPosition().toString());
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(tipe == "node"){
                    marker.showInfoWindow();
                }
                else if(tipe == "line"){
                    addLine(mMap,marker.getPosition());

                    if(!lineDraw){
                        lineDraw = true;
                        node_awal = Integer.parseInt(marker.getTitle());

                        Toast toast = Toast.makeText(getApplicationContext(), "Line Node telah dipilih", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    else{
                        lineDraw = false;
                        node_akhir = Integer.parseInt(marker.getTitle());

                        if(node_awal != node_akhir){
                            Toast toast = Toast.makeText(getApplicationContext(), "Line Node telah selesai", Toast.LENGTH_LONG);
                            toast.show();

                            String poliEn = PolyUtil.encode(listLine);
                            Log.d("EnLine", poliEn);
                            storeLine.add(listLine);

                            String p_latlng = PolyUtil.encode(listLine);

                            int jarak = (int)getJarak(listLine);

                            //insert to sqlite
                            SQLiteDatabase db = dataHelper.getWritableDatabase();
                            db.execSQL("INSERT INTO m_line (id_node_awal,id_node_akhir,p_latlng, jarak) " +
                                    "VALUES ('"+node_awal+"','"+node_akhir+"','"+p_latlng+"','"+jarak+"')");

//                        db = dataHelper.getReadableDatabase();
//                        Cursor cursor = db.rawQuery("SELECT * FROM m_line",null);
//
//                        cursor.moveToFirst();
//                        if(cursor.getCount() > 0){
//                            for (int i = 0; i < cursor.getCount(); i++) {
//                                cursor.moveToPosition(i);
//                                Log.d("Ambil data", cursor.getString(1));
//                                Log.d("Ambil data", cursor.getString(2));
//                                Log.d("Ambil data", cursor.getString(3));
//                            }
//                        }

                            node_awal = 0;
                            node_akhir = 0;
                            listLine = new ArrayList<LatLng>();
                        }
                        else{
                            Toast toast = Toast.makeText(getApplicationContext(), "Anda Tidak Dapat Memilih Node Yang Sama!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }

                }
                else if(tipe == "route"){
                    if(route_awal == 0){
                        route_awal = Integer.parseInt(marker.getTitle());
//                        Log.d("route_awal", String.valueOf(route_awal));
                        Toast toast = Toast.makeText(getApplicationContext(), "Node "+marker.getTitle()+" Route Awal", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    else{
                        route_akhir = Integer.parseInt(marker.getTitle());

                        if(route_awal != route_akhir){
//                            Log.d("route_akhir", String.valueOf(route_akhir));
                            Toast toast = Toast.makeText(getApplicationContext(), "Node "+marker.getTitle()+" Route Akhir", Toast.LENGTH_LONG);
                            toast.show();

                            String s_route = Integer.toString(route_awal)+"-"+Integer.toString(route_akhir);
                            route.add(s_route);

                            route_awal = 0;
                            route_akhir = 0;
                        }
                        else{
                            Toast toast = Toast.makeText(getApplicationContext(), "Node telah anda pilih!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                }
                else if(tipe == "tutup"){
                    if(tutup_awal == 0)
                        tutup_awal = Integer.parseInt(marker.getTitle());
                    else{
                        tutup_akhir = Integer.parseInt(marker.getTitle());

                        if(tutup_awal != tutup_akhir){
                            tutupLine();
                            tutup_awal = 0;
                            tutup_akhir = 0;
                        }
                        else{
                            Toast toast = Toast.makeText(getApplicationContext(), "Node telah anda pilih!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                }
                return true;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(tipe == "node"){
                    addNode(mMap,latLng);
                }
                else if(tipe == "line"){
                    if(lineDraw)
                        addLine(mMap,latLng);
                }
            }
        });

    }

    private void addNode(GoogleMap mMap,LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        jmlNode += 1;
        markerOptions.title(String.valueOf(jmlNode));
        markerOptions.draggable(true);

        mMap.addMarker(markerOptions);
        Log.i("onMapClick", "onMapClick: "+latLng.toString());

        String lat = String.valueOf(latLng.latitude);
        String lng = String.valueOf(latLng.longitude);

        //insert to sqlite
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        db.execSQL("INSERT INTO m_node (nama_node,lat,lng) VALUES ('"+String.valueOf(jmlNode)+"','"+lat+"','"+lng+"')");

        Cursor cursor = db.rawQuery("SELECT * FROM m_node",null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            String id = cursor.getString(0);
            String nama_node = cursor.getString(1);
            String rlat = cursor.getString(2);
            String rlng = cursor.getString(3);
            ModelNode modelNode = new ModelNode(nama_node,rlat,rlng);

            DatabaseReference dNode = database.getReference("anak1");
            dNode.child("m_node").child(id).setValue(modelNode);
//            dNode.removeValue();
        }



        Toast toast = Toast.makeText(getApplicationContext(), "Node "+String.valueOf(jmlNode)+" telah dibuat!", Toast.LENGTH_LONG);
        toast.show();
    }

    private void addLine(GoogleMap mMap,LatLng latLng){
        Log.d("Add Line", latLng.toString());
        PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);

        listLine.add(latLng);
        options.addAll(listLine);

        mMap.addPolyline(options);
    }

    private void addRoute(){
        //insert to sqlite
        String i_route = "";
        for(int i = 0; i < route.size(); i++){
            if(i == 0)
                i_route += route.get(i);
            else
                i_route += ","+route.get(i);
        }

        SQLiteDatabase db = dataHelper.getWritableDatabase();
        db.execSQL("INSERT INTO m_route (route) " +
                "VALUES ('"+i_route+"')");

        Toast toast = Toast.makeText(getApplicationContext(), "Route Telah Disimpan! Route :"+route, Toast.LENGTH_LONG);
        toast.show();
//        db = dataHelper.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM m_route",null);
//
//        cursor.moveToFirst();
//        if(cursor.getCount() > 0){
//            for (int i = 0; i < cursor.getCount(); i++) {
//                cursor.moveToPosition(i);
//                Log.d("Ambil data", cursor.getString(1));
//            }
//        }

    }

    private void tutupLine(){
        SQLiteDatabase db = dataHelper.getReadableDatabase();

        String where = "(id_node_awal ='"+tutup_awal+"' AND id_node_akhir = '"+tutup_akhir+"') " +
                "OR (id_node_awal ='"+tutup_akhir+"' AND id_node_akhir = '"+tutup_awal+"') ";
        Cursor cursor = db.rawQuery("SELECT * FROM m_line WHERE "+where,null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0){
            PolylineOptions options = new PolylineOptions().width(15).color(Color.RED).geodesic(true).zIndex(2);

            List<LatLng> p_listline = new ArrayList<LatLng>();

            p_listline = PolyUtil.decode(cursor.getString(3));
            options.addAll(p_listline);

            mMap.addPolyline(options);

            String get_awal = cursor.getString(1);
            String get_akhir = cursor.getString(2);
            id_tutup.add(get_awal+"-"+get_akhir);

            Toast toast = Toast.makeText(getApplicationContext(), "Line Tutup Telah di Set!", Toast.LENGTH_LONG);
            toast.show();
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(), "Line tidak ditemukan!", Toast.LENGTH_LONG);
            toast.show();
        }

    }

    private void deleteAllData(){
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        db.execSQL("DELETE FROM m_node");
        db.execSQL("DELETE FROM m_line");
        db.execSQL("DELETE FROM m_route");
    }

    private void getData(){
        SQLiteDatabase db = dataHelper.getReadableDatabase();


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
    }

    private void getTracking() {
        String where = "";

        if(id_tutup.size() > 0){
             where = "WHERE ";

            int count = 0;
            for(String tutup : id_tutup){
                Log.d("Tutup", tutup);
                String[] s_tutup = tutup.split("-");
                String tutup_1 = s_tutup[1]+"-"+s_tutup[0];

                Log.d("Tutup", tutup_1);

                if(count == 0)
                    where += "(route NOT LIKE '%"+tutup+"%' AND route NOT LIKE '%"+tutup_1+"%') ";
                else
                    where += "AND (route NOT LIKE '%"+tutup+"%' AND route NOT LIKE '%"+tutup_1+"%') ";

                count++;
            }
        }

        SQLiteDatabase db = dataHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM m_route "+where,null);
        cursor.moveToFirst();

        ArrayList<String[]> routes = new ArrayList<>();

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            String[] get_route = cursor.getString(1).split(",");

            routes.add(get_route);
        }

        ArrayList<Integer> jaraks = new ArrayList<>();
        ArrayList<ArrayList<String>> points = new ArrayList<>();

        int int_route = 0;

        if(routes.size() > 0){
            for (String[] route : routes ){
                Log.d("Point", String.valueOf(int_route));

                int jarak = 0;
                ArrayList<String> point = new ArrayList<>();

                for (String r : route){
                    Log.d("ROute", r);

                    String[] pisah = r.split("-");

                    where = "(id_node_awal ='"+pisah[0]+"' AND id_node_akhir = '"+pisah[1]+"') " +
                            "OR (id_node_awal ='"+pisah[1]+"' AND id_node_akhir = '"+pisah[0]+"') ";
                    Cursor cursor_j = db.rawQuery("SELECT * FROM m_line WHERE "+where,null);
                    cursor_j.moveToFirst();

                    jarak += cursor_j.getInt(4);

                    point.add(cursor_j.getString(3));
                    Log.d("Point", cursor_j.getString(3));
                }

                jaraks.add(jarak);
                points.add(point);

                int_route++;
            }

            int jarak_awal = 0;
            int route_pendek = 0;

            int index = 0;
            for (int j : jaraks){
                Log.d("Jarak", String.valueOf(j));
                if(jarak_awal == 0)
                    jarak_awal = j;
                else{
                    if(j < jarak_awal){
                        jarak_awal = j;
                        route_pendek = index;
                    }
                }
                index++;
            }

            Log.d("Route Terpendek", String.valueOf(route_pendek));

            for(String p: points.get(route_pendek)){

                List<LatLng> getLine = PolyUtil.decode(p);

                PolylineOptions options = new PolylineOptions().width(15).color(Color.GREEN).geodesic(true).zIndex(5);

                options.addAll(getLine);


                mMap.addPolyline(options);
            }
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(), "Route tidak ditemukan! Seluruh jalan tidak dapat dilalui!", Toast.LENGTH_LONG);
            toast.show();
        }

    }

    private void refresh() {
        mMap.clear();
        getData();

        listLine = new ArrayList<LatLng>();
        storeLine = new ArrayList();
        route = new ArrayList<String>();
        id_tutup = new ArrayList<String>();

        node_awal = 0;
        node_akhir = 0;
        route_awal = 0;
        route_akhir = 0;
        tutup_awal = 0;
        tutup_akhir = 0;
    }

    private double getJarak(ArrayList<LatLng> latLngs){
        LatLng latLng_awal = null;
        double jarak = 0;

        for(LatLng latLng: latLngs){
            if(latLng_awal == null){
                latLng_awal = latLng;
            }
            else{
                double pi = 3.14;
                double lat1 = latLng_awal.latitude * pi / 180;
                double lat2 = latLng.latitude * pi / 180;
                double lon1 = latLng_awal.longitude * pi / 180;
                double lon2 = latLng.longitude * pi / 180;

                jarak += Math.acos(Math.sin(lat1) * Math.sin(lat2)
                        + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * 6371000;

                latLng_awal = latLng;
            }

        }

        return jarak;
    }


    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

        HttpURLConnection urlConnection = null;

        URL url = new URL(urlString);

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);

        urlConnection.setDoOutput(true);

        urlConnection.connect();

        BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));

        char[] buffer = new char[1024];

        String jsonString = new String();

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
        }
        br.close();

        jsonString = sb.toString();

        System.out.println("JSON: " + jsonString);
        urlConnection.disconnect();

        return new JSONObject(jsonString);
    }
}
