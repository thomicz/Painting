package models;

import rasters.Raster;
import java.awt.Color;
import java.awt.event.MouseEvent;

public class Rectangle implements Shape {
    private Point p1;
    private Point p2; // Horní-pravý
    private Point p3; // Dolní-levý
    private Point p4; // Dolní-pravý (uživatelský klik)

    private final Raster raster;
    private Color color;
    private int strokeWidth;
    private boolean isDotted; // Odebráno 'final' kvůli klávesové zkratce Ctrl v náhledu

    public Rectangle(Raster raster, Color color, int strokeWidth, boolean isDotted) {
        this.raster = raster;
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.isDotted = isDotted;
    }

    @Override
    public Shape draw(MouseEvent e) {
        Point currentPoint = new Point(e.getX(), e.getY());

        if (this.p1 == null) {
            // První kliknutí: nastavíme počáteční roh
            this.p1 = currentPoint;
            return null;
        } else {
            // Druhé kliknutí: nastavíme protilehlý roh (p4)
            this.p4 = currentPoint;

            // Dopočítáme p2 a p3
            calculateRemainingPoints();

            // Vykreslíme strany obdélníku
            render();

            return this;
        }
    }

    @Override
    public void preview(Point currentPoint, boolean shift, boolean ctrl) {
        if (this.p1 == null) return;

        // Půjčíme si dočasný koncový bod z kurzoru myši
        this.p4 = currentPoint;
        calculateRemainingPoints();

        // Dočasná změna pro tečkování pomocí klávesy Ctrl
        boolean tempDotted = this.isDotted;
        if (ctrl) this.isDotted = true;

        render();

        // Vrácení do původního stavu
        this.isDotted = tempDotted;
        this.p4 = null; // Musíme resetovat, protože tvar ještě není potvrzen
    }

    @Override
    public void drawDirectly() {
        if (p1 != null && p4 != null) {
            render();
        }
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

    private void calculateRemainingPoints() {
        // p1 = [x1, y1], p4 = [x4, y4]
        // p2 bude mít X z p4 a Y z p1
        p2 = new Point(p4.getX(), p1.getY());
        // p3 bude mít X z p1 a Y z p4
        p3 = new Point(p1.getX(), p4.getY());
    }

    private void render() {
        // Vytvoříme 4 pomocné čáry pro obvod obdélníku
        Line[] sides = {
                new Line(raster, p1, color, isDotted, false, strokeWidth), // horní (p1 -> p2)
                new Line(raster, p2, color, isDotted, false, strokeWidth), // pravá (p2 -> p4)
                new Line(raster, p4, color, isDotted, false, strokeWidth), // dolní (p4 -> p3)
                new Line(raster, p3, color, isDotted, false, strokeWidth)  // levá (p3 -> p1)
        };

        // Nastavíme koncové body jednotlivých stran
        sides[0].setP2(p2);
        sides[1].setP2(p4);
        sides[2].setP2(p3);
        sides[3].setP2(p1);

        // Každá čára se sama vykreslí do rastru
        for (Line side : sides) {
            side.drawDirectly();
        }
    }

    public Point[] getPoints() {
        return new Point[]{p1, p2, p3, p4};
    }

    // Gettery (pro případnou pozdější potřebu)
    public Point getP1() { return p1; }
    public Point getP4() { return p4; }
}