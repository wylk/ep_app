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
 * �Զ��������
 * 
 * ������������ Receiver����
 * 1) Ĭ���û����������
 * 2) ���ղ����Զ�����Ϣ
 */
public class MyReceiver extends BroadcastReceiver {
	private static final String TAG = "JPush";
	private static final String NOTIFICATION_SERVICE = null;
	private SpeechSynthesizer mTts;
 
	@Override
	public void onReceive(Context context, Intent intent) {
		
		mTts = SpeechSynthesizer.createSynthesizer(context, null);
		mTts.setParameter(SpeechConstant.VOICE_NAME,"xiaoyan");//���÷����� 
		mTts.setParameter(SpeechConstant.SPEED,"50");//��������  
		mTts.setParameter(SpeechConstant.VOLUME,"100");//������������Χ0~100 
		
        Bundle bundle = intent.getExtras();
		//Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
		
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] ����Registration Id : " + regId);
           
            //send the Registration Id to your server...
                        
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	Log.d(TAG, "[MyReceiver] ���յ������������Զ�����Ϣ: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
        	
        	
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] ���յ�����������֪ͨ");
        	int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "[MyReceiver] ����յ�����������֪ͨ��ID: " + notifactionId);
            //String path=bundle.getString(JPushInterface.EXTRA_ALERT);
            processCustomMessage(context, bundle);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] �û��������֪ͨ");
        	//���Զ����Activity
        	Intent i = new Intent(context, MainActivity.class);
        	i.putExtras(bundle);
        	//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
        	context.startActivity(i);
        	
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] �û��յ���RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //��������� JPushInterface.EXTRA_EXTRA �����ݴ�����룬������µ�Activity�� ��һ����ҳ��..
        	
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
						Log.d(TAG, "��1" + message);
					} else if ("2".equals(type)) {
						Log.d(TAG, "��" + txt);
						mTts.startSpeaking(txt, mTtsListener);
					}

				}
			} catch (JSONException e) {

			}

		}
	}
	
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		//�Ự�����ص��ӿڣ�û�д���ʱ��errorΪnull  
		@Override
		public void onCompleted(SpeechError error) {
			Log.i("info","error"+error);
		}  
		//������Ȼص�  
		//percentΪ�������0~100��beginPosΪ������Ƶ���ı��п�ʼλ�ã�endPos��ʾ������Ƶ���ı��н���λ�ã�infoΪ������Ϣ��  
		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos, String info) {}
		//��ʼ����  
		@Override
		public void onSpeakBegin() {}
		//��ͣ����  
		@Override
		public void onSpeakPaused() {}
		//���Ž��Ȼص�  
		//percentΪ���Ž���0~100,beginPosΪ������Ƶ���ı��п�ʼλ�ã�endPos��ʾ������Ƶ���ı��н���λ��.  
		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {}
		//�ָ����Żص��ӿ�  
		@Override
		public void onSpeakResumed() {}
		//�Ự�¼��ص��ӿ�  
		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}
	};
		
}


