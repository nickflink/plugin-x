package org.cocos2dx.plugin;

import android.app.NativeActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.util.Log;

import java.util.LinkedHashSet;
import java.util.Set;

import org.cocos2dx.lib.Cocos2dxActivity;
//START FUCKING ABOUT
//import org.cocos2dx.plugin.PluginWrapper;
//import org.cocos2dx.plugin.AdsFlurry;
//END FUCKING ABOUT

//import org.cocos2dx.plugin.SocialGPGS;
//import org.cocos2dx.plugin.ShareFacebook;
//import org.cocos2dx.libSocialGPGS.SocialGPGS;
//import org.cocos2dx.lib.Cocos2dxHelper;

public class Cocos2dxPluginActivity extends Cocos2dxActivity {

  private static final String TAG = "Cocos2dxPluginActivity";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Init plugins
    PluginWrapper.init(this);
  }

  private static Set<OnActivityResultListener> onActivityResultListeners = new LinkedHashSet<OnActivityResultListener>();

  public static void addOnActivityResultListener(OnActivityResultListener listener) {
    onActivityResultListeners.add(listener);
  }

  public static Set<OnActivityResultListener> getOnActivityResultListeners() {
    return onActivityResultListeners;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    for (OnActivityResultListener listener : getOnActivityResultListeners()) {
      listener.onActivityResult(requestCode, resultCode, data);
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}

