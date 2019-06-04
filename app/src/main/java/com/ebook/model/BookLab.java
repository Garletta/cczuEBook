package com.ebook.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BookLab {
    public static final String TEXT = "text";
    public static final String IMAGE = "image";
    private static BookLab sBookLab;    //书架上的书

    private AssetManager mAssetManager; //资源管理器
    private List<Book> mBookList;       //书单
    //assets中的文件名清单
    private String[] mAssetsImageList;  //资源文件中的封面
    private String[] mAssetsTextList;   //资源文件中的书

    private BookLab(Context context) {
        mAssetManager = context.getAssets();
        loadAssetsFiles();
    }

    public static BookLab newInstance(Context context) {
        if (sBookLab == null) { //书架上没有书则返回空架
            sBookLab = new BookLab(context);
        }
        return sBookLab;
    }

    //加载assets中的文件
    private void loadAssetsFiles() {
        mBookList = new ArrayList<>();
        try {
            mAssetsImageList = mAssetManager.list(IMAGE);   //从assets中获取文件夹“image”中的清单
            mAssetsTextList = mAssetManager.list(TEXT);     //从assets中获取文件夹“text”中的清单
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < mAssetsTextList.length; i++) {
            //获取书名
            String[] nameSplit = mAssetsTextList[i].split("_");
            String nameSecond = nameSplit[nameSplit.length - 1];
            String bookTitle = nameSecond.replace(".txt", "");
            //获取封面
            String imagePath = IMAGE + "/" + mAssetsImageList[i];
            Bitmap bookCover = loadImage(imagePath);
            //获取文本
            String textPath = TEXT + "/" + mAssetsTextList[i];
            String bodyText = loadText(textPath);

            Book book = new Book(bookTitle, bookCover, bodyText);
            mBookList.add(book);
        }
    }

    //从assets中读取图片
    private Bitmap loadImage(String path) {
        Bitmap image = null;
        InputStream in = null;
        try {
            in = mAssetManager.open(path);  //读取指定的封面
            image = BitmapFactory.decodeStream(in); //存为bigmap类型
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return image;
    }

    //从assets中读取文本
    private String loadText(String path) {
        InputStream in = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            in = mAssetManager.open(path);  //读取指定书内容
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {    //按行读取，转存到StringBuilder
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

    public List<Book> getBookList() {
        return mBookList;
    }
}
