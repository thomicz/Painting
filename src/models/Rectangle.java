package models;

public class Rectangle {
    private Point p1;
    private Point p2;
    private Point p3;
    private Point p4;

    public Rectangle() {
    }

    public void SetP1(Point p1) {
        this.p1 = p1;
    }

    public void SetP4(Point p4) {
        this.p4 = p4;

        if(this.p4 != null && this.p1 != null) {
            CalculateRemainingPoints();
        }
    }

    private void CalculateRemainingPoints(){
        p2 = new Point(p4.getX(), p1.getY());
        p3 = new Point(p1.getX(), p4.getY());
    }

    public Point[] getPoints() {
        return new Point[]{p1, p2, p3, p4};
    }


}
