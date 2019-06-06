package com.ebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.ebook.model.Book;
import com.ebook.model.BookLab;
import com.ebook.util.bookPageUtil.BookPageFactory;
import com.ebook.util.bookPageUtil.Label;
import com.ebook.util.bookPageUtil.ReadInfo;
import com.ebook.util.SaveHelper;
import com.ebook.view.FlipView;
import com.ebook.view.popupWindow.ContentPopup;
import com.ebook.view.popupWindow.SettingPopup;
import com.ebook.view.popupWindow.LabelPopup;

import java.util.ArrayList;
import java.util.List;

//阅读界面的fragment
public class ReadingFragment extends Fragment implements View.OnClickListener {
    public static final String ARG_FLIP_BOOK_ID = "ARG_FLIP_BOOK_ID ";
    private Context mContext;
    private int mBookId;                //书ID
    private Book mBook;                 //书
    private BookPageFactory mBookPageFactory;
    private Bitmap mPrePage;            //上一页
    private Bitmap mNextPage;           //下一页
    private List<Bitmap> mPageList = new ArrayList<>(); //页
    private int[] mBgColors;            //背景色
    private FlipView mFlipView;         //翻页视图
    private LinearLayout mBottomBar;    //底部工具条
    private Button[] mBottomBtns;       //底部按钮集合
    private ContentPopup mContentPopup; //目录弹窗
    private SettingPopup mSettingPopup; //设置弹窗
    private LabelPopup mLabelPopup;     //标签弹窗
    private boolean isBottomBarShow = true; //是否显示底部工具条
    private boolean isFirstRead = true;     //是否是第一次进入
    private float mBackgroundAlpha = 1.0f;  //最初背景

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {    //主线程传消息到子线程
            WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
            layoutParams.alpha = (Float) msg.obj;
            getActivity().getWindow().setAttributes(layoutParams);
        }
    };

    public static ReadingFragment newInstance(int bookId) {
        Bundle args = new Bundle();
        args.putInt(ARG_FLIP_BOOK_ID, bookId);  //存储bookId到Bundle
        ReadingFragment fragment = new ReadingFragment();
        fragment.setArguments(args);            //通过fragment传递参数，即bookId
        return fragment;                        //返回阅读界面
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatas();    //初始化数据
    }

    private void initDatas() {
        mContext = getActivity();
        mBookId = getArguments().getInt(ARG_FLIP_BOOK_ID);
        mBook = new BookLab(mContext).getBookList().get(mBookId);
        mBookPageFactory = new BookPageFactory(mContext, mBookId);
        mBgColors = new int[]{
            0xffe7dcbe,  //复古
            0xffffffff,  // 常规
            0xffcbe1cf,  //护眼
            0xff333232  //夜间
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isFirstRead) {
            ReadInfo readInfo = SaveHelper.getObject(mContext, mBookId + SaveHelper.DRAW_INFO);
            if (readInfo != null) {
                mPageList = mBookPageFactory.drawCurTwoPages();
                mFlipView.updateBitmapList(mPageList);
            } else {
                mPageList.add(mBookPageFactory.drawNextPage());
                mPageList.add(mBookPageFactory.drawNextPage());
                mFlipView.setPrePageOver(false);
                mFlipView.updateBitmapList(mPageList);
            }
            isFirstRead = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //SettingPopup
        SaveHelper.save(mContext, SaveHelper.THEME, mSettingPopup.getTheme());
        SaveHelper.save(mContext, SaveHelper.FLIP_STYLE, mSettingPopup.getFlipStyle());
        //FlipView
        SaveHelper.save(mContext, SaveHelper.IS_PRE_PAGE_OVER, mFlipView.isPrePageOver());
        //BookPageFactory
        SaveHelper.saveObject(mContext, mBookId + SaveHelper.DRAW_INFO, mBookPageFactory.getReadInfo());
        SaveHelper.saveObject(mContext, SaveHelper.PAINT_INFO, mBookPageFactory.getPaintInfo());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_content:
                mContentPopup.setAnimationStyle(R.style.pop_window_anim_style);
                mContentPopup.showAsDropDown(mBottomBar, 0, -mContentPopup.getHeight());
                break;
            case R.id.button_setting:
                int xOff = (mBottomBar.getWidth() - mSettingPopup.getWidth()) / 2;
                int yOff = -mSettingPopup.getHeight() - mBottomBar.getHeight() / 6;
                mSettingPopup.setAnimationStyle(R.style.pop_window_anim_style);
                mSettingPopup.showAsDropDown(mBottomBar, xOff, yOff);
                break;
            case R.id.button_label:
                saveLabel();
                Toast.makeText(mContext, "书签已添加，长按显示书签列表", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //书签存入数据库
    private void saveLabel() {
        ReadInfo readInfo = mBookPageFactory.getReadInfo();
        Label label = new Label();
        label.setBookId(mBookId);
        label.setDetails(mBookPageFactory.getCurContent());
        label.setProgress(mBookPageFactory.getPercentStr());
        label.setPrePageOver(mFlipView.isPrePageOver());
        label.setReadInfoStr(SaveHelper.serObject(readInfo));
        label.save();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reading_layout, container, false);
        initViews(v);
        initEvents();
        return v;
    }

    //初始化视图
    private void initViews(View v) {
        mFlipView = (FlipView) v.findViewById(R.id.flip_view);
        mBottomBar = (LinearLayout) v.findViewById(R.id.bottom_bar_layout);
        mBottomBtns = new Button[]{
            (Button) v.findViewById(R.id.button_content),
            (Button) v.findViewById(R.id.button_setting),
            (Button) v.findViewById(R.id.button_label)
        };
        mContentPopup = new ContentPopup(mContext, mBook);
        mSettingPopup = new SettingPopup(mContext);
        mLabelPopup = new LabelPopup(mContext, mBookId);
    }

    //初始化事件
    private void initEvents() {
        if (isBottomBarShow)    //初始化，隐藏底部工具栏
            hideBottomBar();
        int theme = SaveHelper.getInt(mContext, SaveHelper.THEME);
        setTheme(theme);    //阅读模式改变
        mFlipView.setOnPageFlippedListener(new FlipView.OnPageFlippedListener() {   //翻页监听
            @Override
            public List<Bitmap> onNextPageFlipped() {   //向后读一页
                mNextPage = mBookPageFactory.drawNextPage();
                if (mNextPage == null)
                    return null;
                mPageList.remove(0);
                mPageList.add(mNextPage);
                return mPageList;
            }
            @Override
            public List<Bitmap> onPrePageFlipped() {   //向前读一页
                mPrePage = mBookPageFactory.drawPrePage();
                if (mPrePage == null)
                    return null;
                mPageList.remove(1);
                mPageList.add(0, mPrePage);
                return mPageList;
            }
            @Override
            public void onFlipStarted() {
                if (isBottomBarShow)
                    hideBottomBar();
            }
            @Override
            public void onFoldViewClicked() {
                if (isBottomBarShow)
                    hideBottomBar();
                else
                    showBottomBar();
            }
        });
        for (Button button : mBottomBtns) {
            button.setOnClickListener(this);
        }
        mBottomBtns[2].setOnLongClickListener(new View.OnLongClickListener() {  //长按书签
            @Override
            public boolean onLongClick(View v) {
                mLabelPopup.updateUI(); //刷新书签列表
                mLabelPopup.setAnimationStyle(R.style.pop_window_anim_style);
                mLabelPopup.showAsDropDown(mBottomBar, 0, -mLabelPopup.getHeight());
                return true;
            }
        });
        setPopupWindowListener();
    }

    //弹窗监听
    private void setPopupWindowListener() {
        mSettingPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                hideBottomBar();
            }
        });
        mContentPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                hideBottomBar();
            }
        });
        mLabelPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                hideBottomBar();
            }
        });
        mContentPopup.setOnContentClicked(new ContentPopup.OnContentSelectedListener() {  //注册监听器实现回调方法
            @Override
            public void OnContentClicked(int paraIndex) {
                mPageList = mBookPageFactory.updatePagesByContent(paraIndex);   //通过目录跳转指定章节
                mFlipView.setPageByContent(mPageList);  //通过目录翻页
                mContentPopup.dismiss();    //弹窗消失
            }
        });
        mSettingPopup.setOnSettingChangedListener(new SettingPopup.OnSettingChangedListener() {
            @Override
            public void onSizeChanged(int progress) {
                mPageList = mBookPageFactory.updateTextSize(progress + 50);
                mFlipView.updateBitmapList(mPageList);
            }
            @Override
            public void onThemeChanged(int theme) {
                setTheme(theme);
                mPageList = mBookPageFactory.updateTheme(theme);
                mFlipView.updateBitmapList(mPageList);
            }
            @Override
            public void onFlipStyleChanged(int style) {
                mFlipView.setFlipStyle(style);
            }
        });
        mLabelPopup.setOnLabelClicked(new LabelPopup.OnLabelSelectedListener() {
            @Override
            public void OnLabelClicked(Label label) {
                String objectStr = label.getReadInfoStr();
                ReadInfo readInfo = SaveHelper.deserObject(objectStr);
                boolean isPrePageOver = label.isPrePageOver();
                mBookPageFactory.setReadInfo(readInfo);
                mPageList = mBookPageFactory.drawCurTwoPages();
                mFlipView.setPrePageOver(isPrePageOver);
                mFlipView.updateBitmapList(mPageList);
            }
        });
    }

    //阅读模式的主题
    private void setTheme(int theme) {
        mBottomBar.setBackgroundColor(mBgColors[theme]);    //底部工具栏背景色
        mContentPopup.setBackgroundColor(mBgColors[theme]); //目录弹窗背景色
        mLabelPopup.setBackgroundColor(mBgColors[theme]);   //标签弹窗背景色
    }

    //显示底部工具条
    private void showBottomBar() {
        mBottomBar.setVisibility(View.VISIBLE);
        isBottomBarShow = true;
    }

    //隐藏底部工具条
    private void hideBottomBar() {
        mBottomBar.setVisibility(View.INVISIBLE);
        isBottomBarShow = false;
    }
}


