package models;

import rasters.Raster;
import java.awt.Color;
import java.awt.event.MouseEvent;

public class Circle implements Shape {
    private Point center = null;
    private double radius = 0;
    private Color color;
    private int strokeWidth;
    private final Raster raster;

    public Circle(Raster raster, int strokeWidth, Color color) {
        this.raster = raster;
        this.strokeWidth = strokeWidth;
        this.color = color;
    }

    @Override
    public Shape draw(MouseEvent e) {
        Point p = new Point(e.getX(), e.getY());

        if (this.center == null) {
            this.center = p;
            return null; // Čekáme na určení poloměru
        }
        else {
            // Výpočet finálního poloměru
            double dx = p.getX() - center.getX();
            double dy = p.getY() - center.getY();
            this.radius = Math.sqrt(dx * dx + dy * dy);

            render();

            return this; // Kružnice je hotová
        }
    }

    @Override
    public void preview(Point currentPoint, boolean shift, boolean ctrl) {
        if (this.center == null) return; // Ještě nemáme střed

        // Dočasně vypočítáme poloměr z aktuální pozice myši
        double dx = currentPoint.getX() - center.getX();
        double dy = currentPoint.getY() - center.getY();
        this.radius = Math.sqrt(dx * dx + dy * dy);

        // Zde by šlo přidat např. tečkování kružnice pomocí klávesy ctrl,
        // momentálně jen zavoláme render.
        render();
    }

    @Override
    public void drawDirectly() {
        if (this.center != null && this.radius > 0) {
            render();
        }
    }

    private void render() {
        int centerX = center.getX();
        int centerY = center.getY();
        int r = (int) Math.round(this.radius);
        int c = this.color.getRGB();

        int x = 0;
        int y = r;
        int d = 1 - r;

        while (x <= y) {
            plotCirclePoints(centerX, centerY, x, y, c, strokeWidth);
            if (d < 0) {
                d += 2 * x + 3;
            } else {
                d += 2 * (x - y) + 5;
                y--;
            }
            x++;
        }
    }

    private void plotCirclePoints(int cx, int cy, int x, int y, int color, int w) {
        int[][] offsets = {
                {x, y}, {-x, y}, {x, -y}, {-x, -y},
                {y, x}, {-y, x}, {y, -x}, {-y, -x}
        };

        int half = w / 2;
        for (int[] o : offsets) {
            for (int dx = -half; dx <= (w - half - 1); dx++) {
                for (int dy = -half; dy <= (w - half - 1); dy++) {
                    raster.setPixel(cx + o[0] + dx, cy + o[1] + dy, color);
                }
            }
        }
    }

    // --- Getter ---
    public Point getCenter()    { return center; }
    public double getRadius()   { return radius; }
    @Override
    public Color getColor()     { return color; }
    @Override
    public int getStrokeWidth() { return strokeWidth; }

    // --- Setter ---
    public void setCenter(Point center)     { this.center = center; }
    public void setRadius(double radius)    { this.radius = radius; }
    @Override
    public void setColor(Color color)       { this.color = color; }
    @Override
    public void setStrokeWidth(int strokeWidth) { this.strokeWidth = strokeWidth; }
}