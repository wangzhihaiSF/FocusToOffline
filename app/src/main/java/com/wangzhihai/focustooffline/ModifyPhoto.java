package com.wangzhihai.focustooffline;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

/**
 * Created by 王治海 on 2018/5/17.
 */
public class ModifyPhoto extends PopupWindow {
    private View mMenuView; // PopupWindow 菜单布局
    private Context context; // 上下文参数
    private View.OnClickListener myOnClick; // PopupWindow 菜单 空间单击事件

    public ModifyPhoto(Context context, Context context1, View.OnClickListener myOnClick) {
        super(context);
        this.context = context1;
        this.myOnClick = myOnClick;
    }

    private void init() {
        //PopupWindow 导入
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}
