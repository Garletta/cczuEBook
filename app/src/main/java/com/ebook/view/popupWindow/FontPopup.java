package com.ebook.view.popupWindow;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ebook.R;
import com.ebook.util.bookPageUtil.PaintInfo;
import com.ebook.util.SaveHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FontPopup extends BasePopupWindow {
    public static final String FONTS = "fonts";

    private List<Typeface> mTypefaceList = new ArrayList<>();
    private TextView[] mTexts;
    private Button[] mButtons;

    public interface OnFontSelectedListener {
        void onTypefaceSelected(int typeIndex);
        void onColorSelected(int color);
    }

    @Override
    protected View createConvertView() {
        return LayoutInflater.from(mContext).inflate(R.layout.popup_font_layout, null);
    }

    public FontPopup(Context context) {
        super(context);
        initViews();
        initEvents();
    }

    private void initViews() {
        mTexts = new TextView[]{
                (TextView) mConvertView.findViewById(R.id.text_system),
        };
    }

    private void initEvents() {
        //设置初始状态
        PaintInfo paintInfo = SaveHelper.getObject(mContext, SaveHelper.PAINT_INFO);
    }
}
