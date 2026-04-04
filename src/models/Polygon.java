package models;

import rasters.Raster;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Polygon implements Shape {
    private final List<Point> points = new ArrayList<>();

    private final Raster raster;
    private Color color;
    private int strokeWidth;
    private boolean isDotted;

    public Polygon(Raster raster, Color color, int strokeWidth, boolean isDotted) {
        this.raster = raster;
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.isDotted = isDotted;
    }

    @Override
    public Shape draw(MouseEvent e) {
        Point p = new Point(e.getX(), e.getY());

        // Pokud uživatel klikne pravým tlačítkem, chceme polygon ukončit
        if (SwingUtilities.isRightMouseButton(e)) {
            if (points.size() >= 3) {
                render(null); // Vykreslí finální verzi bez dočasného bodu
                return this;  // Vracíme instanci - polygon se uloží do scény
            } else {
                return null;  // Nemáme 3 body, ignorujeme ukončení
            }
        }

        // Levé tlačítko - přidáme bod (pokud bychom sem chtěli snap, museli bychom sledovat i Shift,
        // ale App.java předává Shift jen do náhledu. Pro zjednodušení si ukážeme aspoň náhled).
        points.add(p);

        return null; // Stále čekáme na další body (nebo pravý klik)
    }

    @Override
    public void preview(Point currentPoint, boolean shift, boolean ctrl) {
        if (points.isEmpty()) return;

        // Klávesa Ctrl pro tečkování
        boolean tempDotted = this.isDotted;
        if (ctrl) this.isDotted = true;

        // Klávesa Shift pro zarovnání aktuálně tažené hrany
        Point tempPoint = currentPoint;
        if (shift) {
            tempPoint = snap(points.get(points.size() - 1), currentPoint);
        }

        render(tempPoint); // Vykreslíme potvrzené hrany + náhled

        // Úklid po náhledu
        this.isDotted = tempDotted;
    }

    @Override
    public void drawDirectly() {
        if (points.size() >= 3) {
            render(null);
        }
    }

    private void render(Point tempPoint) {
        if (points.isEmpty()) return;

        // 1. Vykreslíme pevné (už odkliknuté) hrany mezi body
        for (int i = 0; i < points.size() - 1; i++) {
            drawLine(points.get(i), points.get(i + 1));
        }

        // 2. Vykreslíme dočasný náhled (pokud uživatel zrovna hýbe myší)
        if (tempPoint != null) {
            // Čára od posledního pevného bodu k myši
            drawLine(points.get(points.size() - 1), tempPoint);

            // Pokud máme alespoň jeden bod, ukážeme i čáru zpět k prvnímu bodu,
            // aby uživatel viděl, jak se polygon uzavře.
            if (points.size() >= 1) {
                drawLine(tempPoint, points.get(0));
            }
        }
        // 3. Finální vykreslení z drawDirectly() nebo potvrzení (tempPoint je null)
        else {
            if (points.size() >= 3) {
                // Uzavírací hrana (poslední bod -> první bod)
                drawLine(points.get(points.size() - 1), points.get(0));
            }
        }
    }

    // Pomocná metoda pro vykreslení hrany pomocí tvé hotové třídy Line
    private void drawLine(Point a, Point b) {
        Line line = new Line(raster, a, color, isDotted, false, strokeWidth);
        line.setP2(b);
        line.drawDirectly();
    }

    // Metoda pro snapping (zarovnání) s Shiftem - zkopírovaná z tvé třídy Line
    private Point snap(Point p1, Point p2) {
        int x1 = p1.getX(), y1 = p1.getY();
        int x2 = p2.getX(), y2 = p2.getY();
        int dx = x2 - x1, dy = y2 - y1;
        int adx = Math.abs(dx), ady = Math.abs(dy);

        double ratio = 2.0;
        if (adx >= ratio * ady) return new Point(x2, y1);
        if (ady >= ratio * adx) return new Point(x1, y2);

        int sx = Integer.compare(dx, 0);
        int sy = Integer.compare(dy, 0);
        int m = Math.max(adx, ady);
        return new Point(x1 + sx * m, y1 + sy * m);
    }

    public List<Point> getPoints() { return new ArrayList<>(points); }
    @Override public Color getColor() { return color; }
    @Override public void setColor(Color color) { this.color = color; }
    @Override public int getStrokeWidth() { return strokeWidth; }
    @Override public void setStrokeWidth(int strokeWidth) { this.strokeWidth = strokeWidth; }
}