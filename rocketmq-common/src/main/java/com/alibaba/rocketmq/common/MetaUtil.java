/**
 * $Id: MetaUtil.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.alibaba.rocketmq.common.protocol.MetaProtos.MQResponseCode;
import com.alibaba.rocketmq.remoting.protocol.RemotingProtos.ResponseCode;


/**
 * ���ַ������ӻ�
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public class MetaUtil {
    public static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname"
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        }
        catch (Exception e) {
            return -1;
        }
    }


    /**
     * ��offsetת�����ַ�����ʽ<br>
     * ���������20λ
     */
    public static String offset2FileName(final long offset) {
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(20);
        nf.setMaximumFractionDigits(0);
        nf.setGroupingUsed(false);
        return nf.format(offset);
    }


    /**
     * �����ʱ��������λms
     */
    public static long computeEclipseTimeMilliseconds(final long beginTime) {
        return (System.currentTimeMillis() - beginTime);
    }


    public static boolean isItTimeToDo(final String when) {
        String[] whiles = when.split(";");
        if (whiles != null && whiles.length > 0) {
            Calendar now = Calendar.getInstance();
            for (String w : whiles) {
                int nowHour = Integer.parseInt(w);
                if (nowHour == now.get(Calendar.HOUR_OF_DAY)) {
                    return true;
                }
            }
        }

        return false;
    }


    public static String timeMillisToHumanString() {
        return timeMillisToHumanString(System.currentTimeMillis());
    }


    public static String timeMillisToHumanString(final long t) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(t);
        return String.format("%04d%02d%02d%02d%02d%02d%03d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
    }


    /**
     * ��ȡ���̷����ռ�ʹ����
     */
    public static double getDiskPartitionSpaceUsedPercent(final String path) {
        if (null == path || path.isEmpty())
            return -1;

        try {
            File file = new File(path);
            if (!file.exists()) {
                boolean result = file.mkdirs();
                if (!result) {
                    // TODO
                }
            }

            long totalSpace = file.getTotalSpace();
            long freeSpace = file.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            if (totalSpace > 0) {
                return usedSpace / (double) totalSpace;
            }
        }
        catch (Exception e) {
            return -1;
        }

        return -1;
    }


    public static final int crc32(byte[] array) {
        if (array != null) {
            return crc32(array, 0, array.length);
        }

        return 0;
    }


    public static final int crc32(byte[] array, int offset, int length) {
        CRC32 crc32 = new CRC32();
        crc32.update(array, offset, length);
        return (int) (crc32.getValue() & 0x7FFFFFFF);
    }


    /**
     * �ֽ�����ת����16������ʽ
     */
    public static String bytes2string(byte[] src) {
        StringBuilder sb = new StringBuilder(40);
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                sb.append(0);
            }
            sb.append(hv.toUpperCase());
        }
        return sb.toString();
    }


    /**
     * 16�����ַ���ת�����ֽ�����
     */
    public static byte[] string2bytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }


    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    public static byte[] uncompress(final byte[] src) throws IOException {
        byte[] result = src;
        byte[] uncompressData = new byte[src.length];
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(src);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(src.length);

        try {
            while (true) {
                int len = inflaterInputStream.read(uncompressData, 0, uncompressData.length);
                if (len <= 0) {
                    break;
                }
                byteArrayOutputStream.write(uncompressData, 0, len);
            }
            byteArrayOutputStream.flush();
            result = byteArrayOutputStream.toByteArray();
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            try {
                byteArrayInputStream.close();
            }
            catch (IOException e) {
            }
            try {
                inflaterInputStream.close();
            }
            catch (IOException e) {
            }
            try {
                byteArrayOutputStream.close();
            }
            catch (IOException e) {
            }
        }

        return result;
    }


    public static byte[] compress(final byte[] src, final int level) throws IOException {
        byte[] result = src;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(src.length);
        java.util.zip.Deflater deflater = new java.util.zip.Deflater(level);
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
        try {
            deflaterOutputStream.write(src);
            deflaterOutputStream.finish();
            deflaterOutputStream.close();
            result = byteArrayOutputStream.toByteArray();
        }
        catch (IOException e) {
            deflater.end();
            throw e;
        }
        finally {
            try {
                byteArrayOutputStream.close();
            }
            catch (IOException e) {
            }

            deflater.end();
        }

        return result;
    }


    public static int asInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }


    public static long asLong(String str, long defaultValue) {
        try {
            return Long.parseLong(str);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";


    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }


    public static Date parseDate(String date, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        try {
            return df.parse(date);
        }
        catch (ParseException e) {
            return null;
        }
    }


    public static String responseCode2String(final int code) {
        ResponseCode rc = ResponseCode.valueOf(code);
        if (rc != null) {
            return rc.toString();
        }

        MQResponseCode mrc = MQResponseCode.valueOf(code);
        if (mrc != null) {
            return mrc.toString();
        }

        return null;
    }
}
