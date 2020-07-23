package com.example.metodedjikstra;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DataFirebase {
    FirebaseDatabase database;
    DatabaseReference dNode;

    String namaSession;

    public DataFirebase(String nama) {
        namaSession = nama;
        database = FirebaseDatabase.getInstance();
        dNode = database.getReference(namaSession);
    }

    public void InsertLine(String id,ModelLine modelLine){
        dNode.child("m_line").child(id).setValue(modelLine);
    }

    public void InsertNode(String id,ModelNode modelNode){
        dNode.child("m_node").child(id).setValue(modelNode);
    }

    public void InsertRoute(String id,ModelRoute modelRoute){
        dNode.child("m_route").child(id).setValue(modelRoute);
    }

    public void RemoveAll(){
        dNode.removeValue();
    }

    public void InsertTutup(String id,String data){
        dNode.child("m_tutup").child(id).setValue(data);
    }

    public void InsertPoint(String id,String data){
        dNode.child("m_point").child(id).setValue(data);
    }

}
