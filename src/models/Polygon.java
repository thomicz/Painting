package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Polygon {
    private final List<Point> points;

    public Polygon() {
        this.points = new ArrayList<>();
    }

    public Polygon(Point point) {
        this.points = new ArrayList<>(Collections.singletonList(point));
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public List<Point> getPoints() {
        return new ArrayList<>(points);
    }
}
