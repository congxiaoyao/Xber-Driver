package com.congxiaoyao.location.utils;

/**
 * Created by congxiaoyao on 2016/5/21.
 */
public class Ray extends Line {

    public Ray(VectorD initalPoint, VectorD p1) {
        super(initalPoint, p1);
    }

    public Ray(double x0, double y0, double x1, double y1) {
        super(x0, y0, x1, y1);
    }

    /**
     *将定点改为另外的那个点交换
     */
    public void changeInitalPoint(){
        VectorD temp = getP0();
        p0 = p1;
        p1 = temp;
        getABC();
    }

    /**
     * 固定InitalPoint，使得直线沿逆时针方向旋转angle弧度
     * @param angle
     */
    public void rotate(double angle){
        VectorD line = VectorD.sub(p1, p0);
        line.rotate(angle);
        p1.set(p0).add(line);
        getABC();
    }

    public void setLength(double length){
        VectorD vectorD =VectorD.sub(p1,p0);
        vectorD.setMag(length);
        p1.set(p0).add(vectorD);
        getABC();
    }

    /**
     * 将这条射线变为自己的镜面射线,就是旋转180度
     */
    public void becomeMirrorRay(){
        rotate(Math.PI);
    }

    /**
     * @param ray
     * @return 获取参数ray的镜面射线,就是旋转180度
     */
    public Ray getMirrorRay(Ray ray){
        Ray mirror = new Ray(ray.p0,ray.p1);
        mirror.rotate(Math.PI);
        return mirror;
    }

    /**
     * 射线与x轴所成夹角 取值范围（-PI~PI）
     */
    public double getRayAngle() {
        VectorD line = VectorD.sub(p1, p0);
        return line.heading();
    }

    public VectorD getInitalPoint(){
        return p0.copy();
    }

    public void setP1(double x , double y){
        p1.set(x, y);
        getABC();
    }

    /**
     * 获取射线与直线或直线段的交点，因为射线是有方向的，所以即使不平行，也可能没有交点
     * 如果没有交点，返回null
     * 如果射线的出射点在line上，返回null
     * 还有最重要的一点，如果要求求射线与线段的交点时，当射线射在线的端点上时，也会返回null
     * @param line
     * @return 交点的PVector对象
     */
    public Intersection getRayIntersection(Line line) {
        Intersection intersection = new Intersection();

        //所有的出射点有问题的情况
        if (line.isOnTheSegment(p0.x, p0.y)){
            if (p0.equals(line.p0) || p0.equals(line.p1))
                return intersection.setType(Intersection.TYPE_INTITAL_POINT_ON_THE_ENDS);
            return intersection.setType(Intersection.TYPE_INTITAL_POINT_ON_THE_SEGMENT);
        }
        if (line.isOnTheLine(p0.x, p0.y))
            return intersection.setType(Intersection.TYPE_INTITAL_POINT_ON_THE_EXTEND_SEGMENT);

        //出射点没问题了，先求两条直线的交点
        VectorD p = super.getLineIntersection(line);
        //两线平行 返回null;
        if(p == null) return intersection.setType(Intersection.TYPE_NULL);

        //判断交点是否在射线上
        boolean existOnRay = false;
        if(checkIsHorizentalLine())
            existOnRay = ((p.x - p0.x) * (p1.x - p0.x) > 0);
        else existOnRay = ((p.y - p0.y) * (p1.y - p0.y) > 0);
        if(!existOnRay) return intersection.setType(Intersection.TYPE_NULL);

        intersection.point = p;
        if (line.isOnTheSegment(p.x, p.y)){
            if (p.equals(line.p0) || p.equals(line.p1))
                return intersection.setType(Intersection.TYPE_IN_THE_ENDS);
            return intersection.setType(Intersection.TYPE_IN_THE_SEGMENT);
        }
        if (line.isOnTheLine(p.x, p.y))
            return intersection.setType(Intersection.TYPE_ON_THE_EXTEND_SEGMENT);
        return null;
    }

    /**
     * 做一条与当前射线垂直的直线，使得直线穿过p0点，此时二维平面被分为两部分
     * ray所指向的方向代表一部分，ray的反方向代表另一部分
     * 如果坐标xy位于ray所指向的方向所代表的那一部分，则认为是相同方向
     * @param x
     * @param y
     * @return 坐标xy与ray方向相同，返回true，xy在分界线上返回true，否则false
     */
    public boolean isSameDirection(double x, double y) {
        return ((p1.x - p0.x) * (x - p0.x) >= 0) &&
                ((p1.y - p0.y) * (y - p0.y) >= 0);
    }

    public static class Intersection{

        public static final int TYPE_NULL = 7;                   //没交点

        public static final int TYPE_IN_THE_SEGMENT = 1;          //在线段内
        public static final int TYPE_IN_THE_ENDS  =2;             //在线段两端
        public static final int TYPE_ON_THE_EXTEND_SEGMENT  =3;   //在线段的延长线上

        //出射点在线段的延长线上
        public static final int TYPE_INTITAL_POINT_ON_THE_EXTEND_SEGMENT = 4;
        //出射点在线段两端
        public static final int TYPE_INTITAL_POINT_ON_THE_ENDS = 5;
        //出射点在线段内
        public static final int TYPE_INTITAL_POINT_ON_THE_SEGMENT = 6;

        public VectorD point = null;
        public int type = -1;

        private Intersection() {}

        private Intersection setType(int type) {
            this.type = type;
            return this;
        }

        /**
         * Intersection内的point对象是不是空的
         * @return 当type为123时 point对象不是空的 其他时候point都是空的
         */
        public boolean hasPoint() {
            return type < 4;
        }

    }
}
