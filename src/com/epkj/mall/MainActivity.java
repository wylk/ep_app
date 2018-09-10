package com.epkj.mall;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;

public class MainActivity extends Activity implements TencentLocationListener {

	public static final Intent MESSAGE_RECEIVED_ACTION = null;
	public static final String KEY_EXTRAS = null;
	public static boolean isForeground;
	public static String KEY_MESSAGE;
	public static final String ACTION = "com.kingkr.webapp.activity.MainActivity.PAY";
	private static Boolean isExit = false;
	public static String jsCallbackMethod;
	private BroadcastReceiver localReceiver;
	private TencentLocation mLocation;
	private TencentLocationManager mLocationManager;
	private static final int SDK_PAY_FLAG = 1;
	private TextView lblTitle; 
	com.tencent.smtt.sdk.WebView tbsContent;
	private String url = "https://mall.epaikj.com/wap/index.php";

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		lblTitle = (TextView)findViewById(R.id.lblTitle);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		initView();
		iniGps();
		iniBroadcastre();
		iniUpdata();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initView() {
		tbsContent = (com.tencent.smtt.sdk.WebView) findViewById(R.id.webView);
		tbsContent.loadUrl(url);

		WebSettings webSettings = tbsContent.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setUseWideViewPort(true);
		webSettings .setUseWideViewPort(true);
		webSettings .setLoadsImagesAutomatically(true);
		webSettings.setGeolocationEnabled(true);
		
		tbsContent.addJavascriptInterface(new AndroidtoJs(), "EP");
		tbsContent.setWebChromeClient(new WebChromeClient());
		tbsContent.setWebViewClient(new SimpleWebViewClient(this,lblTitle));

		tbsContent.setWebChromeClient(new WebChromeClient() {
			// 这里可以设置进度条。但我是用另外一种

			public void onProgressChanged(com.tencent.smtt.sdk.WebView webView,
					int i) {
				super.onProgressChanged(webView, i);
			}
		});
	}
	
	public void webReload(View view){
		Toast.makeText(MainActivity.this, "刷新", Toast.LENGTH_SHORT).show();
		tbsContent.reload();
	}

	public static class SimpleWebViewClient extends
			com.tencent.smtt.sdk.WebViewClient {
		/**
		 * 防止加载网页时调起系统浏览器
		 */
		private Context sContext;
		private YWLoadingDialog mDialog = null;
		private TextView lblTitle;

		public SimpleWebViewClient(Context context,TextView lblTitle) {
			sContext = context;
			mDialog = new YWLoadingDialog(sContext);
			this.lblTitle = lblTitle;
		}

		@Override
		public boolean shouldOverrideUrlLoading(
				com.tencent.smtt.sdk.WebView webView, String url) {
			webView.loadUrl(url);
			return true;
		}

		// 在开始的时候，开始loadingDialog
		@Override
		public void onPageStarted(com.tencent.smtt.sdk.WebView webView,
				String s, Bitmap bitmap) {
			super.onPageStarted(webView, s, bitmap);
			try {
				mDialog.show();
				System.out.println("大1");
			} catch (Exception e) {
			}
		}

		// 在页面加载结束的时候，关闭LoadingDialog
		@Override
		public void onPageFinished(com.tencent.smtt.sdk.WebView webView,
				String s) {
			super.onPageFinished(webView, s);
			try {
				String title = webView.getTitle();
				this.lblTitle.setText(title);
				mDialog.dismiss();
				System.out.println("大2");
			} catch (Exception e) {
			}
		}

		@Override
		public void onReceivedError(
				com.tencent.smtt.sdk.WebView webView,
				com.tencent.smtt.export.external.interfaces.WebResourceRequest webResourceRequest,
				com.tencent.smtt.export.external.interfaces.WebResourceError webResourceError) {
			super.onReceivedError(webView, webResourceRequest, webResourceError);
		}

		@Override
		public void onReceivedSslError(
				com.tencent.smtt.sdk.WebView webView,
				com.tencent.smtt.export.external.interfaces.SslErrorHandler sslErrorHandler,
				com.tencent.smtt.export.external.interfaces.SslError sslError) {
			sslErrorHandler.proceed();
		}

	}

