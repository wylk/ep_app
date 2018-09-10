package com.epkj.mall.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	
	private IWXAPI api;
	public static final String ACTION = "com.kingkr.webapp.activity.MainActivity.PAY";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        System.out.println("´ó2");
        //setContentView(R.layout.pay_result);
    	api = WXAPIFactory.createWXAPI(this, "wxf84c1d0aea5d3a70");
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		
	}

	@Override
	public void onResp(BaseResp resp) {
		if(resp.getType()==ConstantsAPI.COMMAND_PAY_BY_WX){
			System.out.println("´ó3");
		    Intent localIntent = new Intent(ACTION);
		    localIntent.putExtra("code", resp.errCode + "");
		    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
		    finish();
        }
	}
}