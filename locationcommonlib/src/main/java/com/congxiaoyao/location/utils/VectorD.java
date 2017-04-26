package com.congxiaoyao.location.utils;

/**
 * Created by congxiaoyao on 2016/5/21.
 */
public class VectorD {

    public double x;
    public double y;

    public VectorD(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public VectorD() {
        this(0, 0);
    }

    public double mag() {
        return Math.sqrt(x*x + y*y);
    }

    public double magSq() {
        return (x*x + y*y);
    }

    /**
     * 向量加法
     * @param v
     * @return
     */
    public VectorD add(VectorD v) {
        x += v.x;
        y += v.y;
        return this;
    }

    /**
     * 向量加法
     * @param x
     * @param y
     * @return
     */
    public VectorD add(double x , double y){
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     * 向量加法
     * @param v1
     * @param v2
     * @return
     */
    public static VectorD add(VectorD v1 ,VectorD v2) {
        VectorD result = v1.copy();
        return result.add(v2);
    }

    /**
     * 向量减法
     * @param v
     * @return
     */
    public VectorD sub(VectorD v) {
        x -= v.x;
        y -= v.y;
        return this;
    }

    /**
     * 向量减法
     * @param x
     * @param y
     * @return
     */
    public VectorD sub(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    /**
     * 向量减法
     * @param v1
     * @param v2
     * @return
     */
    public static VectorD sub(VectorD v1 ,VectorD v2) {
        VectorD result = v1.copy();
        return result.sub(v2);
    }

    /**
     * 向量乘法
     * @param n
     * @return
     */
    public VectorD mult(double n) {
        x *= n;
        y *= n;
        return this;
    }

    /**
     * 向量乘法
     * @param v1
     * @param n
     * @return
     */
    public static VectorD mult(VectorD v1, double n) {
        VectorD result = v1.copy();
        return result.mult(n);
    }

    /**
     * 向量除法
     * @param n
     * @return
     */
    public VectorD div(double n) {
        x /= n;
        y /= n;
        return this;
    }

    /**
     * 向量除法
     * @param v1
     * @param n
     * @return
     */
    public static VectorD div(VectorD v1, double n) {
        VectorD result = v1.copy();
        return result.div(n);
    }

    /**
     * @param v
     * @return 当前点到参数点的距离
     */
    public double dist(VectorD v) {
        double dx = x - v.x;
        double dy = y - v.y;
        return Math.sqrt(dx*dx + dy*dy);
    }

    /**
     * @param v
     * @return 当前点到参数点的距离的平方
     */
    public double distSq(VectorD v){
        double dx = x - v.x;
        double dy = y - v.y;
        return dx*dx + dy*dy;
    }

    /**
     * 向量点乘
     * @param v
     * @return
     */
    public double dot(VectorD v) {
        return x*v.x + y*v.y;
    }

    /**
     * 向量点乘
     * @param x
     * @param y
     * @return
     */
    public double dot(double x, double y) {
        return this.x*x + this.y*y;
    }

    /**
     * 将自己变成单位向量
     */
    public VectorD normalize() {
        double m = mag();
        if (m != 0 && m != 1) {
            div(m);
        }
        return this;
    }

    /**
     * 将自己的模长限制在max内
     * 也就是说如果模长大于max，将自己的模长设置为max
     * @param max
     */
    public VectorD limit(double max) {
        if (magSq() > max*max) {
            normalize();
            mult(max);
        }
        return this;
    }

    /**
     * 设置模长
     * @param len
     */
    public VectorD setMag(double len) {
        normalize();
        mult(len);
        return this;
    }

    /**
     * Calculate the angle of rotation for this vector (only 2D vectors)
     * @return the angle of rotation
     */
    public double heading() {
        double angle = Math.atan2(-y, x);
        return -angle;
    }

    /**
     * Rotate the vector by an angle (only 2D vectors), magnitude remains the same
     * @param theta the angle of rotation
     */
    public void rotate(double theta) {
        if(theta == Math.PI){
            x *= -1;
            y *= -1;
            return;
        }
        double temp = x;
        x = x*Math.cos(theta) - y*Math.sin(theta);
        y = temp*Math.sin(theta) + y*Math.cos(theta);
    }

    /**
     * 将当前向量分解为沿base方向和垂直base方向
     * @param base
     */
    public void changeBaseCoor(VectorD base) {
        double baseMag = base.mag();
        if(baseMag == 0) return;
        double newX = dot(base)/baseMag;
        double temp = base.x;
        base.x = - base.y;
        base.y = temp;
        double newY = dot(base)/baseMag;
        this.x = newX;
        this.y = newY;
    }

    /**
     * Calculates and returns the angle (in radians) between two vectors.
     * @param v1 the x, y, and z components of a VectorD
     * @param v2 the x, y, and z components of a VectorD
     */
    static public double angleBetween(VectorD v1, VectorD v2) {
        if (v1.x == 0 && v1.y == 0) return 0.0;
        if (v2.x == 0 && v2.y == 0) return 0.0;
        double dot = v1.x * v2.x + v1.y * v2.y;
        double v1mag = Math.sqrt(v1.x * v1.x + v1.y * v1.y);
        double v2mag = Math.sqrt(v2.x * v2.x + v2.y * v2.y);
        double amt = dot / (v1mag * v2mag);
        if (amt <= -1) {
            return Math.PI;
        } else if (amt >= 1) {
            return 0;
        }
        return Math.acos(amt);
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public VectorD set(VectorD src) {
        this.x = src.x;
        this.y = src.y;
        return this;
    }

    public VectorD set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public VectorD copy() {
        return new VectorD(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VectorD vectorD = (VectorD) o;

        if (Double.compare(vectorD.x, x) != 0) return false;
        return Double.compare(vectorD.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "VectorD{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
