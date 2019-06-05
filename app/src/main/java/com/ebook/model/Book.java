package com.ebook.model;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Book {

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
        formatText(fullText);   //格式化正文，存储到mParagraphList中
        findContents(mParagraphList);   //找出章节、卷、回、集等标题
    }

    //格式化文本，将文本以段落为单位保存
    private void formatText(String text) {
        String[] paragraphs = text.split("\\s{2,}");    //按段落切分文本
        for (int i = 0; i < paragraphs.length; i++) {   //格式化段落,增加首行缩进、段符，删除空行、空段
            if (paragraphs[i].isEmpty())    //无视空段，空行
                continue;
            String paragraph = mSpace + paragraphs[i] + "\n";
            mParagraphList.add(paragraph);  //将文本以段落为单位保存
        }
    }

    //找出章节、卷、回、集等标题
    private void findContents(List<String> paraList) {
        //正则匹配
        String patternString = "第\\S{2,4}\\s\\S{2,}";   //第字开头，中间有空白字符，为章节标题
        Pattern pattern = Pattern.compile(patternString);
        for (String para:paraList) {
            Matcher matcher = pattern.matcher(para);    //每一段中匹配正则表达式的内容
            if (matcher.find()){    //匹配成功
                String subString = para.substring(matcher.start(), matcher.end());  //截取章节标题
                mBookContents.add(subString);   //目录集合(卷/章/回/集等)
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
