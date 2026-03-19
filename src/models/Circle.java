package models;

import java.awt.Color;

public class Circle {
    private Point center;
    private double radius;
    private Color color;
    private int strokeWidth;

    public Circle(Point center, double radius, Color color) {
        this.center = center;
        this.radius = radius;
        this.color = color;
        this.strokeWidth = 1;
    }

    public Circle(Point center, double radius, Color color, int strokeWidth) {
        this.center = center;
        this.radius = radius;
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    public Point getCenter()    { return center; }
    public double getRadius()   { return radius; }
    public Color getColor()     { return color; }
    public int getStrokeWidth() { return strokeWidth; }

    public void setCenter(Point center)     { this.center = center; }
    public void setRadius(double radius)    { this.radius = radius; }
    public void setColor(Color color)       { this.color = color; }
    public void setStrokeWidth(int strokeWidth) { this.strokeWidth = strokeWidth; }
}