	public void iniGps() {
		mLocationManager = TencentLocationManager.getInstance(this);
		mLocationManager.requestLocationUpdates(TencentLocationRequest.create()
				.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME)
				.setInterval(500).setAllowDirection(true), this);
	}

	// android to Js 交互
	public class AndroidtoJs {

		@JavascriptInterface
		public void clickOnAndroid(String msg, final String backfun) {
			JSONObject json = JSON.parseObject(msg);
			final String name = json.getString("name");
			backJs(name);
			Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
		}

		@JavascriptInterface
		public void Pay(final String payinfo, String PayType,
				final String backfun) {
			MainActivity.jsCallbackMethod = backfun;

			Toast.makeText(MainActivity.this, "调起支付！", Toast.LENGTH_SHORT)
					.show();
			if (PayType.equals("weixin")) {
				IWXAPI api = WXAPIFactory.createWXAPI(MainActivity.this,
						"wxf84c1d0aea5d3a70", false);// 填写自己的APPID
				api.registerApp("wxf84c1d0aea5d3a70");// 填写自己的APPID，注册本身APP
				PayReq req = new PayReq();// PayReq就是订单信息对象
				// 给req对象赋值
				JSONObject json = JSON.parseObject(payinfo);
				req.appId = json.getString("appid");// APPID
				req.partnerId = json.getString("partnerid");// 商户号
				req.prepayId = json.getString("prepayid");// 预付款ID
				req.nonceStr = json.getString("noncestr");// 随机数
				req.timeStamp = json.getString("timestamp");// 时间戳
				req.packageValue = json.getString("package");// 固定值Sign=WXPay
				req.sign = json.getString("sign");// 签名
				System.out.println("大1");
				api.sendReq(req);
			} else if (PayType.equals("alipay")) {
				Runnable payRunnable = new Runnable() {
					@Override
					public void run() {
						// 构造PayTask 对象
						PayTask alipay = new PayTask(MainActivity.this);
						// 调用支付接口，获取支付结果
						String result = alipay.pay(payinfo, true);
						Message msg = new Message();
						msg.what = SDK_PAY_FLAG;
						msg.obj = result;
						mHandler.sendMessage(msg);
					}
				};

				// 必须异步调用
				Thread payThread = new Thread(payRunnable);
				payThread.start();
				// backJs("支付宝支付还在开发中!");
			} else {
				backJs("支付类型不存在!");
			}
		}

		@JavascriptInterface
		public void PushTag(String Tag, final String backfun) {
			MainActivity.jsCallbackMethod = backfun;
			// 建议添加tag标签，发送消息的之后就可以指定tag标签来发送了
			if (Tag == null) {
				backJs("标签不能为空");
			} else {
				Toast.makeText(MainActivity.this, "正在添加标签！", Toast.LENGTH_SHORT)
						.show();
				Set<String> set = new HashSet<>();
				set.add(Tag);// 名字任意，可多添加几个
				TagAliasCallback callback = new TagAliasCallback() {
					@Override
					public void gotResult(int responseCode, String alias,
							Set<String> tags) {
						String code = String.valueOf(responseCode);
						backJs(code);

					}
				};
				JPushInterface.setTags(MainActivity.this, set, callback);// 设置标签
			}
		}

		@JavascriptInterface
		public void GetLocation(final String backfun) {
			MainActivity.jsCallbackMethod = backfun;
			System.out.println("大6");
			System.out.println("大7" + mLocation.getLatitude());

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("Longitude", mLocation.getLongitude());
			jsonObject.put("Latitude", mLocation.getLatitude());
			jsonObject.put("Provider", mLocation.getProvider());
			jsonObject.put("Address", mLocation.getAddress());
			String locationMsg = jsonObject.toString();
			backJs(locationMsg);
		}

		@JavascriptInterface
		public void GetDeviceId(final String backfun) {
			MainActivity.jsCallbackMethod = backfun;
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String DEVICE_ID = tm.getDeviceId();
			backJs(DEVICE_ID);
		}

	}

	// 处理给js返回的值
	public void backJs(final String reJs) {
		runOnUiThread(new Runnable() {
			public void run() {
				tbsContent.loadUrl("javascript:"
						+ MainActivity.jsCallbackMethod + "('" + reJs + "')");
			}
		});
	}

	// 版本更新
	public void iniUpdata() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				UpdateManager manager = new UpdateManager(MainActivity.this);
				manager.checkUpdateInfo();
			}
		}, 7000);//
	}

	public void iniBroadcastre() {
		localReceiver = new BroadcastReceiver() {
			@Override
			@JavascriptInterface
			public void onReceive(Context context, final Intent intent) {
				System.out.println("大5" + intent.getStringExtra("code"));
				backJs(intent.getStringExtra("code"));
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver,
				new IntentFilter(ACTION));
	}

	public void onBackPressed() {

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && tbsContent.canGoBack()) {
			tbsContent.goBack();// 返回前一个页面
			// return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click();
		}
		return false;
		// return super.onKeyDown(keyCode, event);
	}

	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // 准备退出
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // 取消退出
				}
			}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

		} else {
			finish();
			System.exit(0);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (localReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					localReceiver);
		}
		mLocationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(TencentLocation location, int error,
			String reason) {
		// TODO Auto-generated method stub

		if (error == TencentLocation.ERROR_OK) {
			// 定位成功
			mLocation = location;
			StringBuilder sb = new StringBuilder();
			sb.append("(纬度=").append(location.getLatitude()).append(",经度=")
					.append(location.getLongitude()).append(",精度=")
					.append(location.getAccuracy()).append("), 来源=")
					.append(location.getProvider()).append(", 地址=")
					.append(location.getAddress());
			// 更新 status
			System.out.println(sb.toString());
		}

	}

	@Override
	public void onStatusUpdate(String arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@SuppressWarnings("unused")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SDK_PAY_FLAG: {
				PayResult payResult = new PayResult((String) msg.obj);
				/**
				 * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
				 * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
				 * docType=1) 建议商户依赖异步通知
				 */
				String resultInfo = payResult.getResult();// 同步返回需要验证的信息

				String resultStatus = payResult.getResultStatus();
				// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
				if (TextUtils.equals(resultStatus, "9000")) {
					backJs("0");
				} else {
					// 判断resultStatus 为非"9000"则代表可能支付失败
					// "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						backJs("3");

					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						backJs("1");

					}
				}
				break;
			}
			default:
				break;
			}
		}
	};

}
