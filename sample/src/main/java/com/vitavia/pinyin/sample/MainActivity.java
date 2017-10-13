package com.vitavia.pinyin.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.vitavia.pinyin.PinyinComparator;
import com.vitavia.pinyin.PinyinHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PinyinHelper.initialize(this);
        String zhongwen = "朝阳大妈勇斗歹徒";
        for (String str : PinyinHelper.getMultiPinyin(zhongwen)) {
            Log.d("pinyin", str);
        }

        String arrays[] = {
                "张三", "李四", "王五"
        };
        List<String> sort;
        Collections.sort(sort = Arrays.asList(arrays), new PinyinComparator());
        for (String str : sort) {
            Log.d("pinyin", str);
        }
    }
}
