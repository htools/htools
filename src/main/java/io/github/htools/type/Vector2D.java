package io.github.htools.type;

import io.github.htools.lib.Log;

/**
 *
 * @author Jeroen
 */
public class Vector2D {
    public static Log log = new Log(Vector2D.class);
    
    Point2D point;
    double magnitudex, magnitudey;
    
    public Vector2D(Point2D point, Point2D point2) {
        this.point = point;
        this.magnitudex = point2.x - point.x;
        this.magnitudey = point2.y - point.y;
    }
    
    public double magnitude() {
        return Math.sqrt(magnitudex * magnitudex + magnitudey * magnitudey);
    }
    
    public void scale(double scale) {
        magnitudex *= scale;
        magnitudey *= scale;
    }
    
    public Point2D end() {
        return new Point2D(point.x + magnitudex, point.y + magnitudey);
    }
    
}
