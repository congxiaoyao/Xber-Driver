package com.congxiaoyao.location.utils;

/**
 * Created by congxiaoyao on 2017/3/7.
 */
public class Base128 {

    public static byte[] encode(byte[] src) {
        if (src == null) return null;

        int len = src.length;
        byte[] dest = new byte[getEncodedLength(len)];

        if (dest.length == 0) return dest;

        int e = getExtraLength(len), i = e, j = 0, m = 0;
        len = dest.length - 7;

        for (; i < len; i += 7, m += 7, j++) {
            int signal = 1, k = 0;
            while (k < 7) {
                int s = src[m + k];
                dest[i + k] = (byte) (s | 0x01);
                k++;
                signal = signal | ((s & 0x01) << k);
            }
            dest[j] = (byte) signal;
        }

        int signal = 1, k = 0;
        len += 7;
        for (; i < len; i++, m++) {
            int s = src[m];
            dest[i] = (byte) (s | 0x01);
            k++;
            signal = signal | ((s & 0x01) << k);
        }
        dest[j] = (byte) signal;
        return dest;
    }

    public static byte[] decode(byte[] src) {
        if(src == null) return null;

        byte[] dest = new byte[getDecodedLength(src.length)];

        if (dest.length == 0) return dest;

        int len = dest.length;
        int i = 0, j = 0, e = getExtraLength(len);

        for (; i < len - 7; i += 7, j++) {
            int signal = src[j], k = 0;
            while (k < 7) {
                int index = i + k;
                dest[index] = (byte) (src[e + index]
                        & ((signal >> ++k) | 0xFE));
            }
        }

        int signal = src[j], m = 1;
        for (; i < len; i++, m++) {
            dest[i] = (byte) (src[e + i] & ((signal >> m) | 0xFE));
        }
        return dest;
    }

    /**
     * @param srcLen 编码前的长度
     * @return 编码后的长度
     */
    private static int getEncodedLength(int srcLen) {
        float f = srcLen * 8 / 7.0f;
        return (int) (f == (int) f ? f : f + 1);
    }

    /**
     * @param srcLen 解码前的长度
     * @return 解码后的长度
     */
    private static int getDecodedLength(int srcLen) {
        return srcLen * 7 / 8;
    }

    /**
     *
     * @param srcLen byte[]在编码之前的长度
     * @return
     */
    public static int getExtraLength(int srcLen) {
        float f = srcLen / 7.0f;
        return (int) (f == (int) f ? f : f + 1);
    }
}
