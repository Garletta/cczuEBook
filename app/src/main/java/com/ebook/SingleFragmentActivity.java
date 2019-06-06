package com.ebook;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

//简单的fragment
public abstract class SingleFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreen();    //设置屏幕显示状态
        setContentView(R.layout.activity_fragment); //设置对应的视图页面
        FragmentManager fm = getSupportFragmentManager();   //fragment管理器
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);   //利用fragment管理器的findFragmentById方法获得视图的容器
        if (fragment == null) { //如果找不到指定的容器，则调用createFragment方法
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();  //fragment管理器打开事务，添加指定容器，提交
        }
    }

    //设置屏幕显示状态
    protected void setScreen() {

    }

    //返回托管的fragment
    protected abstract Fragment createFragment();
}
