package com.example.metodedjikstra;

public class ModelLine {
    private Integer id;
    private Integer id_node_awal;
    private Integer id_node_akhir;
    private String jarak;
    private String p_latlng;

    public ModelLine() {
        super();
    }

    public ModelLine(Integer id, Integer id_node_awal, Integer id_node_akhir, String jarak, String p_latlng) {
        this.id = id;
        this.id_node_awal = id_node_awal;
        this.id_node_akhir = id_node_akhir;
        this.jarak = jarak;
        this.p_latlng = p_latlng;
    }

    public Integer getId() {
        return id;
    }

    public Integer getId_node_awal() {
        return id_node_awal;
    }

    public Integer getId_node_akhir() {
        return id_node_akhir;
    }

    public String getJarak() {
        return jarak;
    }

    public String getP_latlng() {
        return p_latlng;
    }
}
