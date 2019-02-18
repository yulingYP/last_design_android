package com.definesys.dmportal.main.presenter;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.definesys.base.BasePresenter;
import com.definesys.base.BaseResponse;
import com.definesys.dmportal.appstore.bean.User;
import com.definesys.dmportal.main.util.SharedPreferencesUtil;
import com.google.gson.Gson;
import com.hwangjr.rxbus.SmecRxBus;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 羽翎 on 2019/1/25.
 */

public class UserInfoPresent extends BasePresenter {
    public UserInfoPresent(Context context) {
        super(context);
    }
    //获取用户信息
    public void getUserInfo(Number id,int userType){
        Map map = new HashMap();
        map.put("userId",id);
        map.put("userType",userType);
        Log.d("myMap",new Gson().toJson(map).toString());
        ViseHttp.POST(HttpConst.getUserInfo)
                .tag(HttpConst.getUserInfo)
                .setJson(new Gson().toJson(map))
                .request(new ACallback<BaseResponse<User>>() {
                    @Override
                    public void onSuccess(BaseResponse<User> data) {
                        switch (data.getCode()) {
                            case "200":
                                SharedPreferencesUtil.getInstance().setUser(data.getData());
                                SmecRxBus.get().post(MainPresenter.SUCCESSFUL_GET_USER_INFO, data.getData().getUserImage());
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

    public void getUserName(Number id, int userType, TextView textView, ProgressBar progressBar){
        Map map = new HashMap();
        map.put("userId",id);
        map.put("userType",userType);
        Log.d("myMap",new Gson().toJson(map).toString());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("tv_name",textView);
        hashMap.put("progress",progressBar);
        ViseHttp.POST(HttpConst.getUserName)
                .tag(HttpConst.getUserInfo)
                .setJson(new Gson().toJson(map))
                .request(new ACallback<BaseResponse<String>>() {
                    @Override
                    public void onSuccess(BaseResponse<String> data) {
                        switch (data.getCode()) {
                            case "200":
                                hashMap.put("data",data.getData());
                                SmecRxBus.get().post(MainPresenter.SUCCESSFUL_GET_USER_NAME, hashMap);
                                break;
                            default:
                                SmecRxBus.get().post(MainPresenter.ERROR_NETWORK_NAME, hashMap);
                                break;
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        SmecRxBus.get().post(MainPresenter.ERROR_NETWORK_NAME, hashMap);
                    }
                });
    }
    @Override
    public void unsubscribe() {
        super.unsubscribe();
        ViseHttp.cancelTag(HttpConst.getUserInfo);
    }
}
