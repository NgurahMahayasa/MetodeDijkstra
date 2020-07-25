package com.example.metodedjikstra;

public class ModelNode {
    private String nama_node;
    private String lat;
    private String lng;

    public ModelNode() {

    }

    public ModelNode(String nama_node, String lat, String lng) {
        this.nama_node = nama_node;
        this.lat = lat;
        this.lng = lng;
    }


    public String getNama_node() {
        return nama_node;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }
}
