package com.ebook.util.bookPageUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.ebook.model.Book;
import com.ebook.model.BookLab;
import com.ebook.util.SaveHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//书页工厂
public class BookPageFactory {

    private Context mContext;   //数据传递
    private int mWidth;         //屏幕宽
    private int mHeight;        //屏幕高
    private int marginWidth;    //边宽
    private int marginHeight;   //边高
    private int mBookId;        //书ID

    private float mVisibleWidth;    //正文区域宽
    private float mVisibleHeight;   //正文区域高

    private float mLineHeight;      //每行的高度
    private int mLineCount;         //一页能容纳的行数

    private List<String> mParaList; //文本段落集合
    private List<String> mContents; //目录集合(卷/章/回/集等)
    private List<Integer> mContentParaIndex;    //目录对应的在段落集合中的索引
    private int mParaListSize;     //段落数量

    private List<String> mPageLines = new ArrayList<>();
    private String mCurContent;     //当前page对应的目录
    private Paint mPaint;           //绘图对象

    private int[] mBgColors;        //书页背景颜色
    private int[] mTextColors;      //书页文本颜色

    private List<Typeface> mTypefaceList = new ArrayList<>();   //字体

    private PaintInfo mPaintInfo;   //绘制属性
    private ReadInfo mReadInfo;
    private String percentStr;  //阅读百分比

    public BookPageFactory(Context context, int bookId) {
        mContext = context;
        mBookId = bookId;
        mTypefaceList.add(Typeface.DEFAULT);    //初始化为默认字体
        calWidthAndHeight();    //获取屏幕的宽高
        initDatas();            //初始化数据
    }

