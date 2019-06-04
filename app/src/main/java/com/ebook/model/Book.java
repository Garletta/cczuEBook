package com.ebook.model;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Book {

    private static final String TAG = "Book";
    private String mBookTitle;  //书名
    private Bitmap mBookCover;  //封面
    private List<String> mParagraphList;        //格式化文本，将文本以段落为单位保存
    private List<String> mBookContents;         //目录集合(卷/章/回/集等)
    private List<Integer> mContentParaIndexs;   //目录对应的在段落集合中的索引
    private String mSpace = "\t\t\t\t\t\t";     //首行缩进

    public Book(String bookTitle, Bitmap bookCover, String fullText) {
        mBookTitle = bookTitle;
        mBookCover = bookCover;
        mParagraphList = new ArrayList<>();
        mBookContents = new ArrayList<>();
        mContentParaIndexs = new ArrayList<>();
        formatText(fullText);
        findContents(mParagraphList);
    }

    //格式化文本，将文本以段落为单位保存
    private void formatText(String text) {
        boolean isFirstParas = true;
        String paragraph = "";
        //按段落切分文本
        String[] paragraphs = text.split("\\s{2,}");
        //格式化段落
        for (int i = 0; i < paragraphs.length; i++) {
            if (paragraphs[i].isEmpty()) {  //无视空段，空行
                continue;
            }
            //paragraph = mSpace + paragraphs[i] + "\n";
            if (isFirstParas) {     //段首，增加缩进
                paragraph = mSpace + paragraphs[i];
                isFirstParas = false;
            } else {
                paragraph = "\n" + mSpace + paragraphs[i];
            }
            mParagraphList.add(paragraph);
        }
    }

    private void findContents(List<String> paraList) {
        //字符串匹配模式
        String patternString = "第\\S{2,4}\\s\\S{2,}";
        Pattern pattern = Pattern.compile(patternString);

        for (String para:paraList) {
            Matcher matcher = pattern.matcher(para);
            if (matcher.find()){
                //除去段首多余空格
                int start = matcher.start();
                int end = matcher.end();
                String subString = para.substring(start, end);
                mBookContents.add(subString);   //目录
                mContentParaIndexs.add(paraList.indexOf(para)); //目录对应的在段落集合中的索引
            }
        }
    }

    public String getBookTitle() {
        return mBookTitle;
    }

    public Bitmap getBookCover() {
        return mBookCover;
    }

    public List<String> getParagraphList() {
        return mParagraphList;
    }

    public List<String> getBookContents() {
        return mBookContents;
    }

    public List<Integer> getContentParaIndexs() {
        return mContentParaIndexs;
    }
}
