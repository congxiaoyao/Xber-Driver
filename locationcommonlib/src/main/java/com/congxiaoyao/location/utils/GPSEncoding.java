package com.congxiaoyao.location.utils;

public class GPSEncoding {

    public static byte[] encode(byte[] src) {
        return Base128.encode(src);
    }

    public static byte[] decode(byte[] src) {
        return Base128.decode(src);
    }

    /**
     * 将byte数组src按照','分割为多个不同的小数组
     *
     * @param src 解码前的gpsSample序列化数据
     * @return
     */
    public static byte[][] splitArray(byte[] src) {
        if (src == null || src.length == 0) return new byte[0][];
        int count = 1;
        for (int i = 0; i < src.length; i++) {
            byte b = src[i];
            if (b == ',') count++;
        }
        if (count == 1) {
            return new byte[][]{src};
        }
        byte[][] datas = new byte[count][];
        int[] splitIndexs = new int[count - 1];
        for (int i = 0, index = 0; i < src.length; i++) {
            byte b = src[i];
            if (b == ',') splitIndexs[index++] = i;
        }
        for (int i = 0, start = 0; i < splitIndexs.length; i++) {
            int splitIndex = splitIndexs[i];
            datas[i] = new byte[splitIndex - start];
            System.arraycopy(src, start, datas[i], 0, datas[i].length);
            start = splitIndex + 1;
        }
        datas[datas.length - 1] = new byte[src.length - splitIndexs[splitIndexs.length - 1] - 1];
        byte[] last = datas[datas.length - 1];
        System.arraycopy(src, splitIndexs[splitIndexs.length - 1] + 1, last, 0, last.length);
        return datas;
    }
}
