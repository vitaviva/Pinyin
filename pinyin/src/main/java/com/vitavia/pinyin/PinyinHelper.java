package com.vitavia.pinyin;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 拼音查找的辅助类
 */
public final class PinyinHelper {

    private PinyinHelper() {
    }

    private static final char PINYIN_FROM = '\u4E00';
    private static final char PINYIN_TO = '\u9FA5';
    private static final int SIZE = PINYIN_TO - PINYIN_FROM + 1;
    private static final short[] PINYIN_INDEX = new short[SIZE];
    private static final StringBuilder PINYIN_DATA = new StringBuilder(10 << 10);
    private static final int CONTACT_MAX_CHARS = 10;

    public static void initialize(Context context) {
        BufferedReader reader = null;
        try {
            //相同的pinyin使用相同的index
            BufferedInputStream is = new BufferedInputStream(
                    context.getAssets().open("pinyin.txt"));
            reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            int i = 0;
            Map<String, Short> pinyin2index = new HashMap<>();
            do {
                Short index = pinyin2index.get(line);
                if (index == null) {
                    int dataLength = PINYIN_DATA.length();
                    if (dataLength > Short.MAX_VALUE) {
                        throw new Exception(
                                "PINYIN_DATA.length() is too large, change short to int");
                    }
                    int lineLength = line.length();
                    if (lineLength > Character.MAX_VALUE) {
                        throw new Exception("line.length() is too large");
                    }
                    PINYIN_DATA.append((char) lineLength);
                    PINYIN_DATA.append(line);
                    index = (short) dataLength;
                    pinyin2index.put(line, index);
                }
                PINYIN_INDEX[i] = index;
                line = reader.readLine();
                i++;

                if (line == null) {
                    break;
                }
            } while (i < SIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            PINYIN_DATA.trimToSize();
            try {
                if (reader != null) {
                    reader.close();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getPinyin(char c) {
        if (c >= PINYIN_FROM && c <= PINYIN_TO) {
            int start = PINYIN_INDEX[c - PINYIN_FROM];
            int length = PINYIN_DATA.charAt(start);
            return PINYIN_DATA.substring(start + 1, start + 1 + length);
        }
        return null;
    }

    private static String
    getSinglePinyin(char c) {
        String py = getPinyin(c);
        if (py == null) {
            return null;
        }
        return StringUtil.split(py, ',')[0];
    }

    private static String[] getMultiPinyin(char c) {
        String py = getPinyin(c);
        if (py == null) {
            return null;
        }
        return StringUtil.split(py, ',');
    }

    /**
     * 返回汉字拼音（不考虑多音字）
     *
     * @param zhongwen
     * @return
     */
    public static String getSinglePinyin(String zhongwen) {
        if (TextUtils.isEmpty(zhongwen)) {
            return "";
        }

        StringBuilder buffer = new StringBuilder();
        char[] chars = zhongwen.toCharArray();
        boolean ispin = true;
        for (char aChar : chars) {
            String pinyin = getSinglePinyin(aChar);
            if (pinyin != null) {
                if (!ispin) {// 增加空格分隔符
                    buffer.append(' ');
                }
                buffer.append(pinyin);
                buffer.append(' ');
                ispin = true;
            } else {
                buffer.append(aChar);
                ispin = false;
            }
        }
        return buffer.toString();
    }

    /**
     * 返回拼音数组，用来处理多音字组合
     *
     * @param zhongwen
     * @return
     */
    public static String[] getMultiPinyin(String zhongwen) {
        if (TextUtils.isEmpty(zhongwen)) {
            return null;
        }

        char[] chars = zhongwen.toCharArray();
        String[][] temp = new String[zhongwen.length()][];
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= PINYIN_FROM && c <= PINYIN_TO) {
                temp[i] = getMultiPinyin(c);
            } else if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
                temp[i] = new String[]{String.valueOf(c)};
            } else {
                temp[i] = new String[]{""};
            }
        }
        return concatPinyin(temp);
    }

    /**
     * 对包含多音字的字符串的拼音进行拼接 如朝阳，则返回2*1 = 2种结果
     */
    private static String[] concatPinyin(String[][] pinyinArray) {
        StringBuffer[] temp = doConcat(pinyinArray);
        String[] result = new String[temp.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = temp[i].toString();
        }
        return result;
    }

    /**
     * 递归 对包含多音字的字符串的拼音进行拼接
     */
    private static StringBuffer[] doConcat(String[][] pinyinArray) {
        if (pinyinArray == null || pinyinArray.length == 0) {
            return new StringBuffer[0];
        }

        // 初始化首个字符
        String[] first = pinyinArray[0];
        StringBuffer[] result = new StringBuffer[first.length];
        for (int i = 0; i < first.length; i++) {
            result[i] = new StringBuffer(first[i]);
        }

        // 考虑到性能，尽在最大字符数以内处理多音字情况
        int maxChars = Math.min(pinyinArray.length, CONTACT_MAX_CHARS);
        for (int chars = 1; chars < maxChars; chars++) {
            String[] pinyin = pinyinArray[chars];

            // 单音字，不需扩容数组
            if (pinyin.length == 1) {
                if (result.length == 1) {
                    result[0].append(' ').append(pinyin[0]);
                } else {
                    for (StringBuffer buf : result) {
                        buf.append(' ').append(pinyin[0]);
                    }
                }
                continue;
            }

            // 多音字，需要扩容数组
            int index = 0;
            int newLen = result.length * pinyin.length;
            StringBuffer[] temp = new StringBuffer[newLen];
            for (StringBuffer old : result) {
                for (String py : pinyin) {
                    StringBuffer buf = new StringBuffer(old);
                    buf.append(' ').append(py);
                    temp[index++] = buf;
                }
            }
            result = temp;
        }

        // 超过最大字符数，直接追加
        for (int chars = maxChars; chars < pinyinArray.length; chars++) {
            String[] pinyin = pinyinArray[chars];
            if (result.length == 1) {
                result[0].append(' ').append(pinyin[0]);
            } else {
                for (StringBuffer buf : result) {
                    buf.append(' ').append(pinyin[0]);
                }
            }
        }

        return result;
    }

    public static String getPinyinByTrim(String string) {
        return getPinyinByTrimFromPinyin(getSinglePinyin(string));
    }

    public static String getPinyinByTrimFromPinyin(CharSequence pinyin) {
        return StringUtil.stringExceptSpaces(pinyin);
    }

    public static String getPinyinShortByTrimFromPinyin(String pinyin) {
        return getPinyinByTrimFromPinyin(getPinyinShortFromPinyin(pinyin));
    }

    private static CharSequence getPinyinShortFromPinyin(String pinyin) {
        String[] split = StringUtil.split(pinyin, ' ');
        StringBuilder result = new StringBuilder();
        for (String item : split) {
            if (!item.isEmpty()) {
                result.append(item.charAt(0)).append(' ');
            }
        }
        return result.toString();
    }
}
