package rasterizers;

import models.Circle;
import models.Line;
import models.Point;
import rasters.Raster;

import java.awt.*;

public class TrivialRasterizer implements Rasterizer {

    private Color defaultColor = Color.RED;
    private Raster raster;

    public TrivialRasterizer(Color color, Raster raster) {
        this.raster = raster;
        this.defaultColor = color;
    }

    @Override
    public void setColor(Color color) {
        defaultColor = color;
    }

    @Override
    public void setRaster(Raster raster) {
        this.raster = raster;
    }

    private static Point snap(Point p1, Point p2) {
        int x1 = p1.getX(), y1 = p1.getY();
        int x2 = p2.getX(), y2 = p2.getY();

        int dx = x2 - x1;
        int dy = y2 - y1;

        int adx = Math.abs(dx);
        int ady = Math.abs(dy);

        double ratio = 2.0;

        if (adx >= ratio * ady) {
            return new Point(x2, y1);
        } else if (ady >= ratio * adx) {
            return new Point(x1, y2);
        } else {
            int sx = Integer.compare(dx, 0);
            int sy = Integer.compare(dy, 0);
            int m = Math.max(adx, ady);
            return new Point(x1 + sx * m, y1 + sy * m);
        }
    }

    @Override
    public void rasterize(Line line) {
        if (line.isCorrectionMode()) {
            line.SetP2(snap(line.getP1(), line.getP2()));
        }

        int w = line.getStrokeWidth();
        if (w <= 1) {
            rasterizeThin(line);
            return;
        }

        int x1 = line.getP1().getX(), y1 = line.getP1().getY();
        int x2 = line.getP2().getX(), y2 = line.getP2().getY();
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);

        if (len == 0) {
            rasterizeThin(line);
            return;
        }

        double nx = -dy / len;
        double ny = dx / len;

        int half = w / 2;
        for (int i = -half; i <= half; i++) {
            Point np1 = new Point((int) Math.round(x1 + i * nx), (int) Math.round(y1 + i * ny));
            Point np2 = new Point((int) Math.round(x2 + i * nx), (int) Math.round(y2 + i * ny));
            Line offsetLine = new Line(np1, np2, line.getColor(), false, line.isDotted(), 1);
            rasterizeThin(offsetLine);
        }
    }

    private void rasterizeThin(Line line) {
        int x1 = line.getP1().getX();
        int y1 = line.getP1().getY();
        int x2 = line.getP2().getX();
        int y2 = line.getP2().getY();

        int color = line.getColor().getRGB();

        if (x1 == x2) {
            if (y1 > y2) {
                int t = y1; y1 = y2; y2 = t;
            }
            int step = line.isDotted() ? 3 : 1;
            for (int y = y1; y <= y2; y += step) {
                raster.setPixel(x1, y, color);
            }
            return;
        }

        double k = (double) (y2 - y1) / (x2 - x1);
        double q = y1 - k * x1;

        if (Math.abs(k) < 1) {
            if (x1 > x2) {
                int tx = x1; x1 = x2; x2 = tx;
                int ty = y1; y1 = y2; y2 = ty;
                k = (double) (y2 - y1) / (x2 - x1);
                q = y1 - k * x1;
            }
            int step = line.isDotted() ? 3 : 1;
            for (int x = x1; x <= x2; x += step) {
                int y = (int) Math.round(k * x + q);
                raster.setPixel(x, y, color);
            }
        } else {
            if (y1 > y2) {
                int tx = x1; x1 = x2; x2 = tx;
                int ty = y1; y1 = y2; y2 = ty;
                k = (double) (y2 - y1) / (x2 - x1);
                q = y1 - k * x1;
            }
            int step = line.isDotted() ? 3 : 1;
            for (int y = y1; y <= y2; y += step) {
                int x = (int) Math.round((y - q) / k);
                raster.setPixel(x, y, color);
            }
        }
    }

    public void rasterizeCircle(Circle circle) {
        int cx = circle.getCenter().getX();
        int cy = circle.getCenter().getY();
        int r  = (int) Math.round(circle.getRadius());
        int color = circle.getColor().getRGB();
        int w = circle.getStrokeWidth();

        int x = 0, y = r, d = 1 - r;
        while (x <= y) {
            plotCirclePoints(cx, cy, x, y, color, w);
            d += (d < 0) ? 2 * x + 3 : 2 * (x - y--) + 5;
            x++;
        }
    }

    private void plotCirclePoints(int cx, int cy, int x, int y, int color, int w) {
        int[][] offsets = {{x,y},{-x,y},{x,-y},{-x,-y},{y,x},{-y,x},{y,-x},{-y,-x}};
        int half = w / 2;
        for (int[] o : offsets) {
            for (int t = -half; t <= half; t++) {
                raster.setPixel(cx + o[0] + t, cy + o[1], color);
                raster.setPixel(cx + o[0], cy + o[1] + t, color);
            }
        }
    }
}