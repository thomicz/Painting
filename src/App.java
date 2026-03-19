import models.Circle;
import models.Line;
import models.Point;
import models.Polygon;
import models.Rectangle;
import models.Square;
import rasterizers.FloodFill;
import rasterizers.LineCanvasRasterizer;
import rasterizers.Scene;
import rasterizers.TrivialRasterizer;
import rasters.Raster;
import rasters.RasterBufferedImage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.Serial;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class App {
    private final JFrame frame;
    private final JPanel panel;
    private final RasterBufferedImage raster;
    private final TrivialRasterizer rasterizer;
    private final Scene scene;
    private final LineCanvasRasterizer lineRasterizer;
    private final FloodFill floodFill = new FloodFill();

    private MouseAdapter mouseAdapter;
    private Point mousePosition1;

    private enum Mode { LINE, POLYGON, SQUARE, RECTANGLE, CIRCLE, FILL, MOVE }
    private Mode currentMode = Mode.LINE;

    private Polygon polygon = new Polygon();
    private Point firstPolyPoint = null;
    private Point lastPolyPoint  = null;
    private Line  closingLine    = null;

    private Point squareStart    = null;
    private Point rectangleStart = null;
    private Point circleStart    = null;

    private Color currentColor = Color.RED;
    private int   strokeWidth  = 1;

    // ── Move state ────────────────────────────────────────────────────────────
    // Each drawn "shape" (single line, square, rectangle, polygon, circle) is
    // registered as one group.  lineGroupOf maps every Line to its group list.
    private final Map<Line, List<Line>> lineGroupOf = new IdentityHashMap<>();
    private Object dragTarget = null;   // Circle  OR  List<Line>
    private int dragLastX, dragLastY;

    // ── Design tokens ─────────────────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(0x16, 0x18, 0x1E);
    private static final Color TOOLBAR_BG    = new Color(0x1E, 0x20, 0x28);
    private static final Color SURFACE       = new Color(0x26, 0x28, 0x33);
    private static final Color ACCENT        = new Color(0x5B, 0x8D, 0xFF);
    private static final Color TEXT_PRIMARY  = new Color(0xEC, 0xED, 0xF0);
    private static final Color TEXT_MUTED    = new Color(0x7A, 0x7D, 0x8A);
    private static final Color DIVIDER       = new Color(0x2E, 0x31, 0x3D);
    private static final Color ACTIVE_BG     = new Color(0x5B, 0x8D, 0xFF, 40);
    private static final Color ACTIVE_BORDER = new Color(0x5B, 0x8D, 0xFF);
    private static final Font  FONT_UI       = new Font("Segoe UI", Font.PLAIN, 13);

    private final Map<Mode, JButton> modeButtons = new java.util.EnumMap<>(Mode.class);
    private JLabel statusLabel;

    // =========================================================================

    static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new App(2560, 1440, Color.RED).start());
    }

    public App(int width, int height, Color color) {
        frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        raster       = new RasterBufferedImage(width, height);
        rasterizer   = new TrivialRasterizer(color, raster);
        scene        = new Scene();
        lineRasterizer = new LineCanvasRasterizer(rasterizer);

        panel = new JPanel() {
            @Serial private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                raster.repaint(g);
            }
        };
        panel.setBackground(BG_DARK);
        panel.setPreferredSize(new Dimension(width, height));
        panel.setFocusable(true);

        frame.add(createToolBar(),   BorderLayout.NORTH);
        frame.add(panel,             BorderLayout.CENTER);
        frame.add(createStatusBar(), BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
        panel.requestFocusInWindow();

        setupKeyBindings();
        createMouseAdapters();
        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);
        updateTitle();
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private JPanel createToolBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TOOLBAR_BG);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER));
        bar.setPreferredSize(new Dimension(0, 56));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
        left.setOpaque(false);
        left.add(makeToolBtn("—",  Mode.LINE,      "Line",      "L"));
        left.add(makeToolBtn("⬠",  Mode.POLYGON,   "Polygon",   "P"));
        left.add(makeToolBtn("□",  Mode.SQUARE,    "Square",    "S"));
        left.add(makeToolBtn("▭",  Mode.RECTANGLE, "Rectangle", "R"));
        left.add(makeToolBtn("○",  Mode.CIRCLE,    "Circle",    "O"));
        left.add(makeToolBtn("🪣", Mode.FILL,      "Fill",      "F"));
        left.add(makeToolBtn("✥",  Mode.MOVE,      "Move",      "M"));
        left.add(makeDivider());
        left.add(makeColorButton());
        left.add(makeDivider());

        JLabel strokeLabel = new JLabel("1 px");
        strokeLabel.setFont(FONT_UI.deriveFont(12f));
        strokeLabel.setForeground(TEXT_MUTED);
        strokeLabel.setBorder(new EmptyBorder(0, 4, 0, 6));
        JLabel sliderPre = new JLabel("Stroke");
        sliderPre.setFont(FONT_UI.deriveFont(11f));
        sliderPre.setForeground(TEXT_MUTED);
        sliderPre.setBorder(new EmptyBorder(0, 0, 0, 6));
        left.add(sliderPre);
        left.add(buildStrokeSlider(strokeLabel));
        left.add(strokeLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        right.setOpaque(false);
        right.add(makeClearButton());

        bar.add(left,  BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JButton makeToolBtn(String icon, Mode mode, String tooltip, String key) {
        JButton btn = new JButton(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = currentMode == mode;
                if (active) {
                    g2.setColor(ACTIVE_BG);
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                    g2.setColor(ACTIVE_BORDER); g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(.75f,.75f,getWidth()-1.5f,getHeight()-1.5f,8,8));
                } else if (getModel().isRollover()) {
                    g2.setColor(SURFACE);
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        btn.setForeground(TEXT_PRIMARY);
        btn.setToolTipText(tooltip + "  [" + key + "]");
        btn.setPreferredSize(new Dimension(40, 36));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> { toggleMode(mode); refreshModeButtons(); });
        modeButtons.put(mode, btn);
        return btn;
    }

    private JButton makeColorButton() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DIVIDER);        g2.fillOval(6, 6, 28, 28);
                g2.setColor(currentColor);   g2.fillOval(8, 8, 24, 24);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(42, 36));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setToolTipText("Pick colour");
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(frame, "Pick a colour", currentColor);
            if (c != null) { currentColor = c; rasterizer.setColor(c); btn.repaint(); }
        });
        return btn;
    }

    private JSlider buildStrokeSlider(JLabel label) {
        JSlider s = new JSlider(1, 20, 1) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cy=getHeight()/2, th=4, pad=10, tw=getWidth()-pad*2;
                g2.setColor(SURFACE); g2.fillRoundRect(pad,cy-th/2,tw,th,th,th);
                float pct=(float)(getValue()-getMinimum())/(getMaximum()-getMinimum());
                g2.setColor(ACCENT);  g2.fillRoundRect(pad,cy-th/2,(int)(tw*pct),th,th,th);
                int tx=pad+(int)(tw*pct);
                g2.setColor(Color.WHITE); g2.fillOval(tx-7,cy-7,14,14);
                g2.setColor(ACCENT); g2.setStroke(new BasicStroke(1.5f)); g2.drawOval(tx-7,cy-7,14,14);
                g2.dispose();
            }
        };
        s.setOpaque(false); s.setPreferredSize(new Dimension(140,36));
        s.setPaintTicks(false); s.setPaintLabels(false); s.setBorder(null); s.setFocusable(false);
        s.addChangeListener(e -> { strokeWidth=s.getValue(); label.setText(strokeWidth+" px"); });
        return s;
    }

    private JButton makeClearButton() {
        JButton btn = new JButton("Clear") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0xC0,0x40,0x40) : new Color(0x8B,0x2A,0x2A));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(FONT_UI.deriveFont(Font.BOLD, 12f)); btn.setForeground(TEXT_PRIMARY);
        btn.setToolTipText("Clear canvas  [C]"); btn.setPreferredSize(new Dimension(72,34));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> clearCanvas());
        return btn;
    }

    private JComponent makeDivider() {
        JPanel d = new JPanel(); d.setOpaque(true); d.setBackground(DIVIDER);
        d.setPreferredSize(new Dimension(1,28)); d.setMaximumSize(new Dimension(1,28));
        return d;
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TOOLBAR_BG);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,DIVIDER));
        bar.setPreferredSize(new Dimension(0,26));
        statusLabel = new JLabel("  Ready");
        statusLabel.setFont(FONT_UI.deriveFont(11f)); statusLabel.setForeground(TEXT_MUTED);
        bar.add(statusLabel, BorderLayout.WEST);
        JLabel hint = new JLabel("P · S · R · O · F · M · C  ");
        hint.setFont(FONT_UI.deriveFont(11f)); hint.setForeground(new Color(0x44,0x47,0x55));
        bar.add(hint, BorderLayout.EAST);
        return bar;
    }

    private void refreshModeButtons() { modeButtons.values().forEach(JButton::repaint); }

    // ── Key bindings ──────────────────────────────────────────────────────────

    private void setupKeyBindings() {
        InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();
        bindKey(im, am, "C", this::clearCanvas);
        bindKey(im, am, "P", () -> toggleMode(Mode.POLYGON));
        bindKey(im, am, "S", () -> toggleMode(Mode.SQUARE));
        bindKey(im, am, "R", () -> toggleMode(Mode.RECTANGLE));
        bindKey(im, am, "O", () -> toggleMode(Mode.CIRCLE));
        bindKey(im, am, "F", () -> toggleMode(Mode.FILL));
        bindKey(im, am, "M", () -> toggleMode(Mode.MOVE));
    }

    private void bindKey(InputMap im, ActionMap am, String key, Runnable action) {
        im.put(KeyStroke.getKeyStroke("pressed " + key), key);
        am.put(key, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    private void toggleMode(Mode mode) {
        currentMode = (currentMode == mode) ? Mode.LINE : mode;
        resetAllState();
        updateTitle();
        refreshModeButtons();
    }

    private void clearCanvas() {
        scene.clear();
        lineGroupOf.clear();
        raster.setClearColor(0x16181E);
        raster.clear();
        resetAllState();
        updateTitle();
        panel.repaint();
    }

    private void resetAllState() {
        polygon = new Polygon();
        firstPolyPoint = null; lastPolyPoint = null; closingLine = null;
        squareStart = null; rectangleStart = null; circleStart = null;
        dragTarget = null;
    }

    private void updateTitle() {
        String name = currentMode.name().charAt(0) + currentMode.name().substring(1).toLowerCase();
        frame.setTitle("Painting  ·  " + name);
        if (statusLabel != null) statusLabel.setText("  Mode: " + name);
        refreshModeButtons();
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    private void createMouseAdapters() {
        mouseAdapter = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                panel.requestFocusInWindow();
                switch (currentMode) {
                    case POLYGON   -> handlePolygonClick(e);
                    case SQUARE    -> handleSquareClick(e);
                    case RECTANGLE -> handleRectangleClick(e);
                    case CIRCLE    -> handleCircleClick(e);
                    case FILL      -> handleFillClick(e);
                    case MOVE      -> handleMoveStart(e);
                    default        -> mousePosition1 = pointOf(e);
                }
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (currentMode == Mode.MOVE)  { handleMoveDrag(e); return; }
                if (currentMode != Mode.LINE)  return;
                raster.clear();
                lineRasterizer.rasterizeCanvas(scene);
                for (Circle c : scene.getCircles()) rasterizer.rasterizeCircle(c);
                rasterizer.rasterize(lineOf(mousePosition1, pointOf(e), e));
                panel.repaint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (currentMode == Mode.MOVE)  { dragTarget = null; return; }
                if (currentMode != Mode.LINE)  return;
                Line l = lineOf(mousePosition1, pointOf(e), e);
                registerSoloLine(l);
                scene.addLines(List.of(l));
                redrawCanvas();
            }
        };
    }

    // ── Flood fill ────────────────────────────────────────────────────────────

    private void handleFillClick(MouseEvent e) {
        floodFill.fill(raster, e.getX(), e.getY(), currentColor);
        panel.repaint();
    }

    // ── Move ──────────────────────────────────────────────────────────────────

    private void handleMoveStart(MouseEvent e) {
        int mx = e.getX(), my = e.getY();

        // Circles
        for (Circle c : scene.getCircles()) {
            double dist = Math.hypot(mx - c.getCenter().getX(), my - c.getCenter().getY());
            if (dist <= c.getRadius() + 6) {
                dragTarget = c;
                dragLastX = mx; dragLastY = my;
                return;
            }
        }

        // Lines — iterate in reverse (topmost drawn = last in scene)
        List<Line> allLines = scene.getLines();
        for (int i = allLines.size() - 1; i >= 0; i--) {
            Line l = allLines.get(i);
            if (distToSegment(mx, my, l) < 6) {
                List<Line> group = lineGroupOf.get(l);
                dragTarget = (group != null) ? group : List.of(l);
                dragLastX = mx; dragLastY = my;
                return;
            }
        }
    }

    private void handleMoveDrag(MouseEvent e) {
        if (dragTarget == null) return;
        int mx = e.getX(), my = e.getY();
        int dx = mx - dragLastX, dy = my - dragLastY;
        dragLastX = mx; dragLastY = my;

        if (dragTarget instanceof Circle c) {
            c.setCenter(new Point(c.getCenter().getX() + dx, c.getCenter().getY() + dy));

        } else if (dragTarget instanceof List<?> rawGroup) {
            @SuppressWarnings("unchecked")
            List<Line> group = (List<Line>) rawGroup;

            // Build replacements
            List<Line> replacements = new ArrayList<>();
            for (Line old : group) {
                replacements.add(new Line(
                        new Point(old.getP1().getX() + dx, old.getP1().getY() + dy),
                        new Point(old.getP2().getX() + dx, old.getP2().getY() + dy),
                        old.getColor(), old.isDotted(), old.isCorrectionMode(), old.getStrokeWidth()
                ));
            }

            // Swap in scene and re-register groups
            for (int i = 0; i < group.size(); i++) {
                Line old = group.get(i);
                Line neu = replacements.get(i);
                scene.removeLine(old);
                lineGroupOf.remove(old);
            }
            scene.addLines(replacements);
            group.clear();
            group.addAll(replacements);
            for (Line neu : replacements) lineGroupOf.put(neu, group);
        }

        redrawCanvas();
    }

    /** Perpendicular distance from point to line segment */
    private double distToSegment(int px, int py, Line l) {
        double x1 = l.getP1().getX(), y1 = l.getP1().getY();
        double x2 = l.getP2().getX(), y2 = l.getP2().getY();
        double ddx = x2-x1, ddy = y2-y1;
        if (ddx == 0 && ddy == 0) return Math.hypot(px-x1, py-y1);
        double t = Math.max(0, Math.min(1, ((px-x1)*ddx + (py-y1)*ddy) / (ddx*ddx + ddy*ddy)));
        return Math.hypot(px-(x1+t*ddx), py-(y1+t*ddy));
    }

    // ── Shape handlers ────────────────────────────────────────────────────────

    private void handleRectangleClick(MouseEvent e) {
        Point p = pointOf(e);
        if (rectangleStart == null) { rectangleStart = p; return; }
        Rectangle r = new Rectangle(); r.SetP1(rectangleStart); r.SetP4(p);
        Point[] pts = r.getPoints();
        Line l1=lineOf(pts[0],pts[1],e), l2=lineOf(pts[1],pts[3],e),
                l3=lineOf(pts[3],pts[2],e), l4=lineOf(pts[2],pts[0],e);
        registerGroup(l1, l2, l3, l4);
        scene.addLines(List.of(l1, l2, l3, l4));
        redrawCanvas(); rectangleStart = null;
    }

    private void handlePolygonClick(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) { closingLine=null; resetAllState(); return; }
        Point p = pointOf(e);
        polygon.addPoint(p);
        if (lastPolyPoint == null) { firstPolyPoint=p; lastPolyPoint=p; return; }
        if (closingLine != null) {
            scene.removeLine(closingLine);
            lineGroupOf.remove(closingLine);
            closingLine = null;
        }
        Line seg = lineOf(lastPolyPoint, p, e);
        registerSoloLine(seg);          // temporary solo — merged on next segment
        scene.addLines(List.of(seg));
        lastPolyPoint = p;
        closingLine = lineOf(lastPolyPoint, firstPolyPoint, e);
        registerSoloLine(closingLine);
        scene.addLines(List.of(closingLine));
        redrawCanvas();
    }

    private void handleSquareClick(MouseEvent e) {
        Point p = pointOf(e);
        if (squareStart == null) { squareStart=p; return; }
        Point[] pts = new Square(squareStart, p).getPoints();
        Line l1=lineOf(pts[0],pts[1],e), l2=lineOf(pts[1],pts[2],e),
                l3=lineOf(pts[2],pts[3],e), l4=lineOf(pts[3],pts[0],e);
        registerGroup(l1, l2, l3, l4);
        scene.addLines(List.of(l1, l2, l3, l4));
        redrawCanvas(); squareStart = null;
    }

    private void handleCircleClick(MouseEvent e) {
        Point p = pointOf(e);
        if (circleStart == null) { circleStart=p; return; }
        int dx=p.getX()-circleStart.getX(), dy=p.getY()-circleStart.getY();
        scene.addCircle(new Circle(circleStart, Math.sqrt(dx*dx+dy*dy), currentColor, strokeWidth));
        circleStart = null; redrawCanvas();
    }

    // ── Group registration ────────────────────────────────────────────────────

    /** All given lines form one shared group (square / rectangle). */
    private void registerGroup(Line... lines) {
        List<Line> group = new ArrayList<>(List.of(lines));
        for (Line l : lines) lineGroupOf.put(l, group);
    }

    /** Single line gets its own 1-element group. */
    private void registerSoloLine(Line l) {
        List<Line> group = new ArrayList<>();
        group.add(l);
        lineGroupOf.put(l, group);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Line lineOf(Point p1, Point p2, MouseEvent e) {
        return new Line(p1, p2, currentColor, e.isControlDown(), e.isShiftDown(), strokeWidth);
    }

    private static Point pointOf(MouseEvent e) { return new Point(e.getX(), e.getY()); }

    private void redrawCanvas() {
        raster.clear();
        lineRasterizer.rasterizeCanvas(scene);
        for (Circle c : scene.getCircles()) rasterizer.rasterizeCircle(c);
        panel.repaint();
    }

    public void start() { clearCanvas(); }
}