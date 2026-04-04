import models.*;
import models.Point;
import models.Polygon;
import models.Rectangle;
import models.Shape;
import rasterizers.FloodFill;
import rasterizers.Scene;
import rasters.RasterBufferedImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class App {
    private final JFrame frame;
    private final JPanel panel;
    private final RasterBufferedImage raster;
    private final Scene scene;
    private final FloodFill floodFill = new FloodFill();

    private Shape currentShape = null;

    // Přidán mód POLYGON
    private enum Mode { LINE, SQUARE, RECTANGLE, CIRCLE, POLYGON, FILL }
    private Mode currentMode = Mode.LINE;

    private Color currentColor = Color.RED;
    private int strokeWidth = 1;

    // Sledování kláves a myši
    private boolean isShiftPressed = false;
    private boolean isCtrlPressed = false;
    private Point currentMouseLocation = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App(1280, 720).start());
    }

    public App(int width, int height) {
        frame = new JFrame("Vector Painter");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        raster = new RasterBufferedImage(width, height);
        scene = new Scene();

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                raster.repaint(g);
            }
        };
        panel.setPreferredSize(new Dimension(width, height));

        // Aby panel mohl chytat stisky kláves
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        setupToolbar();
        setupMouse();
        setupKeyboard();

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.requestFocusInWindow(); // Pro jistotu po zobrazení
    }

    private void setupKeyboard() {
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) isShiftPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) isCtrlPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_C) clearCanvas();
                redrawCanvas(); // Při změně klávesy se musí překreslit náhled
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) isShiftPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) isCtrlPressed = false;
                redrawCanvas();
            }
        });
    }

    private void setupMouse() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panel.requestFocusInWindow(); // Zaměření pro klávesnici

                if (currentMode == Mode.FILL) {
                    floodFill.fill(raster, e.getX(), e.getY(), currentColor);
                    panel.repaint();
                    return;
                }

                if (currentShape == null) {
                    currentShape = createNewShape();
                }

                // Bezpečnostní kontrola, aby aplikace nespadla
                if (currentShape != null) {
                    // Zachycení kliknutí tvarem (může přidat bod, ukončit polygon pravým klikem atd.)
                    Shape finishedShape = currentShape.draw(e);

                    if (finishedShape != null) {
                        // Cílový tvar může získat vlastnosti modifikátorů navždy (např. tečkování ctrl)
                        if (finishedShape instanceof Line && isCtrlPressed) {
                            ((Line) finishedShape).setDotted(true);
                        }
                        scene.addShape(finishedShape);
                        currentShape = null; // Reset pro další kreslení
                    }
                }

                redrawCanvas();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                currentMouseLocation = new Point(e.getX(), e.getY());
                if (currentShape != null) {
                    redrawCanvas();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                currentMouseLocation = new Point(e.getX(), e.getY());
                if (currentShape != null) {
                    redrawCanvas();
                }
            }
        };
        panel.addMouseListener(adapter);
        panel.addMouseMotionListener(adapter);
    }

    /**
     * Komplexní překreslení: vyčistí rastr, vykreslí hotové tvary a přes ně vykreslí aktuální náhled.
     */
    private void redrawCanvas() {
        raster.clear();

        // 1. Vykreslíme všechny potvrzené tvary
        for (Shape s : scene.getShapes()) {
            s.drawDirectly();
        }

        // 2. Vykreslíme náhled rozpracovaného tvaru
        if (currentShape != null && currentMouseLocation != null) {
            currentShape.preview(currentMouseLocation, isShiftPressed, isCtrlPressed);
        }

        panel.repaint();
    }

    private Shape createNewShape() {
        return switch (currentMode) {
            case LINE      -> new Line(raster, null, currentColor, false, false, strokeWidth);
            case SQUARE    -> new Square(raster, currentColor, strokeWidth, false);
            case RECTANGLE -> new Rectangle(raster, currentColor, strokeWidth, false);
            case CIRCLE    -> new Circle(raster, strokeWidth, currentColor);
            case POLYGON   -> new Polygon(raster, currentColor, strokeWidth, false); // Nový polygon
            default        -> null;
        };
    }

    private void setupToolbar() {
        JToolBar toolbar = new JToolBar();

        // Přidána všechna tlačítka včetně Polygonu
        addModeBtn(toolbar, "Line", Mode.LINE);
        addModeBtn(toolbar, "Square", Mode.SQUARE);
        addModeBtn(toolbar, "Rect", Mode.RECTANGLE);
        addModeBtn(toolbar, "Circle", Mode.CIRCLE);
        addModeBtn(toolbar, "Polygon", Mode.POLYGON);
        addModeBtn(toolbar, "Fill", Mode.FILL);

        toolbar.addSeparator();

        // Ovládací prvek pro tloušťku čáry
        toolbar.add(new JLabel(" Tloušťka: "));
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 50, 1);
        JSpinner thicknessSpinner = new JSpinner(spinnerModel);
        thicknessSpinner.setMaximumSize(thicknessSpinner.getPreferredSize());
        thicknessSpinner.addChangeListener(e -> {
            strokeWidth = (int) thicknessSpinner.getValue();
            panel.requestFocusInWindow(); // Vracení fokusu pro klávesnici (Shift, Ctrl, C)
        });
        toolbar.add(thicknessSpinner);

        toolbar.addSeparator();

        JButton colorBtn = new JButton("Color");
        colorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(frame, "Pick color", currentColor);
            if (c != null) currentColor = c;
            panel.requestFocusInWindow(); // Vracení fokusu po dialogu
        });
        toolbar.add(colorBtn);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> clearCanvas());
        toolbar.add(clearBtn);

        frame.add(toolbar, BorderLayout.NORTH);
    }

    private void addModeBtn(JToolBar bar, String name, Mode mode) {
        JButton btn = new JButton(name);
        btn.addActionListener(e -> {
            currentMode = mode;
            currentShape = null; // Zruší rozdělaný tvar při změně módu
            panel.requestFocusInWindow();
        });
        bar.add(btn);
    }

    private void clearCanvas() {
        scene.clearShapes();
        raster.setClearColor(0x16181E); // Tmavé pozadí
        raster.clear();
        currentShape = null;
        panel.repaint();
    }

    public void start() {
        clearCanvas();
    }
}