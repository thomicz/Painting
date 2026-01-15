package rasterizers;

import models.LIneCanvas;
import models.Line;

public class LineCanvasRasterizer {
    private Rasterizer lineRasterizer;
    private Rasterizer dottedRasterizer;
    public LineCanvasRasterizer(Rasterizer lineRasterizer,Rasterizer dottedRasterizer) {
        this.lineRasterizer = lineRasterizer;
        this.dottedRasterizer = dottedRasterizer;
    }
    public void rasterizeCanvas(LIneCanvas lIneCanvas)
    {
        for(Line line : lIneCanvas.getLines()){
            if (line.isDotted()) {
                dottedRasterizer.rasterize(line);
            }
            else {
                lineRasterizer.rasterize(line);
            }
        }
    }
}
