package com.epkj.mall;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


public class UpdateManager {
	// Ӧ�ó���Context
	private Context mContext;
	// �Ƿ������µ�Ӧ��,Ĭ��Ϊfalse
	private boolean isNew = false;
	private boolean intercept = false;
	// ���ذ�װ��������·��
	private String apkUrl = "https://mall.epaikj.com/upload/download/"
			+ "mall.apk";
	// ����APK���ļ���
	private static final String savePath = "/sdcard/updatedemo/";
	private static final String saveFileName = savePath
			+ "UpdateDemoRelease.apk";
	// �����߳�
	private Thread downLoadThread;
	private int progress;// ��ǰ����
	TextView text;
	// ��������֪ͨUIˢ�µ�handler��msg����
	private String alertMsg = "�����";
	private ProgressBar mProgress;
	private static final int DOWN_UPDATE = 1;
	private static final int DOWN_OVER = 2;
	private HttpConnectionUtil httpConnect = HttpConnectionUtil.getHttp();
	public UpdateManager(Context context) {
		mContext = context;
	}

	/**
	 * ����Ƿ���µ�����
	 */
	public void checkUpdateInfo() {
		
		checkStatus();
		// �����isNew������Ҫ�ӷ�������ȡ�ģ����������ȼ�������Ҫ����
		if (isNew) {
			return;
		} else {
			showUpdateDialog();
		}
	}
	
	public void checkStatus(){
		String versionCode = APKVersionCodeUtils.getVersionCode(mContext)+"";
    	String versionName = APKVersionCodeUtils.getVerName(mContext);
    	System.out.println("��8"+versionCode);
		Map<String, String> map = new HashMap<>();
        map.put("code", versionCode);
        map.put("name", versionName);
        String y = httpConnect.postRequset("https://mall.epaikj.com/wap/test2.php", map);
        JSONObject json = 	JSON.parseObject(y);
        String errors = json.getString("error");
        alertMsg = json.getString("msg");
        if(errors.equals("0")){
        	isNew = false;
        }else{
        	isNew = true; 
        }
	}

	/**
	 * ��ʾ���³���Ի��򣬹����������
	 */
	private void showUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.update_manage_dialog, null);
        TextView content = (TextView) v.findViewById(R.id.dialog_content);
        content.setText(alertMsg);
        Button btn_sure = (Button) v.findViewById(R.id.dialog_btn_sure);
        Button btn_cancel = (Button) v.findViewById(R.id.dialog_btn_cancel);
        //builer.setView(v);//�������ʹ��builer.setView(v)���Զ��岼��ֻ�Ḳ��title��button֮����ǲ���
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(v);//�Զ��岼��Ӧ����������ӣ�Ҫ��dialog.show()�ĺ���
        //dialog.getWindow().setGravity(Gravity.CENTER);//����������ʾ��λ��
        btn_sure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	showDownloadDialog();
                dialog.dismiss();     
            }
        });

        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
               
            }
        });
	}

	/**
	 * ��ʾ���ؽ��ȵĶԻ���
	 */
	private void showDownloadDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("����汾����");
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.progress);

		builder.setView(v);
		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				intercept = true;
			}
		});
		builder.show();
		downloadApk();
	}

	/**
	 * �ӷ���������APK��װ��
	 */
	private void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}

	private Runnable mdownApkRunnable = new Runnable() {

		@Override
		public void run() {
			URL url;
			try {
				url = new URL(apkUrl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream ins = conn.getInputStream();
				File file = new File(savePath);
				if (!file.exists()) {
					file.mkdir();
				}
				File apkFile = new File(saveFileName);
				FileOutputStream fos = new FileOutputStream(apkFile);
				int count = 0;
				byte[] buf = new byte[1024];
				while (!intercept) {
					int numread = ins.read(buf);
					count += numread;
					progress = (int) (((float) count / length) * 100);

					// ���ؽ���
					mHandler.sendEmptyMessage(DOWN_UPDATE);
					if (numread <= 0) {
						// �������֪ͨ��װ
						mHandler.sendEmptyMessage(DOWN_OVER);
						break;
					}
					fos.write(buf, 0, numread);
				}
				fos.close();
				ins.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * ��װAPK����
	 */
	private void installAPK() {
		File apkFile = new File(saveFileName);
		if (!apkFile.exists()) {
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + apkFile.toString()),
				"application/vnd.android.package-archive");
		mContext.startActivity(intent);
		android.os.Process.killProcess(android.os.Process.myPid());
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {

			case DOWN_UPDATE:
				mProgress.setProgress(progress);
				break;

			case DOWN_OVER:
				installAPK();
				break;

			default:
				break;
			}
		}

	};
}