package com.example.metodedjikstra;

public class ModelRoute {
    private Integer id;
    private String route;

    public ModelRoute() {
        super();
    }

    public ModelRoute(Integer id, String route) {
        this.id = id;
        this.route = route;
    }

    public Integer getId() {
        return id;
    }

    public String getRoute() {
        return route;
    }
}
