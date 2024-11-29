package com.artillery.musicbase.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.artillery.musicbase.R;

import java.util.List;

/**
 * @author ArtilleryOrchid
 */
public class MaterialDialogUtils {

    public void showThemed(Context context, String
            title, String content) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText("agree")
                .negativeText("disagree")
                .positiveColorRes(R.color.white)
                .negativeColorRes(R.color.white)
                .titleGravity(GravityEnum.CENTER)
                .titleColorRes(R.color.white)
                .contentColorRes(android.R.color.white)
                .dividerColorRes(R.color.white)
                .positiveColor(Color.WHITE)
                .negativeColorAttr(android.R.attr.textColorSecondaryInverse)
                .theme(Theme.DARK)
                .autoDismiss(true)  //点击是否关闭对话框
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        //dialog 出现
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //dialog 消失（返回键）
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //dialog 消失
                    }
                })
                .show();
    }

    /***
     * 获取一个耗时等待对话框
     *
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder showIndeterminateProgressDialog(Context context, String content, boolean horizontal) {
        return new MaterialDialog.Builder(context)
                .title(content)
                .progress(true, 0)
                .progressIndeterminateStyle(horizontal)
                .canceledOnTouchOutside(false)
                .backgroundColorRes(R.color.white)
                .keyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {//如果是按下，则响应，否则，一次按下会响应两次
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                //activity.onBackPressed();

                            }
                        }
                        return false;//false允许按返回键取消对话框，true除了调用取消，其他情况下不会取消
                    }
                });
    }


    /***
     * 获取基本对话框
     *
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder showBasicDialog(final Context context, String
            content) {
        return new MaterialDialog.Builder(context)
                .title(content)
                .positiveText("确定")
                .negativeText("取消");
    }

    /***
     * 显示一个基础的对话框  只有内容没有标题
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder showBasicDialogNoTitle(final Context context, String content) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .positiveText("确定")
                .negativeText("取消");
    }

    /***
     * 显示一个基础的对话框  带标题 带内容
     * 没有取消按钮
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder showBasicDialogNoCancel(final Context context, String
            title, String content) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText("确定");
    }

    /***
     * 显示一个基础的对话框  带标题 带内容
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder showBasicDialog(final Context context, String
            title, String content) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText("确定")
                .negativeText("取消");
    }

    /***
     * 显示一个基础的对话框  带标题 带内容
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder showBasicDialogPositive(final Context context, String
            title, String content) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText("复制")
                .negativeText("取消");
    }

    /***
     * 选择图片等Item的对话框  带标题
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder getSelectDialog(Context context, String title, String[] arrays) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .items(arrays)
                .itemsColor(0XFF456ea6)
                .negativeText("取消");
        if (!TextUtils.isEmpty(title)) {
            builder.title(title);
        }
        return builder;
    }

    /***
     * 获取LIST对话框
     *
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder showBasicListDialog(final Context context, String title, List
            content) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .items(content)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {

                    }
                })
                .negativeText("取消");
    }

    /***
     * 获取单选LIST对话框
     *
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder showSingleListDialog(final Context context, String title, List
            content) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .items(content)
                .itemsCallbackSingleChoice(1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which,
                                               CharSequence text) {


                        return true; // allow selection
                    }
                })
                .positiveText("选择");
    }


    /***
     * 获取多选LIST对话框
     *
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder showMultiListDialog(final Context context, String title, List
            content) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .items(content)
                .itemsCallbackMultiChoice(new Integer[]{1, 3}, new MaterialDialog
                        .ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {


                        return true; // allow selection
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.clearSelectedIndices();
                    }
                })
                .alwaysCallMultiChoiceCallback()
                .autoDismiss(false)
                .neutralText("clear")
                .itemsDisabledIndices(0, 1);
    }

    /***
     * 获取自定义对话框
     *
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder showCustomDialog(final Context context, String title, int
            content) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .customView(content, true)
                .positiveText("确定")
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog1, @NonNull DialogAction which) {

                    }
                }).build().getBuilder();
    }

    /***
     * 获取输入对话框
     *
     * @return MaterialDialog.Builder
     */
    public static MaterialDialog.Builder showInputDialog(final Context context, String title, String
            content) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .positiveText("确定")
                .negativeText("取消")
                .input("hint", "prefill", true, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                    }
                });
    }

}
