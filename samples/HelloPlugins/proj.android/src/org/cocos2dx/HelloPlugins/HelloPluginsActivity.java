package org.cocos2dx.HelloPlugins;

import android.app.NativeActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.util.Log;

import java.util.LinkedHashSet;
import java.util.Set;

import org.cocos2dx.plugin.Cocos2dxActivity;
import org.cocos2dx.plugin.IAPGooglePlay;

//import org.cocos2dx.plugin.SocialGPGS;
//import org.cocos2dx.plugin.ShareFacebook;
//import org.cocos2dx.libSocialGPGS.SocialGPGS;
//import org.cocos2dx.plugin.IAPGooglePlay;
//import org.cocos2dx.lib.Cocos2dxHelper;

public class HelloPluginsActivity extends Cocos2dxActivity {

  private static final String TAG = "HelloPluginsActivity";
  private static IAPGooglePlay mIAPGooglePlay = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      mIAPGooglePlay = new IAPGooglePlay(this);
  }
}
