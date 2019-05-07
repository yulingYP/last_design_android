package com.definesys.dmportal.main.userSettingActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.definesys.base.BaseActivity;
import com.definesys.base.BaseResponse;
import com.definesys.dmportal.MyActivityManager;
import com.definesys.dmportal.R;
import com.definesys.dmportal.appstore.bean.User;
import com.definesys.dmportal.appstore.customViews.BottomDialog;
import com.definesys.dmportal.appstore.customViews.RCImageView;
import com.definesys.dmportal.appstore.utils.ARouterConstants;
import com.definesys.dmportal.appstore.utils.Constants;
import com.definesys.dmportal.appstore.utils.ImageUntil;
import com.definesys.dmportal.appstore.utils.PermissionsUtil;
import com.definesys.dmportal.commontitlebar.CustomTitleBar;
import com.definesys.dmportal.config.MyCongfig;
import com.definesys.dmportal.main.presenter.ChangeUserImagePresenter;
import com.definesys.dmportal.main.presenter.MainPresenter;
import com.definesys.dmportal.main.presenter.UserInfoPresent;
import com.definesys.dmportal.main.util.SharedPreferencesUtil;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.jakewharton.rxbinding2.view.RxView;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;


@Route(path = ARouterConstants.UserInfoActivity)
public class UserInfoActivity extends BaseActivity<UserInfoPresent> {
    @BindView(R.id.title_bar_att_us)
    CustomTitleBar titleBar;
    @BindView(R.id.head_pic)
    RCImageView iv_head;
    @BindView(R.id.user_name)
    TextView tv_name;
    @BindView(R.id.user_id)
    TextView tv_id;
    @BindView(R.id.group_name)
    TextView tv_group;
    @BindView(R.id.group_tip)
    TextView tv_groupTip;
    @BindView(R.id.facult_name)
    TextView tv_facult;
    @BindView(R.id.user_sex)
    TextView tv_sex;
    @BindView(R.id.phone_number)
    TextView tv_phone;
    @BindView(R.id.phone_icon)
    ImageView iv_phoneIcon;
    @BindView(R.id.aut_layout)
    LinearLayout lg_aut;
    @BindView(R.id.phone_layout)
    LinearLayout lg_phone;
    @Autowired(name = "userId")
    Number userId;
    private User user;//用户信息
    private int requestCount=0;//重新获取尝试次数
    private RequestOptions option = new RequestOptions()
            .centerCrop()
            .signature(new ObjectKey(UUID.randomUUID().toString()))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);
        ARouter.getInstance().inject(this);
        if(userId.intValue() == SharedPreferencesUtil.getInstance().getUserId().intValue()){//本人信息
            user = SharedPreferencesUtil.getInstance().getUserInfo();
            intView();
        }else {
            mPersenter.getUserInfo(userId);
        }

    }

    private void intView() {
        titleBar.setTitle(getString(R.string.user_info));
        titleBar.setBackgroundDividerEnabled(false);
        titleBar.setBackground(getResources().getDrawable(R.drawable.title_bg));
        //退出
        titleBar.addLeftBackImageButton().setOnClickListener((view) -> finish());
        refreshUserImage();//刷新头像
        //点击头像
        RxView.clicks(iv_head).throttleFirst(Constants.clickdelay, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    List<String> photoStyles = new ArrayList<>();
                    photoStyles.add(getString(R.string.ui_photo));
                    photoStyles.add(getString(R.string.ui_galary));
                    photoStyles.add(getString(R.string.ui_check_head));
                    showBottomDialog(photoStyles);
                });
        //姓名
        tv_name.setText(user.getName());

        //id
        tv_id.setText(String.valueOf(user.getUserId()));

        //院系/大学
        tv_facult.setText(user.getUserType() ==0?user.getFacultyName():getString(R.string.college_name));

        //班级/部门
        tv_group.setText(user.getUserType() ==0?user.getClassName():user.getBranchName());
        tv_groupTip.setText(user.getUserType() ==0?getString(R.string.class_tip):getString(R.string.branch_tip));

        //性别
        tv_sex.setText(user.getUserSex()==1?getString(R.string.man):getString(R.string.woman));

        //未绑定手机
        if("".equals(user.getPhone())){
            tv_phone.setText(getString(R.string.bound_phone));
            if(userId.intValue() == SharedPreferencesUtil.getInstance().getUserId().intValue()){//本人信息
                iv_phoneIcon.setVisibility(View.VISIBLE);
                //点击 绑定手机
                RxView.clicks(lg_phone)
                        .throttleFirst(Constants.clickdelay,TimeUnit.MILLISECONDS)
                        .subscribe(o->
                                ARouter.getInstance().build(ARouterConstants.PhoneBindActivity).withBoolean("isBind",false).navigation()
                        );
            }else {
                iv_phoneIcon.setVisibility(View.GONE);
            }

        }else {//已绑定
            tv_phone.setText(user.getPhone());
            iv_phoneIcon.setVisibility(View.GONE);
        }
        //本人且有权限
        if((user.getLeaveAuthority()>=0||user.getLeaveTeacherAuthority()>=0)&&userId.intValue() == SharedPreferencesUtil.getInstance().getUserId().intValue()){
            lg_aut.setVisibility(View.VISIBLE);
            //点击 查看权限
            RxView.clicks(lg_phone)
                    .throttleFirst(Constants.clickdelay,TimeUnit.MILLISECONDS)
                    .subscribe(o->
                            ARouter.getInstance().build(ARouterConstants.AuthoritySettingActivity).navigation()
                    );
        }else {//无权限
            lg_aut.setVisibility(View.GONE);
        }


    }
    //显示底部选择框
    private void showBottomDialog(List<String> list) {
        BottomDialog bottomDialog = new BottomDialog(this,list);
        bottomDialog.setOnOptionClickListener(position -> {
            if(position==2){//查看头像
                String path = SharedPreferencesUtil.getInstance().getUserLocal();
                boolean isEmpty = "".equals(path);
                if(isEmpty){
                    Bitmap image = ((BitmapDrawable)iv_head.getDrawable()).getBitmap();
                    path= ImageUntil.saveBitmapFromView(image, UUID.randomUUID().toString(),this,0);
                }
                LocalMedia localMedia = new LocalMedia();
                localMedia.setPath(path);
                localMedia.setPosition(0);
                List<LocalMedia> localMedias = new ArrayList<>();
                localMedias.add(localMedia);
                PictureSelector.create(this).openGallery(PictureMimeType.ofImage())
                        .openExternalPreview(0, localMedias);
                if(isEmpty)
                    iv_head.setImageBitmap(BitmapFactory.decodeFile(path));
            }
            if (position == 1) {//从相册选择
                PictureSelector.create(this)
                        .openGallery(PictureMimeType.ofImage())
                        .maxSelectNum(1).enableCrop(true).compress(true)
                        .circleDimmedLayer(true)
                        .rotateEnabled(false)
                        .withAspectRatio(1, 1)
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
            if(position == 0) {//拍照
                PictureSelector.create(this)
                        .openCamera(PictureMimeType.ofImage())
                        .maxSelectNum(1).enableCrop(true).compress(true)
                        .circleDimmedLayer(true)
                        .rotateEnabled(false)
                        .withAspectRatio(1, 1)
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
            bottomDialog.dismiss();
        });
        bottomDialog.setOnCancelButtonClickListener(view -> bottomDialog.dismiss()).show();
    }
    /**
     * 更新完成刷新显示用户头像
     */
    public void refreshUserImage() {
        //提示单机登陆或账号冻结
        if(MyCongfig.isShowing) return;
        String str = SharedPreferencesUtil.getInstance().getUserLocal();
        if("".equals(str)){
            str = getString(R.string.get_image, SharedPreferencesUtil.getInstance().getHttpUrl(),SharedPreferencesUtil.getInstance().getUserImageUrl(),1);
        }

        Glide.with(this)
                .asBitmap()
                .load(str)
                .apply(option)
                .into(new SimpleTarget<Bitmap>() {
                    //得到图片
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        String path = ImageUntil.saveBitmapFromView(resource,UUID.randomUUID().toString(),UserInfoActivity.this,4);
                        SharedPreferencesUtil.getInstance().setUserLocal(path);
                        iv_head.setImageBitmap(BitmapFactory.decodeFile(path));

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
//                        SharedPreferencesUtil.getInstance().setUserLocal("");
//                        userImage.setImageResource(R.drawable.my);
                        if(++requestCount<5)
                            refreshUserImage();
                        else
                            iv_head.setImageResource(R.drawable.my);
                    }
                });

    }
    /**
     * 获取用户信息成功
     * @param data 信息
     */
    @Subscribe(tags = {
            @Tag(MainPresenter.SUCCESSFUL_GET_REQUEST_USER_INFO)
    }, thread = EventThread.MAIN_THREAD)
    public void getUserUrl(BaseResponse<User> data) {
       if(MyActivityManager.getInstance().getCurrentActivity()==this){
           user = data.getData();
           intView();
        }
    }

    /*
      修改头像成功
      */
    @Subscribe(tags = {
            @Tag(MainPresenter.SUCCESSFUL_UPLOAD_USER_IMAGE)
    }, thread = EventThread.MAIN_THREAD)
    public void successfulUploadUserImage(String newUrl) {
        if(MyActivityManager.getInstance().getCurrentActivity()==this) {
            Toast.makeText(this, R.string.msg_success_upload_image, Toast.LENGTH_SHORT).show();
            Glide.with(this).load(newUrl).apply(option).into(iv_head);
            //更新本地头像信息
            SharedPreferencesUtil.getInstance().setUserLocal(newUrl);
        }
    }
    /**
     * 上传失败
     * @param msg msg
     */
    @Subscribe(tags = {
            @Tag(MainPresenter.ERROR_NETWORK)
    }, thread = EventThread.MAIN_THREAD)
    public void netWorkError(String msg) {
        if(MyActivityManager.getInstance().getCurrentActivity()==this) {
            Toast.makeText(this, ("".equals(msg) ? getString(R.string.net_work_error) : msg), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    if(!PermissionsUtil.isNetworkConnected(this)){//网络检测
                        Toast.makeText(this, R.string.no_net_tip_2,Toast.LENGTH_SHORT).show();
                        //updateImg(path);
                        return;
                    }
                    // 图片选择结果回调
                    List<LocalMedia> selectImages = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种 path
                    // 1.media.getPath(); 为原图 path
                    // 2.media.getCutPath();为裁剪后 path，需判断 media.isCut();是否为 true
                    // 3.media.getCompressPath();为压缩后 path，需判断 media.isCompressed();是否为 true
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    String pathName = selectImages.get(0).getCompressPath();
                    File file = new File(pathName);
                    new ChangeUserImagePresenter(this).uploadUserImage(String.valueOf(SharedPreferencesUtil.getInstance().getUserId()), file,"0");
                    break;

            }
        }
    }
    @Override
    public UserInfoPresent getPersenter() {
        return new UserInfoPresent(this);
    }
}
