package com.definesys.dmportal.appstore.presenter;

import android.content.Context;

import com.definesys.base.BasePresenter;
import com.definesys.base.BaseResponse;
import com.definesys.dmportal.appstore.bean.ApplyInfo;
import com.definesys.dmportal.appstore.tempEntity.AuthorityDetail;
import com.definesys.dmportal.main.presenter.HttpConst;
import com.definesys.dmportal.main.presenter.MainPresenter;
import com.google.gson.Gson;
import com.hwangjr.rxbus.SmecRxBus;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;

import java.util.HashMap;
import java.util.List;

/**
 *
 * Created by 羽翎 on 2019/3/2.
 */

public class LeaveAuthorityPresenter extends BasePresenter {
    public LeaveAuthorityPresenter(Context context) {
        super(context);
    }
    //获取用户对应权限的详细信息
    public void getUserAuthorityDetail(Number userId, int authorityType, int authority){
        HashMap<String,Number> map = new HashMap<>();
        map.put("userId",userId);
        map.put("authority",authorityType==1?authority+10:authority); //审批老师权限的时候+10；方便以后整理textView
        map.put("authorityType",authorityType);
        ViseHttp.POST(HttpConst.getAuthorityDetailInfo)
                .tag(HttpConst.getAuthorityDetailInfo)
                .setJson(new Gson().toJson(map))
                .request(new ACallback<BaseResponse<List<String>>>() {
                    @Override
                    public void onSuccess(BaseResponse<List<String>> data) {
                        switch (data.getCode()){
                            case "200":
                                data.setExtendInfo(authorityType);
                                data.setMsg(""+authority);
//                                SmecRxBus.get().post(MainPresenter.SUCCESSFUL_GET_AUTHORITY_DETAIL_INFO,  data);
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
    public void getUserAuthorityDetail(Number userId, int authorityType, List<String> authorities){
        HashMap<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("authorities",authorities); //审批老师权限的时候+10；方便以后整理textView
        map.put("authorityType",authorityType);
        ViseHttp.POST(HttpConst.getAuthorityDetailInfo)
                .tag(HttpConst.getAuthorityDetailInfo)
                .setJson(new Gson().toJson(map))
                .request(new ACallback<BaseResponse<List<AuthorityDetail>>>() {
                    @Override
                    public void onSuccess(BaseResponse<List<AuthorityDetail>> data) {
                        switch (data.getCode()){
                            case "200":
                                data.setExtendInfo(authorityType);
//                                data.setMsg(""+authority);
                                SmecRxBus.get().post(MainPresenter.SUCCESSFUL_GET_AUTHORITY_DETAIL_INFO,  data);
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
    /**
     * 获取对应的数组
     * @param extendId1 type: 0/2.用户id 1.班级id *100 3/4/5/6/7/8/9/10/11/12/20/21.空
     * @param extendId2 type: 0/2.院系id 1.用户性别 3.空 4.院系名称 5.空 6.院系名称7.班级id 8.空 9.院系名称 10/11/12/20/21.空
     * @param type 0.寝室长权限根据facultyId获取班级名称
     *             1.根据班级id获取班级名单
     *             2.班长权限 根据facultyId获取班级名称
     *             3.班主任权限 获取院系列表
     *             4.获取该院系所有班级的id
     *             5.毕设老师权限 获取所有院系的名称
     *             6.毕设老师权限 获取该院系所有班级的id
     *             7.获取班级全部成员
     *             8.辅导员权限 获取院系列表
     *             9.获取所有班级id
     *             10.学院实习工作负责人权限 获取院系列表
     *             11.学生工作负责人权限 获取院系列表
     *             12.教学院长权限 获取院系列表
     *             20.部门请假负责人权限 获取所有部门的id
     *             21.部门教学院长权限 获取所有部门的id
     */
    public void getApplyList(Number extendId1, String extendId2,int type){
        if(type ==0||type==2) extendId1 = (extendId1.intValue()/10000)*100; //例151110401 则 1511100
        HashMap<String,Object> map = new HashMap<>();
        map.put("extendId1",extendId1);
        map.put("extendId2",extendId2);
        map.put("type",type);
        ViseHttp.POST(HttpConst.getApplyListInfo)
                .tag(HttpConst.getAuthorityDetailInfo)
                .setJson(new Gson().toJson(map))
                .request(new ACallback<BaseResponse<List<String>>>() {
                    @Override
                    public void onSuccess(BaseResponse<List<String>> data) {
                        switch (data.getCode()){
                            case "200":
                                data.setExtendInfo(type);
                                SmecRxBus.get().post(MainPresenter.SUCCESSFUL_GET_APPLY_LIST_INFO,  data);
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
    //提交权限请求
    public void submitAuthoritiesApply(List<ApplyInfo> applyList, String applyReason){
        for(ApplyInfo applyInfo:applyList){
         applyInfo.setApplyReason(applyReason.trim());
         }
        ViseHttp.POST(HttpConst.submitAuthoritiesApply)
                .tag(HttpConst.getAuthorityDetailInfo)
                .setJson(new Gson().toJson(applyList))
                .request(new ACallback<BaseResponse<List<String>>>() {
                    @Override
                    public void onSuccess(BaseResponse<List<String>> data) {
                        switch (data.getCode()){
                            case "200":
                                data.setExtendInfo(100);
                                SmecRxBus.get().post(MainPresenter.SUCCESSFUL_GET_APPLY_LIST_INFO,  data);
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
        super.unsubscribe();
        ViseHttp.cancelTag(HttpConst.getAuthorityDetailInfo);
    }
}