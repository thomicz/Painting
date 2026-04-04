package rasterizers;

import models.Shape;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private final List<Shape> shapes = new ArrayList<>();

    public void addShape(Shape shape) {
        shapes.add(shape); // TOTO TADY CHYBĚLO! :)
    }

    public void removeShape(Shape shape) {
        shapes.remove(shape);
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public void clearShapes() {
        shapes.clear();
    }
}