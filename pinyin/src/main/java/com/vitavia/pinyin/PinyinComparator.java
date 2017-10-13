package com.vitavia.pinyin;

import java.io.Serializable;
import java.util.Comparator;

/**
 * String比较器 中英混排，忽略大小写，忽略半角全角，数字<字母（忽略大小写）==汉字拼音
 *
 */
public class PinyinComparator implements Comparator<String>, Serializable {

    private static final long serialVersionUID = -7007249670945439369L;

    @Override
    public int compare(String lhs, String rhs) {
        if (lhs == null) {
            return rhs == null ? 0 : 1;
        } else if (rhs == null) {
            return -1;
        } else {
            return PinyinHelper.getSinglePinyin(toDBC(lhs))
                               .compareToIgnoreCase(PinyinHelper.getSinglePinyin(toDBC(rhs)));
        }
    }

    /**
     * 全角转半角
     *
     * @param input String.
     * @return 半角字符串
     */
    private static String toDBC(String input) {
        boolean isTrans = false;
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                isTrans = true;
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] -= 65248;
                isTrans = true;
            }
        }
        return isTrans ? new String(c) : input;
    }
}
