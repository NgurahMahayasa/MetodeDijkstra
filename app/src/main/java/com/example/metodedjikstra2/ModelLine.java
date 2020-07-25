package com.example.metodedjikstra2;

public class ModelLine {
    private String id_node_awal;
    private String id_node_akhir;
    private String jarak;
    private String p_latlng;

    public ModelLine() {
        super();
    }

    public ModelLine(String id_node_awal, String id_node_akhir, String jarak, String p_latlng) {
        this.id_node_awal = id_node_awal;
        this.id_node_akhir = id_node_akhir;
        this.jarak = jarak;
        this.p_latlng = p_latlng;
    }

    public String getId_node_awal() {
        return id_node_awal;
    }

    public String getId_node_akhir() {
        return id_node_akhir;
    }

    public String getJarak() {
        return jarak;
    }

    public String getP_latlng() {
        return p_latlng;
    }
}
