package com.library.media.ui.dialog;

import android.content.Context;

import com.lib.mediaplaylib.R;


/**
 * 初始化MateriaDialog Builder
 * Created by qingshengzheng on 15/6/11.
 */
public class MaterialDialogBuilder {

    public static MaterialDialog.Builder getBuilder(Context activity) {
        //初始化对话框
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);
        builder.contentColor(activity.getResources().getColor(R.color.black_deep))
                .backgroundColor(activity.getResources().getColor(R.color.white));
        return builder;
    }
}