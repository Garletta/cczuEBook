package com.ebook;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

//阅读界面
public class ReadingActivity extends SingleFragmentActivity {

    public static final String EXTRA_BOOK_ID = "EXTRA_BOOK_ID";

    public static Intent newIntent(Context context, int bookId) {
        Intent intent = new Intent(context, ReadingActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, bookId); //存储bookId
        return intent;
    }

    @Override
    protected void setScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);   //全屏
    }

    @Override
    protected Fragment createFragment() {
        int bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, 0); //取bookId，0为默认值
        return ReadingFragment.newInstance(bookId);
    }
}

