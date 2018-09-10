package com.epkj.mall;
 

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
 
/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {
	private static final String TAG = "JPush";
	private static final String NOTIFICATION_SERVICE = null;
	private SpeechSynthesizer mTts;
 
	@Override
	public void onReceive(Context context, Intent intent) {
		
		mTts = SpeechSynthesizer.createSynthesizer(context, null);
		mTts.setParameter(SpeechConstant.VOICE_NAME,"xiaoyan");//设置发音人 
		mTts.setParameter(SpeechConstant.SPEED,"50");//设置语速  
		mTts.setParameter(SpeechConstant.VOLUME,"100");//设置音量，范围0~100 
		
        Bundle bundle = intent.getExtras();
		//Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
		
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
           
            //send the Registration Id to your server...
                        
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
        	
        	
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
        	int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "[MyReceiver] 大接收到推送下来的通知的ID: " + notifactionId);
            //String path=bundle.getString(JPushInterface.EXTRA_ALERT);
            processCustomMessage(context, bundle);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
        	//打开自定义的Activity
        	Intent i = new Intent(context, MainActivity.class);
        	i.putExtras(bundle);
        	//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
        	context.startActivity(i);
        	
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
        	
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
        	boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
        	Log.w(TAG, "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
        } else {
        	Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
	}
	
	//send msg to MainActivity
	private void processCustomMessage(Context context, Bundle bundle) {

		String message = bundle.getString(JPushInterface.EXTRA_ALERT);
		String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
		if (!TextUtils.isEmpty(extras)) {
			try {
				JSONObject extraJson = new JSONObject(extras);
				if (null != extraJson && extraJson.length() > 0) {
					String txt = extraJson.getString("txt");
					String type = extraJson.getString("type");
					if ("1".equals(type)) {
						mTts.startSpeaking(message, mTtsListener);
						Log.d(TAG, "大1" + message);
					} else if ("2".equals(type)) {
						Log.d(TAG, "大" + txt);
						mTts.startSpeaking(txt, mTtsListener);
					}

				}
			} catch (JSONException e) {

			}

		}
	}
	
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		//会话结束回调接口，没有错误时，error为null  
		@Override
		public void onCompleted(SpeechError error) {
			Log.i("info","error"+error);
		}  
		//缓冲进度回调  
		//percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。  
		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos, String info) {}
		//开始播放  
		@Override
		public void onSpeakBegin() {}
		//暂停播放  
		@Override
		public void onSpeakPaused() {}
		//播放进度回调  
		//percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.  
		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {}
		//恢复播放回调接口  
		@Override
		public void onSpeakResumed() {}
		//会话事件回调接口  
		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}
	};
		
}