    //获取屏幕的宽高
    private void calWidthAndHeight() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        mWidth = metrics.widthPixels;   //屏幕宽
        mHeight = metrics.heightPixels; //屏幕高
    }

    //初始化数据
    private void initDatas() {
        Book book = new BookLab(mContext).getBookList().get(mBookId);   //获得点击书架上对应的书

        marginWidth = (int) (mWidth / 30f);             //边宽
        marginHeight = (int) (mHeight / 60f);           //边高
        mVisibleWidth = mWidth - marginWidth * 2;      //正文区域宽
        mVisibleHeight = mHeight - marginHeight * 2;   //正文区域高

        mParaList = book.getParagraphList();              //以段落为单位保存的正文内容
        mParaListSize = mParaList.size();               //段落数量
        mContents = book.getBookContents();              //目录集合(卷/章/回/集等)
        mContentParaIndex = book.getContentParaIndexs();//目录对应的在段落集合中的索引

        mBgColors = new int[]{  //阅读模式背景颜色
                0xffe7dcbe, //复古
                0xffffffff, //常规
                0xffcbe1cf, //护眼
                0xff333232  //夜间
        };
        mTextColors = new int[]{    //阅读模式文本颜色
                0x8A000000, //复古
                0x8A000000, //常规
                0x8A000000, //护眼
                0xffa9a8a8  //夜间
        };

        PaintInfo paintInfo = SaveHelper.getObject(mContext, SaveHelper.PAINT_INFO);    //绘制属性
        mPaintInfo = (paintInfo != null) ? paintInfo : new PaintInfo();
        mLineHeight = mPaintInfo.textSize * 1.5f;   //每行的高度
        mLineCount = (int) (mVisibleHeight / mLineHeight) - 1;  //一页能容纳的行数
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);  //绘图对象，抗锯齿
        mPaint.setTextAlign(Paint.Align.LEFT);       //文本左对齐
        mPaint.setColor(mPaintInfo.textColor);     //绘图颜色
        mPaint.setTextSize(mPaintInfo.textSize);   //文字大小
        mPaint.setTypeface(Typeface.DEFAULT);       //字体，使用默认字体

        ReadInfo info = SaveHelper.getObject(mContext, mBookId + SaveHelper.DRAW_INFO); //阅读信息
        mReadInfo = (info != null) ? info : new ReadInfo();
    }

    //通过目录跳转到指定章节
    public List<Bitmap> updatePagesByContent(int nextParaIndex) {
        mReadInfo.nextParaIndex = nextParaIndex;
        if (mReadInfo.nextParaIndex == 1)    //第一章和卷名一起处理
            mReadInfo.nextParaIndex = 0;
        reset();
        mReadInfo.isLastNext = true;//设置为直接往后读
        List<Bitmap> bitmaps = new ArrayList<>();
        bitmaps.add(drawNextPage());
        bitmaps.add(drawNextPage());
        return bitmaps;
    }

    //绘制下一页
    public Bitmap drawNextPage() {
        if (!mReadInfo.isLastNext) {
            pageDown();
            mReadInfo.isLastNext = true;
        }
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(mPaintInfo.bgColor);
        mPageLines = getNextPageLines();    //下一页
        if (mPageLines.size() == 0 || mPageLines == null) { //已经到最后一页了
            return null;
        }
        float y = mPaintInfo.textSize;
        for (String strLine : mPageLines) {
            y += mLineHeight;
            canvas.drawText(strLine, marginWidth, y, mPaint);
        }
        drawInfo(canvas);   //绘制显示在顶部和底部的信息
        return bitmap;
    }

    //向后移动两页的距离
    private void pageDown() {
        mReadInfo.nextParaIndex += 1;   //移动到最后已读的段落
        String string = "";
        List<String> lines = new ArrayList<>();
        int totalLines = 2 * mLineCount + mReadInfo.preResLines.size();
        reset();
        while (lines.size() < totalLines && mReadInfo.nextParaIndex < mParaListSize) {
            string = mParaList.get(mReadInfo.nextParaIndex);
            mReadInfo.nextParaIndex++;
            while (string.length() > 0) {
                //检测一行能够显示多少字
                int size = mPaint.breakText(string, true, mVisibleWidth, null);
                lines.add(string.substring(0, size));
                string = string.substring(size);
            }
        }
        while (lines.size() > totalLines) {
            mReadInfo.isNextRes = true;
            int end = lines.size() - 1;
            mReadInfo.nextResLines.add(0, lines.get(end));
            lines.remove(end);
        }
    }

    //绘图信息
    private void drawInfo(Canvas canvas) {
        Paint infoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        infoPaint.setTextAlign(Paint.Align.LEFT);
        infoPaint.setTextSize(32);
        infoPaint.setColor(0xff5c5c5c);
        //当前page对应的目录
        canvas.drawText(mCurContent, marginWidth, 2f * marginHeight, infoPaint);
        //阅读进度
        float percent = mReadInfo.nextParaIndex * 1.0f / mParaListSize;
        DecimalFormat df = new DecimalFormat("#0.00");
        percentStr = df.format(percent * 100) + "%";
        canvas.drawText(percentStr, marginWidth, mHeight - marginHeight, infoPaint);
        //当前系统时间
        SimpleDateFormat sDF = new SimpleDateFormat("HH:mm");
        String timeNow = sDF.format(new Date());
        canvas.drawText(timeNow, mWidth - 3f * marginWidth, mHeight - marginHeight, infoPaint);
    }

    //绘制上一页
    public Bitmap drawPrePage() {
        if (mReadInfo.isLastNext) {
            pageUp();
            mReadInfo.isLastNext = false;
        }
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(mPaintInfo.bgColor);
        mPageLines = getPrePageLines(); //下一页
        if (mPageLines.size() == 0 || mPageLines == null) { //已经到第一页了
            return null;
        }
        float y = mPaintInfo.textSize;
        for (String strLine : mPageLines) {
            y += mLineHeight;
            canvas.drawText(strLine, marginWidth, y, mPaint);
        }
        drawInfo(canvas);   //绘制显示在顶部和底部的信息
        return bitmap;
    }

    //修改主题
    public List<Bitmap> updateTheme(int theme) {
        mPaintInfo.bgColor = mBgColors[theme];
        mPaintInfo.textColor = mTextColors[theme];
        return drawCurTwoPages();
    }

    //修改字体大小
    public List<Bitmap> updateTextSize(int textSize) {
        mPaintInfo.textSize = textSize;
        mLineHeight = textSize * 1.5f;
        mLineCount = (int) (mVisibleHeight / mLineHeight) - 1;
        return drawCurTwoPages();
    }

    //绘制最近的两页
    public List<Bitmap> drawCurTwoPages() {
        setIndexToCurStart();
        mPaint.setColor(mPaintInfo.textColor);  //绘图颜色
        mPaint.setTextSize(mPaintInfo.textSize);//文字大小
        mPaint.setTypeface(Typeface.DEFAULT);   //字体
        List<Bitmap> bitmaps = new ArrayList<>();
        if (mReadInfo.isLastNext) {
            bitmaps.add(drawNextPage());
            bitmaps.add(drawNextPage());
        } else {
            bitmaps.add(drawPrePage());
            bitmaps.add(0, drawPrePage());
        }
        return bitmaps;
    }

    //设置索引起点
    private void setIndexToCurStart() {
        if (mReadInfo.isLastNext) {
            pageUp();
            mReadInfo.nextParaIndex += 1;
            if (!mReadInfo.isPreRes)
                return;
            String string = mParaList.get(mReadInfo.nextParaIndex);

            while (string.length() > 0) {
                //检测一行能够显示多少字
                int size = mPaint.breakText(string, true, mVisibleWidth, null);
                mReadInfo.nextResLines.add(string.substring(0, size));
                string = string.substring(size);
            }

            mReadInfo.nextResLines.clear();
            mReadInfo.isNextRes = true;
            mReadInfo.nextParaIndex += 1;

            mReadInfo.preResLines.clear();
            mReadInfo.isPreRes = false;
        } else {
            pageDown();
            mReadInfo.nextParaIndex -= 1;
            if (!mReadInfo.isNextRes)
                return;
            String string = mParaList.get(mReadInfo.nextParaIndex);
            while (string.length() > 0) {
                //检测一行能够显示多少字
                int size = mPaint.breakText(string, true, mVisibleWidth, null);
                mReadInfo.preResLines.add(string.substring(0, size));
                string = string.substring(size);
            }

            mReadInfo.preResLines.removeAll(mReadInfo.nextResLines);
            mReadInfo.isPreRes = true;
            mReadInfo.nextParaIndex -= 1;

            mReadInfo.nextResLines.removeAll(mReadInfo.preResLines);
            mReadInfo.isNextRes = false;
        }
    }

    //找到当前page对应的目录
    private String findContent(int paraIndex) {
        for (int i = 0; i < mContentParaIndex.size() - 1; i++) {
            if (paraIndex >= mContentParaIndex.get(i) && paraIndex < mContentParaIndex.get(i + 1)) {
                if (i == 0)
                    i = 1;   //合并卷名和第一章
                return mContents.get(i);
            }
        }
        return mContents.get(mContentParaIndex.size() - 1);
    }

    //获取下一页的lines
    private List<String> getNextPageLines() {
        String string = "";
        List<String> lines = new ArrayList<>();
        if (mReadInfo.isNextRes) {  //往后读还剩余字符串
            lines.addAll(mReadInfo.nextResLines);
            mReadInfo.nextResLines.clear(); //上一次向后读剩余的lines清空
            mReadInfo.isNextRes = false;
        }

        if (mReadInfo.nextParaIndex >= mParaListSize) { //即将读取的段落的索引在文本末
            return lines;
        }

        mCurContent = findContent(mReadInfo.nextParaIndex);

        while (lines.size() < mLineCount && mReadInfo.nextParaIndex < mParaListSize) {
            string = mParaList.get(mReadInfo.nextParaIndex);
            mReadInfo.nextParaIndex++;
            while (string.length() > 0) {
                //检测一行能够显示多少字
                int size = mPaint.breakText(string, true, mVisibleWidth, null);
                lines.add(string.substring(0, size));
                string = string.substring(size);
            }
        }

        while (lines.size() > mLineCount) {
            mReadInfo.isNextRes = true;
            int end = lines.size() - 1;
            mReadInfo.nextResLines.add(0, lines.get(end));
            lines.remove(end);
        }
        return lines;
    }

    //获取上一页的lines
    private List<String> getPrePageLines() {
        String string = "";
        List<String> lines = new ArrayList<>();
        if (mReadInfo.isPreRes) {
            lines.addAll(mReadInfo.preResLines);
            mReadInfo.preResLines.clear();
            mReadInfo.isPreRes = false;
        }

        if (mReadInfo.nextParaIndex < 0) {
            return lines;
        }

        mCurContent = findContent(mReadInfo.nextParaIndex);

        while (lines.size() < mLineCount && mReadInfo.nextParaIndex >= 0) {
            List<String> paraLines = new ArrayList<>();
            string = mParaList.get(mReadInfo.nextParaIndex);
            mReadInfo.nextParaIndex--;
            while (string.length() > 0) {
                //检测一行能够显示多少字
                int size = mPaint.breakText(string, true, mVisibleWidth, null);
                paraLines.add(string.substring(0, size));
                string = string.substring(size);
            }
            lines.addAll(0, paraLines);
        }
        while (lines.size() > mLineCount) {
            mReadInfo.isPreRes = true;
            mReadInfo.preResLines.add(lines.get(0));
            lines.remove(0);
        }
        return lines;
    }

    //向前移动两页的距离
    private void pageUp() {
        mReadInfo.nextParaIndex -= 1; //移动到最后已读的段落
        String string = "";
        List<String> lines = new ArrayList<>();
        int totalLines = 2 * mLineCount + mReadInfo.nextResLines.size();
        reset();
        while (lines.size() < totalLines && mReadInfo.nextParaIndex >= 0) {
            List<String> paraLines = new ArrayList<>();
            string = mParaList.get(mReadInfo.nextParaIndex);
            mReadInfo.nextParaIndex--;
            while (string.length() > 0) {
                //检测一行能够显示多少字
                int size = mPaint.breakText(string, true, mVisibleWidth, null);
                paraLines.add(string.substring(0, size));
                string = string.substring(size);
            }
            lines.addAll(0, paraLines);
        }
        while (lines.size() > totalLines) {
            mReadInfo.isPreRes = true;
            mReadInfo.preResLines.add(lines.get(0));
            lines.remove(0);
        }
    }

    //重置
    private void reset() {
        mReadInfo.preResLines.clear();
        mReadInfo.isPreRes = false;
        mReadInfo.nextResLines.clear();
        mReadInfo.isNextRes = false;
    }

    public ReadInfo getReadInfo() {
        return mReadInfo;
    }

    public PaintInfo getPaintInfo() {
        return mPaintInfo;
    }

    public void setReadInfo(ReadInfo readInfo) {
        mReadInfo = readInfo;
    }

    public String getCurContent() {
        return mCurContent;
    }

    public String getPercentStr() {
        return percentStr;
    }
}
