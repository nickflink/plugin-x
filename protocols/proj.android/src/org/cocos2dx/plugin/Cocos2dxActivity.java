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


//import org.cocos2dx.plugin.SocialGPGS;
//import org.cocos2dx.plugin.ShareFacebook;
//import org.cocos2dx.libSocialGPGS.SocialGPGS;
//import org.cocos2dx.lib.Cocos2dxHelper;

public class Cocos2dxActivity extends NativeActivity {

  private static final String TAG = "Cocos2dxActivity";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    //Context ctx = (Context)this;
    //SocialGPGS sgps = SocialGPGS(ctx);
    //Log.i(TAG, "SocialGPGS "+sgps+" CREATED");
    //ShareFacebook facebook = ShareFacebook(ctx);
    //Log.i(TAG, "ShareFacebook "+facebook+" CREATED");
    //String classFullName = "org/cocos2dx/plugin/SocialGPGS";
    //Class<?> c = null;
    //try {
    //    String fullName = classFullName.replace('/', '.');
    //    c = Class.forName(fullName);
    //} catch (ClassNotFoundException e) {  
    //    Log.e(TAG, "Class " + classFullName + " not found.");
    //    e.printStackTrace();
    //}
    //if(c != null) {
    //    try {
    //        if (ctx != null) {
    //            Object o = c.getDeclaredConstructor(Context.class).newInstance(ctx);
    //            if(o != null) {
    //                Log.i(TAG, "Load "+classFullName+" SUCCESS");
    //            }
    //        } else {
    //            Log.e(TAG, "Plugin " + classFullName + " wasn't initialized.");
    //        }
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //}
    
    //For supports translucency
    
    //1.change "attribs" in cocos\2d\platform\android\nativeactivity.cpp
    /*const EGLint attribs[] = {
              EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
              EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,  
              //EGL_BLUE_SIZE, 5,   -->delete 
              //EGL_GREEN_SIZE, 6,  -->delete 
              //EGL_RED_SIZE, 5,    -->delete 
              EGL_BUFFER_SIZE, 32,  //-->new field
              EGL_DEPTH_SIZE, 16,
              EGL_STENCIL_SIZE, 8,
              EGL_NONE
      };*/
    
    //2.Set the format of window
    // getWindow().setFormat(PixelFormat.TRANSLUCENT);
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

