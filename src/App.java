import models.LIneCanvas;
import models.Point;
import models.Line;
import rasterizers.LineCanvasRasterizer;
import rasterizers.TrivialRasterizer;
import rasters.Raster;
import rasters.RasterBufferedImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;

public class App {

    private final JPanel panel;
    private final Raster raster;
    private TrivialRasterizer rasterizer;
    private KeyAdapter keyAdapter;
    private MouseAdapter mouseAdapter;
    private Point mousePosition1;
    private LIneCanvas lineCanvas;
    private LineCanvasRasterizer lineRasterizer;
    private boolean dottedMode = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App(2560, 1440).start());
    }

    public void clear(int color) {
        raster.setClearColor(color);
        raster.clear();
    }

    public void present(Graphics graphics) {
        raster.repaint(graphics);
    }

    public void start() {
        clear(0xaaaaaa);
        panel.repaint();
    }

    public App(int width, int height) {
        JFrame frame = new JFrame();

        frame.setLayout(new BorderLayout());
        frame.setTitle("Delta : " + this.getClass().getName());
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        raster = new RasterBufferedImage(width, height);

        panel = new JPanel() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                present(g);
            }
        };

        panel.setPreferredSize(new Dimension(width, height));

        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        rasterizer = new TrivialRasterizer(Color.ORANGE, raster);

        createMouseAdapters();
        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);
        lineCanvas = new LIneCanvas();
        lineRasterizer = new LineCanvasRasterizer(rasterizer,rasterizer);
    }

    private void createMouseAdapters() {
        mouseAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                mousePosition1 = new Point(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point mousePosition2 = new Point(e.getX(), e.getY());
                Line line = new Line(mousePosition1, mousePosition2);
                raster.clear();
                lineRasterizer.rasterizeCanvas(lineCanvas);
                rasterizer.rasterize(line);
                panel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point mousePosition2 = new Point(e.getX(), e.getY());
                Line line = new Line(mousePosition1, mousePosition2);
                lineCanvas.addLine(line);
                lineRasterizer.rasterizeCanvas(lineCanvas);
                rasterizer.rasterize(line);
                panel.repaint();
            }
        };
        keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    dottedMode = true;
                }
            }
            @Override
            public void keyReleased(KeyEvent e){
                dottedMode = false;
            }
        };

    }
}
