package models;

import rasters.Raster;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Line implements Shape {

    private Point p1;
    private Point p2;
    private Color color;
    private boolean isDotted;
    private boolean correctionMode;
    private int strokeWidth;
    private final Raster raster;

    // Konstruktor pro vytváření nové čáry (vstupuje Raster)
    public Line(Raster raster, Point p1, Color color, boolean isDotted, boolean correctionMode, int strokeWidth) {
        this.raster = raster;
        this.p1 = p1;
        this.p2 = null; // Druhý bod se určí až při draw
        this.color = color;
        this.isDotted = isDotted;
        this.correctionMode = correctionMode;
        this.strokeWidth = strokeWidth;
    }

    @Override
    public Shape draw(MouseEvent e) {
        Point currentPoint = new Point(e.getX(), e.getY());

        // 1. PRVNÍ KLIKNUTÍ: Zachytíme startovní bod
        if (this.p1 == null) {
            this.p1 = currentPoint;
            return null; // Vracíme null, tvar ještě není hotový, čeká na 2. klik
        }
        // 2. DRUHÉ KLIKNUTÍ: Zachytíme koncový bod a vykreslíme
        else if (this.p2 == null) {
            this.p2 = currentPoint;

            // Pokud je zapnutý režim korekce (snapping), upravíme p2
            // Poznámka: Zde můžeme kontrolovat shift buďto přes instanční proměnnou,
            // nebo to nechat na náhledu, ale pro jistotu:
            if (this.correctionMode) {
                this.p2 = snap(this.p1, this.p2);
            }

            // Samotné vykreslení
            render();

            return this; // Čára je hotová
        }

        return null;
    }

    @Override
    public void preview(Point currentPoint, boolean shift, boolean ctrl) {
        if (this.p1 == null) return; // Nemáme z čeho táhnout

        // Půjčíme si bod z myši, popřípadě ho zarovnáme, pokud drží Shift
        this.p2 = shift ? snap(this.p1, currentPoint) : currentPoint;

        // Půjčíme si stav tečkování z klávesy Ctrl
        boolean tempDotted = this.isDotted;
        if (ctrl) this.isDotted = true;

        render(); // Vykreslíme náhled do rastru

        // Uklidíme po sobě (vrátíme do původního stavu, aby shape čekal dál na 2. klik)
        this.isDotted = tempDotted;
        this.p2 = null;
    }

    @Override
    public void drawDirectly() {
        if (this.p1 != null && this.p2 != null) {
            render();
        }
    }

    private void render() {
        if (strokeWidth <= 1) {
            rasterizeThin(this.p1, this.p2);
        } else {
            rasterizeThick();
        }
    }

    private void rasterizeThick() {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double len = Math.sqrt(dx * dx + dy * dy);

        if (len == 0) {
            rasterizeThin(p1, p2);
            return;
        }

        // Normálový vektor pro odsazení tloušťky
        double nx = -dy / len;
        double ny = dx / len;

        int half = strokeWidth / 2;
        for (int i = -half; i <= (strokeWidth - half - 1); i++) {
            Point np1 = new Point((int) Math.round(p1.getX() + i * nx), (int) Math.round(p1.getY() + i * ny));
            Point np2 = new Point((int) Math.round(p2.getX() + i * nx), (int) Math.round(p2.getY() + i * ny));
            rasterizeThin(np1, np2);
        }
    }

    private void rasterizeThin(Point a, Point b) {
        int x1 = a.getX();
        int y1 = a.getY();
        int x2 = b.getX();
        int y2 = b.getY();
        int colorRGB = this.color.getRGB();
        int step = isDotted ? 4 : 1; // 4 pixely mezera pro tečkovanou čáru

        // Svislá čára
        if (x1 == x2) {
            if (y1 > y2) { int t = y1; y1 = y2; y2 = t; }
            for (int y = y1, count = 0; y <= y2; y++) {
                if (!isDotted || count++ % step == 0) raster.setPixel(x1, y, colorRGB);
            }
            return;
        }

        double k = (double) (y2 - y1) / (x2 - x1);
        double q = y1 - k * x1;

        if (Math.abs(k) < 1) {
            if (x1 > x2) { int tx = x1; x1 = x2; x2 = tx; int ty = y1; y1 = y2; y2 = ty; }
            for (int x = x1, count = 0; x <= x2; x++) {
                int y = (int) Math.round(k * x + q);
                if (!isDotted || count++ % step == 0) raster.setPixel(x, y, colorRGB);
            }
        } else {
            if (y1 > y2) { int tx = x1; x1 = x2; x2 = tx; int ty = y1; y1 = y2; y2 = ty; }
            for (int y = y1, count = 0; y <= y2; y++) {
                int x = (int) Math.round((y - q) / k);
                if (!isDotted || count++ % step == 0) raster.setPixel(x, y, colorRGB);
            }
        }
    }

    private Point snap(Point p1, Point p2) {
        int x1 = p1.getX(), y1 = p1.getY();
        int x2 = p2.getX(), y2 = p2.getY();
        int dx = x2 - x1, dy = y2 - y1;
        int adx = Math.abs(dx), ady = Math.abs(dy);

        double ratio = 2.0;
        if (adx >= ratio * ady) return new Point(x2, y1); // Horizontální
        if (ady >= ratio * adx) return new Point(x1, y2); // Vertikální

        // Diagonální
        int sx = Integer.compare(dx, 0);
        int sy = Integer.compare(dy, 0);
        int m = Math.max(adx, ady);
        return new Point(x1 + sx * m, y1 + sy * m);
    }

    // Gettery a Settery
    public Point getP1() { return p1; }
    public Point getP2() { return p2; }
    public void setP2(Point p2) { this.p2 = p2; }
    @Override
    public Color getColor() { return color; }
    @Override
    public void setColor(Color color) { this.color = color; }
    public boolean isDotted() { return isDotted; }
    public void setDotted(boolean dotted) { this.isDotted = dotted; }
    public boolean isCorrectionMode() { return correctionMode; }
    public void setCorrectionMode(boolean correctionMode) { this.correctionMode = correctionMode; }
    @Override
    public int getStrokeWidth() { return strokeWidth; }
    @Override
    public void setStrokeWidth(int strokeWidth) { this.strokeWidth = strokeWidth; }
}