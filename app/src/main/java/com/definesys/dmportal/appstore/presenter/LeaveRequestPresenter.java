package com.definesys.dmportal.appstore.presenter;

import android.content.Context;
import android.util.Log;

import com.definesys.base.BasePresenter;
import com.definesys.base.BaseResponse;
import com.definesys.dmportal.appstore.bean.LeaveInfo;
import com.definesys.dmportal.main.presenter.HttpConst;
import com.definesys.dmportal.main.presenter.MainPresenter;
import com.google.gson.Gson;
import com.hwangjr.rxbus.SmecRxBus;
import com.luck.picture.lib.entity.LocalMedia;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;

import java.io.File;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by 羽翎 on 2019/1/10.
 */

public class LeaveRequestPresenter extends BasePresenter {
    public LeaveRequestPresenter(Context context) {
        super(context);
    }

    /**
     * 提交请假请求
     * @param selectImages 选择的图片
     */
    public void getRequestResult (LeaveInfo submitLeaveInfo, List<LocalMedia> selectImages){
        //设置请假id
        submitLeaveInfo.setId(submitLeaveInfo.getUserId().toString() + System.currentTimeMillis());
        //减少传输的数据
        submitLeaveInfo.setSubTime(null);
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        RequestBody requestBody=null;
        //上传图片文件
        if(selectImages!=null&&selectImages.size()>0) {
            for (int i=0;i<selectImages.size();i++) {
                File file = new File(selectImages.get(i).getCompressPath()==null?selectImages.get(i).getPath():selectImages.get(i).getCompressPath());
                String fileName = file.getName();
                String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                MediaType type = MediaType.parse("image/" + fileType);
                builder.addFormDataPart("files", fileName, RequestBody.create(type, file));
                builder.addFormDataPart("uuids", UUID.randomUUID().toString());
            }
            builder.addFormDataPart("leaveId", submitLeaveInfo.getId());
            requestBody = builder.build();
        }


        //submitLeaveInfo.setSubmitDate(DensityUtil.getFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",new Date()));

        Log.d("myMap",new Gson().toJson(submitLeaveInfo).toString());
        RequestBody finalRequestBody = requestBody;
        ViseHttp.POST(HttpConst.submitLeaveRequest)
                .tag(HttpConst.submitLeaveRequest)
                .setJson(new Gson().toJson(submitLeaveInfo))
                .request(new ACallback<BaseResponse<String>>() {
                    @Override
                    public void onSuccess(BaseResponse<String> data) {
                        switch (data.getCode()) {
                            case "200":
                                if(finalRequestBody == null)//没有图片要上传
                                    SmecRxBus.get().post(MainPresenter.SUCCESSFUL_GET_LEAVE_REQUEST, data.getMsg());
                                else //有图片要上传
                                    setPicture(finalRequestBody);
                                break;
                            default:
                                SmecRxBus.get().post(MainPresenter.ERROR_NETWORK, data.getMsg());
                                break;
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        SmecRxBus.get().post(MainPresenter.ERROR_NETWORK, errMsg);
                    }
                });

    }

    /**
     * 上传请假图片
     * @param finalRequestBody
     */
    private void setPicture(RequestBody finalRequestBody) {
        ViseHttp.POST(HttpConst.uploadLeaveImg)
                .setRequestBody(finalRequestBody)
                .tag(HttpConst.uploadLeaveImg)
                .request(new ACallback<BaseResponse<String>>() {
                    @Override
                    public void onSuccess(BaseResponse<String> data) {
                        switch (data.getCode()) {
                            case "200":
                                SmecRxBus.get().post(MainPresenter.SUCCESSFUL_GET_LEAVE_REQUEST, data.getMsg());
                                break;
                            default:
                                SmecRxBus.get().post(MainPresenter.ERROR_NETWORK, data.getMsg());
                                break;
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        SmecRxBus.get().post(MainPresenter.ERROR_NETWORK, "");
                    }
                });
    }

    @Override
    public void unsubscribe() {
        ViseHttp.cancelTag(HttpConst.submitLeaveRequest);
        ViseHttp.cancelTag(HttpConst.uploadLeaveImg);
        super.unsubscribe();
    }
}