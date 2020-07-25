package com.example.metodedjikstra2;

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.*;
import android.widget.Button;
import android.view.*;
import android.widget.EditText;
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
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Polyline currentPolyline;
    private DataHelper dataHelper;

    //Session untuk menyimpan data
    final String NAMASESSION = "NamaMap";
    String namaSession;

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    Button btnNode, btnLine, btnRoute, btnClear, btnLineTutup, btnTracking, btnRefresh,btnShare;
    TextView tvProses;
    String tipe = "";

    EditText txtIDShare,txtGetShare;
    Button dialogGetData;

    ArrayList<LatLng> listLine;
    ArrayList storeLine;

    boolean lineDraw = false, routeDraw = false;

    int jmlNode = 0;
    int node_awal, node_akhir, route_awal, route_akhir, tutup_awal = 0, tutup_akhir = 0;

    Double loc_lat = -8.6551843;
    Double loc_long = 115.2159034;

    ArrayList<String> route;
    ArrayList<String> id_tutup;
    ArrayList<String> id_tutup_line;

    DataFirebase dataFirebase;
    DataGetFirebase dataGetFirebase;

    AlertDialog dialog;
    LayoutInflater inflater;
    View dialogView;

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

        sp = getSharedPreferences("MySession", MODE_PRIVATE);
        Log.d("MySession", sp.getString(NAMASESSION, ""));

        if (sp.getString(NAMASESSION, "") == "") {
            String currentDateTimeString = android.text.format.DateFormat.format("yyyMMddhhmmss", new Date()).toString();
            spEditor = sp.edit();
            spEditor.putString(NAMASESSION, currentDateTimeString);
            spEditor.commit();

            Log.d("Session", currentDateTimeString);
        } else {
            Log.d("Session Ada", sp.getString(NAMASESSION, ""));
        }

        namaSession = sp.getString(NAMASESSION, "");

        dataFirebase = new DataFirebase(namaSession);

        btnNode = (Button) findViewById(R.id.btn_node);
        btnLine = (Button) findViewById(R.id.btn_line);
        btnRoute = (Button) findViewById(R.id.btn_route);
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnLineTutup = (Button) findViewById(R.id.btn_tutup);
        btnTracking = (Button) findViewById(R.id.btn_tracking);
        btnRefresh = (Button) findViewById(R.id.btn_refresh);
        btnShare = (Button) findViewById(R.id.btn_share);
        tvProses = (TextView) findViewById(R.id.tv_proses);

        listLine = new ArrayList<LatLng>();
        storeLine = new ArrayList();
        route = new ArrayList<String>();
        id_tutup = new ArrayList<String>();
        id_tutup_line = new ArrayList<>();

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

                if (routeDraw) {
                    routeDraw = false;
                    btnRoute.setText("Add Route");

                    Log.d("Route", String.valueOf(route.size()));
                    addRoute();
                    route = new ArrayList<String>();
                } else {
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

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogShare();
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

        dataFirebase.RefreshTracking();
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

                            Cursor cursor = db.rawQuery("SELECT * FROM m_line",null);
                            cursor.moveToFirst();

                            for (int i = 0; i < cursor.getCount(); i++){
                                cursor.moveToPosition(i);
                                String id = cursor.getString(0);
                                String id_node_awal = cursor.getString(1);
                                String id_node_akhir = cursor.getString(2);
                                String dp_latlng = cursor.getString(3);
                                String djarak = cursor.getString(4);
                                ModelLine modelLine = new ModelLine(id_node_awal,id_node_akhir,djarak,dp_latlng);

                                dataFirebase.InsertLine(id,modelLine);
                            }

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

            dataFirebase.InsertNode(id,modelNode);
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

        Cursor cursor = db.rawQuery("SELECT * FROM m_route",null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            String id = cursor.getString(0);
            String route = cursor.getString(1);
            ModelRoute modelRoute = new ModelRoute(route);

            dataFirebase.InsertRoute(id,modelRoute);
        }

        Toast toast = Toast.makeText(getApplicationContext(), "Route Telah Disimpan! Route :"+route, Toast.LENGTH_LONG);
        toast.show();
    }

    private void tutupLine(){
        SQLiteDatabase db = dataHelper.getReadableDatabase();

        String where = "(id_node_awal ="+tutup_awal+" AND id_node_akhir = "+tutup_akhir+") " +
                "OR (id_node_awal ="+tutup_akhir+" AND id_node_akhir = "+tutup_awal+") ";
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
            id_tutup_line.add(cursor.getString(3));

//            id_tutup.add(cursor.getString(3));

            for(Integer x = 0;x<id_tutup.size();x++){
                ModelTutup modelTutup = new ModelTutup(id_tutup.get(x),id_tutup_line.get(x));
                dataFirebase.InsertTutup(x.toString(),modelTutup);
            }

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

        id_tutup.clear();
        id_tutup_line.clear();
        route.clear();

        dataFirebase.RemoveAll();
    }

    private void getData(){
        Log.d("GetData", "getData: ");
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

            Integer x = 0;
            for(String p: points.get(route_pendek)){

                ModelPoint modelPoint = new ModelPoint(p);
                dataFirebase.InsertPoint(x.toString(),modelPoint);

                List<LatLng> getLine = PolyUtil.decode(p);

                PolylineOptions options = new PolylineOptions().width(15).color(Color.GREEN).geodesic(true).zIndex(5);

                options.addAll(getLine);


                mMap.addPolyline(options);
                x++;
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
        id_tutup_line.clear();

        dataFirebase.RefreshTracking();

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

    private void dialogShare(){
        dialog = new AlertDialog.Builder(MapsActivity.this).create();
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_share,null);
        dialog.setView(dialogView);
        dialog.setTitle("Share Tracking");

        txtIDShare = (EditText) dialogView.findViewById(R.id.editTextShare);
        txtGetShare = (EditText) dialogView.findViewById(R.id.editTextInputShare);
        dialogGetData = (Button) dialogView.findViewById(R.id.btnGetShareData);

        txtIDShare.setText(namaSession);

        dialogGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = dataHelper.getWritableDatabase();

                db.execSQL("DELETE FROM m_line");
                db.execSQL("DELETE FROM m_node");
                db.execSQL("DELETE FROM m_route");

                dataGetFirebase = new DataGetFirebase(db,txtGetShare.getText().toString(),namaSession,mMap);
                dataGetFirebase.getDataFirebase();

                dialog.dismiss();
                Toast toast = Toast.makeText(getApplicationContext(), "Sharing Route Berhasil!", Toast.LENGTH_LONG);
                toast.show();
            }
        });
        dialog.show();
    }
}
