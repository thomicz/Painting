package models;

import rasters.Raster;
import java.awt.Color;
import java.awt.event.MouseEvent;

public class Square implements Shape {

    private Point p1;
    private Point p2, p3, p4; // Ostatní body dopočítáme

    private final Raster raster;
    private Color color;
    private int strokeWidth;
    private boolean isDotted; // Odebráno 'final', abychom mohli dočasně měnit při náhledu (Ctrl)

    public Square(Raster raster, Color color, int strokeWidth, boolean isDotted) {
        this.raster = raster;
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.isDotted = isDotted;
    }

    @Override
    public Shape draw(MouseEvent e) {
        Point currentPoint = new Point(e.getX(), e.getY());

        if (this.p1 == null) {
            // První kliknutí - uložíme počáteční roh
            this.p1 = currentPoint;
            return null;
        } else {
            // Druhé kliknutí - dopočítáme body čtverce
            calculatePoints(this.p1, currentPoint);

            // Vykreslíme 4 čáry, které tvoří čtverec
            render();

            return this;
        }
    }

    @Override
    public void preview(Point currentPoint, boolean shift, boolean ctrl) {
        if (this.p1 == null) return;

        // Dopočítáme hrany čtverce podle aktuální pozice kurzoru myši
        calculatePoints(this.p1, currentPoint);

        // Dočasná změna pro tečkování pomocí klávesy Ctrl
        boolean tempDotted = this.isDotted;
        if (ctrl) this.isDotted = true;

        render();

        // Vrácení do původního stavu
        this.isDotted = tempDotted;
    }

    @Override
    public void drawDirectly() {
        if (p1 != null && p2 != null && p3 != null && p4 != null) {
            render();
        }
    }

    private void calculatePoints(Point a, Point b) {
        int x1 = a.getX();
        int y1 = a.getY();
        int x2 = b.getX();
        int y2 = b.getY();

        int dx = x2 - x1;
        int dy = y2 - y1;

        // Délka strany čtverce (vezmeme delší stranu opsaného obdélníku)
        int s = Math.max(Math.abs(dx), Math.abs(dy));

        int sx = (dx >= 0) ? 1 : -1;
        int sy = (dy >= 0) ? 1 : -1;

        int xb = x1 + sx * s;
        int yd = y1 + sy * s;

        // p1 je už nastaven z prvního kliknutí nebo konstruktoru
        p2 = new Point(xb, y1);
        p3 = new Point(xb, yd);
        p4 = new Point(x1, yd);
    }

    private void render() {
        // Vytvoříme pomocné čáry pro vykreslení stran
        Line[] sides = {
                new Line(raster, p1, color, isDotted, false, strokeWidth),
                new Line(raster, p2, color, isDotted, false, strokeWidth),
                new Line(raster, p3, color, isDotted, false, strokeWidth),
                new Line(raster, p4, color, isDotted, false, strokeWidth)
        };

        // Každé čáře nastavíme její koncový bod
        sides[0].setP2(p2);
        sides[1].setP2(p3);
        sides[2].setP2(p4);
        sides[3].setP2(p1);

        // Vykreslíme jednotlivé hrany pomocí metody drawDirectly ve třídě Line
        for (Line side : sides) {
            side.drawDirectly();
        }
    }

    public Point[] getPoints() {
        return new Point[]{p1, p2, p3, p4};
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public int getStrokeWidth() {
        return this.strokeWidth;
    }

    @Override
    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
}