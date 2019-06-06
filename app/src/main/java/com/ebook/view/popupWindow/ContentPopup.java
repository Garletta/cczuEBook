package com.ebook.view.popupWindow;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ebook.R;
import com.ebook.model.Book;

import java.util.List;

public class ContentPopup extends BasePopupWindow {
    private RecyclerView mRecyclerView;
    private LinearLayout mLinearLayout;
    private Book mBook;

    private OnContentSelectedListener mListener;//定义对象

    @Override
    protected View createConvertView() {
        return LayoutInflater.from(mContext).inflate(R.layout.popup_content_layout, null);
    }

    public interface OnContentSelectedListener {   //定义了一个接口，在adapter中实现，在Activity中调用
        void OnContentClicked(int paraIndex);
    }

    public void setOnContentClicked(OnContentSelectedListener listener) {   //编写注册接口方法
        mListener = listener;
    }

    public ContentPopup(Context context, Book book) {
        super(context);
        mBook = book;
        mLinearLayout = (LinearLayout) mConvertView.findViewById(R.id.pop_content_linear_layout);
        mRecyclerView = (RecyclerView) mConvertView.findViewById(R.id.pop_contents_recycle_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));    //设置布局管理器
        mRecyclerView.setAdapter(new ContentsAdapter(mBook.getBookContents()));  //设置适配器
    }

    private class ContentsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTextView;
        private int mPosition;
        public ContentsHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
            itemView.setOnClickListener(this);
        }
        public void bind(String content, int position) {    //建立视图与数据的关联
            mPosition = position;
            mTextView.setText(content);
        }
        @Override
        public void onClick(View v) {     //事件触发时进行回调
            if (mListener != null)
                mListener.OnContentClicked(mBook.getContentParaIndexs().get(mPosition));
        }
    }

    private class ContentsAdapter extends RecyclerView.Adapter<ContentsHolder> {   //控制每个item的显示内容
        private List<String> mBookContents;
        public ContentsAdapter(List<String> bookContents) {
            mBookContents = bookContents;
        }
        @Override
        public ContentsHolder onCreateViewHolder(ViewGroup parent, int viewType) {  //为每一个item加载布局，并将布局转换为view传递给
            LayoutInflater inflater = LayoutInflater.from(mContext);       //布局加载器
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ContentsHolder(view);
        }
        @Override
        public void onBindViewHolder(ContentsHolder holder, int position) {    //将数据绑定在viewHolder上
            holder.bind(mBookContents.get(position), position);
        }
        @Override
        public int getItemCount() {
            return mBookContents.size();
        }      //返回章节目录的长度
    }

    public void setBackgroundColor(int color) {
        mLinearLayout.setBackgroundColor(color);
    }
}
