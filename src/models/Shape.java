package models;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface Shape {
    Shape draw(MouseEvent e);

    // Nové metody pro Fázi 1
    void preview(Point currentPoint, boolean shift, boolean ctrl);
    void drawDirectly();

    Color getColor();
    void setColor(Color color);

    int getStrokeWidth();
    void setStrokeWidth(int strokeWidth);
}