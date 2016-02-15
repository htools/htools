package io.github.htools.type;

import io.github.htools.lib.Log;

import java.util.Collection;

/**
 *
 * @author Jeroen
 */
public class Point2D {
    public static Log log = new Log(Point2D.class);

    public double x, y;
    
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector2D vectorTo(Point2D other) {
        return new Vector2D(this, other);
    }
    
    public static Point2D middle(Collection<Point2D> points) {
        double x = 0;
        double y = 0;
        for (Point2D point : points) {
            x += point.x;
            y += point.y;
        }
        return new Point2D(x / points.size(), y / points.size());
    }
}
