package rasterizers;

import models.Circle;
import models.Line;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Line> lines = new ArrayList<>();
    private List<Circle> circles = new ArrayList<>();

    public List<Line> getLines() { return lines; }
    public List<Circle> getCircles() { return circles; }

    public void addLines(List<Line> lines) { this.lines.addAll(lines); }
    public void removeLine(Line line) { lines.remove(line); }
    public void addCircle(Circle circle) { circles.add(circle); }

    public void clear() {
        lines.clear();
        circles.clear();
    }
}