package rasterizers;

import models.Line;

public class LineCanvasRasterizer {

    private final Rasterizer rasterizer;

    public LineCanvasRasterizer(Rasterizer rasterizer) {
        this.rasterizer = rasterizer;
    }

    public void rasterizeCanvas(Scene scene) {
        if (scene == null) return;

        for (Line line : scene.getLines()) {
            if (line == null) continue;
            rasterizer.rasterize(line);
        }
    }
}
