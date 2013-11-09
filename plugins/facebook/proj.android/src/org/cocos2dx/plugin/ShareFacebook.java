/****************************************************************************
Copyright (c) 2013 gogododo
Copyright (c) 2013 Nicholas Flink
****************************************************************************/
package org.cocos2dx.plugin;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Thread;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class ShareFacebook implements InterfaceShare {

	private static final String TAG = "ShareFacebook";
	private static final int AUTH_ACTIVITY_CODE = 100;
	private static final int REAUTH_ACTIVITY_CODE = 101;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static Activity mActivity = null;
	private static InterfaceShare mShareAdapter = null;
	protected static boolean bDebug = false;
	protected static boolean bAsyncRunning = false;
	private static String CONSUMER_KEY="";
	private static String CONSUMER_SECRET="";
	private static boolean mIsInitialized = false;
	private static Hashtable<String, String> mShareInfo = null;
	private Handler mHandler;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	public static String KEY_TEXT="SharedText";
	public static String KEY_IMAGE_PATH = "SharedImagePath";

	protected static void LogE(String msg, Exception e) {
		Log.e(TAG, msg, e);
		e.printStackTrace();
	}

	protected static void LogD(String msg) {
		if (bDebug) {
			int depth = 3;
			Log.d(TAG, Thread.currentThread().getStackTrace()[depth].getFileName()+":"+
				Thread.currentThread().getStackTrace()[depth].getLineNumber()+":"+
				Thread.currentThread().getStackTrace()[depth].getMethodName()+": "+msg);
		}
	}
	public static boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		boolean handled = false;
		if(requestCode == AUTH_ACTIVITY_CODE) {
			Session session = Session.getActiveSession();
			if(session != null) {
				session.onActivityResult(activity, requestCode, resultCode, data);
				handled = true;
			}
		}
		return handled;
	}

	public static String stateToString(SessionState state) {
		String stateString = "SessionState.UNKNOWN";
		switch(state) {
			case CREATED:
				stateString = "SessionState.CREATED";
			break;
			case CREATED_TOKEN_LOADED:
				stateString = "SessionState.CREATED_TOKEN_LOADED";
			break;
			case OPENING:
				stateString = "SessionState.OPENING";
			break;
			case OPENED:
				stateString = "SessionState.OPENED";
			break;
			case OPENED_TOKEN_UPDATED:
				stateString = "SessionState.OPENED_TOKEN_UPDATED";
			break;
			case CLOSED_LOGIN_FAILED:
				stateString = "SessionState.CLOSED_LOGIN_FAILED";
			break;
			case CLOSED:
				stateString = "SessionState.CLOSED";
			break;
		}
		return stateString;
	}

	public ShareFacebook(Context context) {
		mShareAdapter = this;
		mActivity = (Activity) context;
		PluginWrapper.runOnMainThread(new Runnable() {
			@Override
			public void run() {
				mHandler = new Handler();
			}
		});
	}
	

	@Override
	public void configDeveloperInfo(Hashtable<String, String> cpInfo) {
		LogD("configDeveloperInfo invoked " + cpInfo.toString());
		try {
			if(!mIsInitialized){
				ShareFacebook.CONSUMER_KEY = cpInfo.get("FacebookAppKey");
				ShareFacebook.CONSUMER_SECRET = cpInfo.get("FacebookAppSecret");
				LogD("key : " + ShareFacebook.CONSUMER_KEY);
				LogD("secret : " + ShareFacebook.CONSUMER_SECRET);
				login();
				mIsInitialized = true;	
			}
		} catch (Exception e) {
			LogE("configDeveloperInfo failed!", e);
		}
	}

	private void login() {
		Session session = Session.getActiveSession();
		LogD("Session.getActiveSession "+java.lang.System.identityHashCode(session));
		if (session == null) {
			LogD("new Session");
			session = new Session.Builder((Context)mActivity)
				.setApplicationId(ShareFacebook.CONSUMER_KEY)
				.build();
			LogD("openForRead");
			Session.OpenRequest openRequest = new Session.OpenRequest(mActivity)
				.setCallback(statusCallback)
				.setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK)//SUPPRESS_SSO)
				.setRequestCode(AUTH_ACTIVITY_CODE)
				.setPermissions(PERMISSIONS)
				.setDefaultAudience(SessionDefaultAudience.FRIENDS);
			session.openForPublish(openRequest);
			LogD("Session.setActiveSession "+java.lang.System.identityHashCode(session));
			Session.setActiveSession(session);
			session = Session.getActiveSession();
			LogD("Session.getActiveSession "+java.lang.System.identityHashCode(session));
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			LogD(ShareFacebook.stateToString(state));
			Session activeSession = Session.getActiveSession();
			LogD("Session.getActiveSession "+java.lang.System.identityHashCode(activeSession));
			if(session != activeSession) {
				LogD("Our changing sessions isn't the active session maybe an error");
				LogD("Session.setActiveSession "+java.lang.System.identityHashCode(session));
				Session.setActiveSession(session);
			}
			if (session.isOpened()) {
				LogD("opened need to add permissions!");
			} else {
				LogD("Need to login my session!");
			}
		}
	}

	@Override
	public void share(Hashtable<String, String> info) {
		if (bAsyncRunning == false && networkReachable() && mIsInitialized) {
			LogD("share invoked " + info.toString());
			mShareInfo =  info;
			String strImagePath = info.get("SharedImagePath");
			Session session = Session.getActiveSession();
			LogD("Session.getActiveSession "+java.lang.System.identityHashCode(session));
			if (session != null && session.getState() == SessionState.OPENED) {
				LogD("uploading photo");
				if (strImagePath != null) {
					LogD("opening photo using AssetManager"+strImagePath);
					final File file = new File(strImagePath);
					if(file != null) {
							mActivity.runOnUiThread(new Runnable() {
								public void run() {
									try {
										LogD("start Request to upload a photo");
										bAsyncRunning = true;
										Session session = Session.getActiveSession();
										Request.Callback callback = new Request.Callback() {
											@Override
											public void onCompleted(Response response) {
												LogD("uploading photo completed");
												bAsyncRunning = false;
											}
										};
										RequestAsyncTask asyncReq = Request.executeUploadPhotoRequestAsync(session, file, callback);
									} catch (FileNotFoundException e) {
										LogD("file not found");
									}
								}
							});
							
					} else {
						LogD("file not valid");
					}
				} else {
					LogD("strImagePath not valid");
				}
			} else {
				if(session == null) {
					LogD("can not upload photo session null");
				} else {
					LogD("can not upload photo session "+java.lang.System.identityHashCode(session)+" in wrong state:"+ShareFacebook.stateToString(session.getState()));
					Session activeSession = Session.getActiveSession();
					LogD("Session.getActiveSession "+java.lang.System.identityHashCode(activeSession));
					if(session != activeSession) {
						LogD("Session.setActiveSession "+java.lang.System.identityHashCode(session));
						Session.setActiveSession(session);
						LogD("session is not the activeSession this causes errors");
					}
				}
			}
		} else {
			if (bAsyncRunning == false) {
				shareResult(ShareWrapper.SHARERESULT_FAIL, "Async task already running!");
				return;
			}
			if (! networkReachable()) {
				shareResult(ShareWrapper.SHARERESULT_FAIL, "Network error!");
				return;
			}
			if (! mIsInitialized) {
				shareResult(ShareWrapper.SHARERESULT_FAIL, "Initialize failed!");
				return;
			}
		}
	}

	@Override
	public void setDebugMode(boolean debug) {
		if(debug) {
			LogD("ShareFacebook setDebugMode true");
		} else {
			LogD("ShareFacebook setDebugMode false");
		}
		bDebug = debug;
	}

	@Override
	public String getSDKVersion() {
		return "Unknown version";
	}

	private boolean networkReachable() {
		LogD("ShareFacebook networkReachable");
		boolean bRet = false;
		try {
			ConnectivityManager conn = (ConnectivityManager)mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = conn.getActiveNetworkInfo();
			bRet = (null == netInfo) ? false : netInfo.isAvailable();
		} catch (Exception e) {
			LogE("Fail to check network status", e);
		}
		LogD("NetWork reachable : " + bRet);
		return bRet;
	}

	private static void shareResult(int ret, String msg) {
		ShareWrapper.onShareResult(mShareAdapter, ret, msg);
		LogD("ShareFacebook result : " + ret + " msg : " + msg);
	}

	private static void sendToFacebook() {
		LogD("ShareFacebook sendToFacebook");
	}

	@Override
	public String getPluginVersion() {
		return "0.2.0";
	}

}
