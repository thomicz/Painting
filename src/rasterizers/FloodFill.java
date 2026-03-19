package rasterizers;

import rasters.RasterBufferedImage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

public class FloodFill {

    /**
     * BFS flood fill on a RasterBufferedImage.
     * Replaces all pixels connected to (startX, startY) that share the seed colour
     * with fillColor.
     */
    public void fill(RasterBufferedImage raster, int startX, int startY, Color fillColor) {
        BufferedImage img = raster.getImage();
        int w = img.getWidth();
        int h = img.getHeight();

        if (startX < 0 || startX >= w || startY < 0 || startY >= h) return;

        int seedRgb  = img.getRGB(startX, startY);
        int fillRgb  = fillColor.getRGB();
        if (seedRgb == fillRgb) return;

        Deque<int[]> queue = new ArrayDeque<>();
        queue.push(new int[]{startX, startY});

        while (!queue.isEmpty()) {
            int[] px = queue.pop();
            int x = px[0], y = px[1];
            if (x < 0 || x >= w || y < 0 || y >= h) continue;
            if (img.getRGB(x, y) != seedRgb) continue;

            img.setRGB(x, y, fillRgb);
            queue.push(new int[]{x + 1, y});
            queue.push(new int[]{x - 1, y});
            queue.push(new int[]{x, y + 1});
            queue.push(new int[]{x, y - 1});
        }
    }
}