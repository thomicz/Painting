package models;

public class Square {

    private final Point p1;
    private final Point p2;
    private final Point p3;
    private final Point p4;

    public Square(Point a, Point b) {
        int x1 = (int) a.getX();
        int y1 = (int) a.getY();
        int x2 = (int) b.getX();
        int y2 = (int) b.getY();

        int dx = x2 - x1;
        int dy = y2 - y1;

        int s = Math.max(Math.abs(dx), Math.abs(dy)); // délka strany

        int sx = dx >= 0 ? 1 : -1;
        int sy = dy >= 0 ? 1 : -1;

        // osa-aligned čtverec (p1 je levý-horní / levý-dolní podle sy)
        int xb = x1 + sx * s;

        int yd = y1 + sy * s;

        p1 = new Point(x1, y1);
        p2 = new Point(xb, y1);
        p3 = new Point(xb, yd);
        p4 = new Point(x1, yd);
    }

    public Point[] getPoints() {
        return new Point[]{p1, p2, p3, p4};
    }
}
