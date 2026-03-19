package models;

import java.awt.*;

public class Line {

    private final Point p1;
    private Point p2;

    private Color color;

    private boolean isDotted = false;
    private boolean correctionMode = false;
    private int strokeWidth = 1;

    public Line(Point p1, Point p2, Color color, boolean isDotted, boolean correctionMode) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
        this.isDotted = isDotted;
        this.correctionMode = correctionMode;
        this.strokeWidth = 1;
    }

    public Line(Point p1, Point p2, Color color, boolean isDotted, boolean correctionMode, int strokeWidth) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
        this.isDotted = isDotted;
        this.correctionMode = correctionMode;
        this.strokeWidth = strokeWidth;
    }

    public Point getP1() { return p1; }
    public Point getP2() { return p2; }
    public void SetP2(Point p2) { this.p2 = p2; }
    public void setColor(Color color) { this.color = color; }
    public Color getColor() { return color; }
    public boolean isDotted() { return isDotted; }
    public boolean isCorrectionMode() { return correctionMode; }
    public int getStrokeWidth() { return strokeWidth; }

    public void toggleDotted() { this.isDotted = !this.isDotted; }
    public void toggleCorrectionMode() { this.correctionMode = !this.correctionMode; }
}