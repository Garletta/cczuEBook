package com.ebook;

import android.support.v4.app.Fragment;

//书架页面（初始页面）
public class ShelfActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ShelfFragment(); //使用书架fragment创建书架页面
    }
}
