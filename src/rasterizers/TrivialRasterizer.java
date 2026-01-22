package rasterizers;

import models.Line;

import java.awt.*;
import models.Point;
import rasters.Raster;

public class TrivialRasterizer implements Rasterizer{

    private Color defaultColor = Color.RED;
    private Raster raster;

    public TrivialRasterizer(Color color,Raster raster) {
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

    @Override
    public void rasterize(Line line) {
        double k = calculateK(line);
        double q = calculateQ(line.getP1(), k);



        if(Math.abs(k) < 1)
        {

            if(line.getP1().getX() > line.getP2().getX())
            {
                Point tmp = line.getP1();

                line.setP1(line.getP2());
                line.setP2(tmp);
            }

            if(line.isDotted())
            {
                for (int x = line.getP1().getX(); x <= line.getP2().getX(); x+=3) {
                    int y = (int) Math.round(k * x + q);
                    raster.setPixel(x, y, defaultColor.getRGB());
                }
            }
            else{
                for (int x = line.getP1().getX(); x <= line.getP2().getX(); x++) {
                    int y = (int) Math.round(k * x + q);
                    raster.setPixel(x, y, defaultColor.getRGB());
                }
            }



        }
        else{
            if(line.getP1().getY() > line.getP2().getY())
            {
                Point tmp = line.getP1();

                line.setP1(line.getP2());
                line.setP2(tmp);
            }
if(line.isDotted())
{
    for (int y = line.getP1().getY(); y <= line.getP2().getY(); y+=3) {
        int x = (int) Math.round((y - q) / k);
        raster.setPixel(x, y, defaultColor.getRGB());
    }
}
else{
    for (int y = line.getP1().getY(); y <= line.getP2().getY(); y++) {
        int x = (int) Math.round((y - q) / k);
        raster.setPixel(x, y, defaultColor.getRGB());
    }
}


        }





    }

    private double calculateK(Line line){
        return (double) (line.getP2().getY() - line.getP1().getY())
                /( line.getP2().getX() - (line.getP1().getX()));
    }

    private double calculateQ(Point p, double k){
        return p.getY() - k * p.getX();
    }
}
