package com.congxiaoyao.location.utils;

/**
 * Created by congxiaoyao on 2016/5/21.
 */
public class Line {

    VectorD p0;
    VectorD p1;
    private double a;
    private double b;
    private double c;

    public static final int SIDE_LEFT = -1;     //p0到p1方向 的左边
    public static final int SIDE_RIGHT = 1;     //p0到p1方向 的右边
    public static final int SIDE_UPON = 0;      //在线上

    public Line(VectorD p0, VectorD p1) {
        this.p0 = p0.copy();
        this.p1 = p1.copy();
        getABC();
    }

    public Line(double x0, double y0, double x1, double y1) {
        p0 = new VectorD(x0, y0);
        p1 = new VectorD(x1, y1);
        getABC();
    }

    public VectorD getP0() {
        return p0.copy();
    }

    public double getP0X() {
        return p0.x;
    }

    public double getP0Y() {
        return p0.y;
    }

    public double getP1X() {
        return p1.x;
    }

    public double getP1Y() {
        return p1.y;
    }

    public VectorD getP1() {
        return p1.copy();
    }

    public Line copy() {
        return new Line(p0, p1);
    }

    /**
     * @return p1的x值减去p0的x值
     */
    public double dx(){
        return p1.x-p0.x;
    }

    /**
     * @return p1的y值减去p0的y值
     */
    public double dy(){
        return p1.y-p0.y;
    }

    protected void getABC() {
        a = dy();
        b = -dx();
        c = p1.x * p0.y - p0.x * p1.y;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }

    /**
     * @return 线的长度
     */
    public double getLength() {
        return p0.dist(p1);
    }

    /**
     * @return 线的长度的平方
     */
    public double getLengthSq() {
        return p0.distSq(p1);
    }

    /**
     * @return 是竖线返回true
     */
    public boolean checkIsVerticalLine(){
        return b == 0;
    }
    /**
     * @return 是横线返回true
     */
    public boolean checkIsHorizentalLine(){
        return a == 0;
    }

    /**
     * 是否平行
     * @param line
     * @return
     */
    public boolean isParallel(Line line){
        return (float)a * line.b == (float)b * line.a;
    }

    /**
     * @return 得到线段的中点
     */
    public VectorD getMiddlePoint(){
        double x = p0.x+p1.x;
        double y = p0.y+p1.y;
        return new VectorD(x/2, y/2);
    }

    /**
     * @return 线段与x轴所成夹角 取值范围（-PI/2~PI/2）
     */
    public double getAngle(){
        VectorD temp = VectorD.sub(p0, p1);
        if(temp.x < 0){
            temp.mult(-1);
        }
        return temp.heading();
    }

    /**
     * 通过x求y的值
     * 如果这条线不是竖线，返回值是正常的
     * 如果这条线是竖线，返回值有可能是:
     * Double.NaN 当x在线上
     * Double.NEGATIVE_INFINITY 当x在线左边
     * Double.POSITIVE_INFINITY 当x在线右边
     * @param x
     * @return
     */
    public double getYByX(double x) {
        return -(a * x + c) / b;
    }

    /**
     * 判断一个点的坐标是否在线上
     * @param x
     * @param y
     * @return 返回点坐标是否位于这条线上
     */
    public boolean isOnTheLine(double x , double y){
        if(checkIsVerticalLine()){
            return x == p0.x;
        }
        return (float)y == (float)getYByX(x);
    }

    /**
     * 判断一个点的坐标是否在线段内
     * @param x
     * @param y
     * @return 如果在线段内 true 如果!!在线段端点 返回true
     */
    public boolean isOnTheSegment(double x , double y){
        if(!isOnTheLine(x, y)) return false;
        if(checkIsVerticalLine()) return (p0.y - y) * (p1.y - y) <= 0;
        return (p0.x - x) * (p1.x - x) <= 0;
    }

    /**
     * @param point
     * @return 点到这条直线的距离
     */
    public double distanceOfPoint(VectorD point){
        double d = 0;
        if(checkIsVerticalLine()){
            d = Math.abs(point.x-p0.x);
        }else {
            d = (float) (Math.abs(a*point.x+b*point.y+c)/
                    Math.sqrt(a*a+b*b));
        }
        return d;
    }

    public void translate(double dx, double dy) {
        p0.add(dx, dy);
        p1.add(dx, dy);
        getABC();
    }

    /**
     * @param point
     * @return 点到直线的距离的平方
     */
    public double distanceSqOfPoint(VectorD point){
        double d = 0;
        if(checkIsVerticalLine()){
            d = Math.abs(point.x-p0.x);
        }else {
            double m = a*point.x+b*point.y+c;
            double n = a * a + b * b;
            d = m*m/n;
        }
        return d;
    }

    /**
     * @return 点到直线的距离的平方
     */
    public double distanceSqOfPoint(double x, double y) {
        double d = 0;
        if (checkIsVerticalLine()) {
            d = Math.abs(x - p0.x);
        } else {
            double m = a * x + b * y + c;
            double n = a * a + b * b;
            d = m * m / n;
        }
        return d;
    }

    /**
     * 判断点坐标在线的哪一侧
     * 以p0到p1为正方向，如果点在线的左侧，返回SIDE_LEFT
     * 如果点在线的右侧 返回SIDE_RIGHT
     * 如果点在线上 返回SIDE_UPON
     * 注意 如果点在线段的延长线上 也认为是在线上
     * @param x
     * @param y
     * @return
     */
    public int whichSide(double x , double y){
        if(isOnTheLine(x, y)) return SIDE_UPON;
        //如果是一条竖线
        if(checkIsVerticalLine()){
            return ((p1.y - p0.y) * (x - p0.x)) < 0 ? SIDE_LEFT : SIDE_RIGHT;
        }
        //如果是正常的线
        return (a * x + b * y + c) < 0 ? SIDE_LEFT : SIDE_RIGHT;
    }

    /**
     * @param line
     * @return 获取与传入直线的交点，如果没有返回null，所谓的没有交点，是指两条直线平行
     */
    public VectorD getLineIntersection(Line line){
        if(isParallel(line)) return null;
        if(checkIsVerticalLine()) {
            return new VectorD(p0.x, line.getYByX(p0.x));
        }else if (line.checkIsVerticalLine()) {
            return new VectorD(line.p0.x, getYByX(line.p0.x));
        }
        double base = line.b * a - line.a * b;
        double x = (line.c * b - line.b * c) / base;
        double y = (line.a * c - line.c * a) / base;
        return new VectorD(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Line){
            Line line = (Line) obj;
            return line.p0.equals(p0) && line.p1.equals(p1);
        }
        return false;
    }
}
