package com.vitavia.pinyin;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class StringUtil {

    /**
     * 清空字符串中的空格
     */
    @Nullable
    public static String stringExceptSpaces(CharSequence string) {
        if (null == string) {
            return null;
        }
        Pattern p = Pattern.compile("\\s*|\\t|\\r|\\n");
        Matcher m = p.matcher(string);
        return m.replaceAll("");
    }

    public static String[] split(String str, char ch) {
        return split(str, ch, true);
    }

    /**
     * 按字符ch分割字符串str，性能为String.split的2~3倍
     */
    public static String[] split(String str, char ch, boolean includeEmptySplit) {
        if (TextUtils.isEmpty(str)) {
            return new String[]{str};
        }

        List<String> result = null;

        int start = 0;
        int len = str.length();
        while (start < len) {
            int index = str.indexOf(ch, start);
            // 未找到
            if (index < 0) {
                break;
            }
            // 已找到
            if (index > start || includeEmptySplit) {
                if (result == null) {
                    result = new ArrayList<>(5);
                }
                result.add(str.substring(start, index));
            }
            // 下一组
            start = index + 1;
        }

        if (start != 0 && start < len && result != null) {
            result.add(str.substring(start));
        }

        return result == null ? new String[]{str} : result.toArray(new String[result.size()]);
    }

}