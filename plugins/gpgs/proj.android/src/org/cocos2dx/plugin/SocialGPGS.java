/****************************************************************************
Copyright (c) 2013 cocos2d-x.org

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/
package org.cocos2dx.plugin;

import java.util.Hashtable;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.OnSignOutCompleteListener;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.plus.PlusClient;

public class SocialGPGS implements InterfaceSocial, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, OnSignOutCompleteListener {

    private static final String TAG = "SocialGPGS";
    private static Activity mActivity = null;
    private static SocialGPGS mGPGS = null;
    private static boolean bDebug = false;

    protected static void LogE(String msg, Exception e) {
        Log.e(TAG, msg, e);
        e.printStackTrace();
    }

    protected static void LogD(String msg) {
        if (bDebug) {
            Log.d(TAG, msg);
        }
    }

    public SocialGPGS(Context context) {
        mActivity = (Activity) context;
        mGPGS = this;
        //mActivity = activity;
        setup();
        //These should be localized
        setLocalizedMessages("NL: signing in", "NL: signing out", "NL: unknown error");
        LogD("setup a submitListener");
        //submitListener = new NdCallbackListener<Object>(){
        //    @Override
        //    public void callback(int arg0, Object arg1) {
        //        int nRet = SocialWrapper.SOCIAL_SUBMITSCORE_FAILED;
        //        String msg = "Unknow Error";
        //        switch (arg0) {
        //        case NdCommplatform.SCORE_SUBMIT_SUCCESS:
        //            nRet = SocialWrapper.SOCIAL_SUBMITSCORE_SUCCESS;
        //            msg = "Submit Success";
        //            break;
        //        case NdCommplatform.SCORE_SAVE_LOCAL:
        //            nRet = SocialWrapper.SOCIAL_SUBMITSCORE_FAILED;
        //            msg = "Score saved locally";
        //            break;
        //        case NdCommplatform.LEADERBOARD_NOT_EXIST:
        //            nRet = SocialWrapper.SOCIAL_SUBMITSCORE_FAILED;
        //            msg = "The leaderboard not exist";
        //            break;
        //        default:
        //            nRet = SocialWrapper.SOCIAL_SUBMITSCORE_FAILED;
        //            break;
        //        }
        //        SocialWrapper.onSocialResult(mGPGS, nRet, msg);
        //    }
        //};

        LogD("setup an unlockListener");
        //unlockListener = new NdCallbackListener<Object>(){
        //    @Override
        //    public void callback(int arg0, Object arg1) {
        //        int nRet = SocialWrapper.SOCIAL_UNLOCKACH_FAILED;
        //        String msg = "Unknow Error";
        //        switch (arg0) {
        //        case NdErrorCode.ND_COM_PLATFORM_SUCCESS:
        //            nRet = SocialWrapper.SOCIAL_UNLOCKACH_SUCCESS;
        //            msg = "Unlock Success";
        //            break;
        //        case NdErrorCode.ND_COM_PLATFORM_ERROR_SERVER_RETURN_ERROR:
        //            nRet = SocialWrapper.SOCIAL_UNLOCKACH_FAILED;
        //            msg = "Server return error";
        //            break;
        //        case NdErrorCode.ND_COM_PLATFORM_ERROR_ACHIEVEMENT_NO_EXIST:
        //            nRet = SocialWrapper.SOCIAL_UNLOCKACH_FAILED;
        //            msg = "Achievement not exist";
        //            break;
        //        default:
        //            nRet = SocialWrapper.SOCIAL_UNLOCKACH_FAILED;
        //            break;
        //        }
        //        SocialWrapper.onSocialResult(mGPGS, nRet, msg);
        //    }
        //};
    }


    @Override
    public void configDeveloperInfo(Hashtable<String, String> cpInfo) {
        LogD("initDeveloperInfo invoked " + cpInfo.toString());
        final Hashtable<String, String> curCPInfo = cpInfo;
        PluginWrapper.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String appId = curCPInfo.get("GPGSAppId");
                    String appKey = curCPInfo.get("GPGSAppKey");
                    String signInMsg = curCPInfo.get("GPGSSignInMsg");
                    String signOutMsg = curCPInfo.get("GPGSSignOutMsg");
                    String unknownErrMsg = curCPInfo.get("GPGSUnknownErrMsg");
                    setLocalizedMessages(signInMsg, signOutMsg, unknownErrMsg);
                } catch (Exception e) {
                    LogE("Developer info is wrong!", e);
                }
            }
        });
    }

    @Override
    public void submitScore(String leaderboardID, long score) {
        final String curID = leaderboardID;
        final long curScore = score;
        PluginWrapper.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if(isSignedIn()) {
                    getGamesClient().submitScore(curID, curScore);
                } else {
                    LogD("NL: You must sign in to submit scores");
                }
                LogD("implement submitScore");
            }
        });
    }

    @Override
    public void showLeaderboard(String leaderboardID) {
        final String curID = leaderboardID;
        PluginWrapper.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (isSignedIn()) {
                    LogD("implement showLeaderboard");
                    mActivity.startActivityForResult(getGamesClient().getAllLeaderboardsIntent(), RC_UNUSED_GPGS);
                } else {
                    //TODO: fix this, a thread => main thread => ui thread
                    beginUserInitiatedSignIn();
                }
            }
        });
    }

    @Override
    public void unlockAchievement(Hashtable<String, String> achInfo) {
        final Hashtable<String, String> curInfo = achInfo;
        PluginWrapper.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String achID = curInfo.get("AchievementID");
                    String strPortion = curInfo.get("Portion");
                    int portion = Integer.parseInt(strPortion);
                    if (achID == null || TextUtils.isEmpty(achID))
                    {
                        SocialWrapper.onSocialResult(mGPGS, SocialWrapper.SOCIAL_UNLOCKACH_FAILED, "Achievement info error");
                        return;
                    }
                    if(isSignedIn()) {
                        if(portion == 0) {
                          getGamesClient().unlockAchievement(achID);
                        } else {
                          getGamesClient().incrementAchievement(achID, portion);
                        }
                    } else {
                        LogD("NL: You must sign in to report achievements");
                    }
                } catch (Exception e) {
                    LogE("Unknown Error!", e);
                }
            }
        });
    }

    @Override
    public void showAchievements() {
        PluginWrapper.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (isSignedIn()) {
                    mActivity.startActivityForResult(getGamesClient().getAchievementsIntent(), RC_UNUSED_GPGS);
                } else {
                    beginUserInitiatedSignIn();
                    //TODO: When it succeeds we should show the achievements
                }
            }
        });
    }

    @Override
    public void setDebugMode(boolean debug) {
        bDebug = debug;
    }

//COPIED CODE

    // ===========================================================
    // Constants
    // ===========================================================
    // Request code we use when invoking other Activities to complete the
    // sign-in flow.
    public final static int RC_RESOLVE_GPGS = 9001;
    // Request code when invoking Activities whose result we don't care about.
    public final static int RC_UNUSED_GPGS = 9002;
    // What clients we manage (OR-able values, can be combined as flags)
    public final static int CLIENT_NONE = 0x00;
    public final static int CLIENT_GAMES = 0x01;
    public final static int CLIENT_PLUS = 0x02;
    public final static int CLIENT_APPSTATE = 0x04;
    public final static int CLIENT_ALL = CLIENT_GAMES | CLIENT_PLUS | CLIENT_APPSTATE;
    // ===========================================================
    // Fields
    // ===========================================================
    //private static Context sContext = null;
    //
    // The Activity we are bound to. We need to keep a reference to the Activity
    // because some games methods require an Activity (a Context won't do). We
    // are careful not to leak these references: we release them on onStop().
    //
    //private Activity mActivity = null;
    // OAuth scopes required for the clients. Initialized in setup().
    private String mScopes[];
    // Client objects we manage. If a given client is not enabled, it is null.
    private GamesClient mGamesClient = null;
    private PlusClient mPlusClient = null;
    private AppStateClient mAppStateClient = null;
    // What clients were requested? (bit flags)
    private int mRequestedClients = CLIENT_NONE;
    // What clients are currently connected? (bit flags)
    private int mConnectedClients = CLIENT_NONE;
    // What client are we currently connecting?
    private int mClientCurrentlyConnecting = CLIENT_NONE;
    // A progress dialog we show when we are trying to sign the user is
    private ProgressDialog mProgressDialog = null;
    // Whether to automatically try to sign in on onStart().
    private boolean mAutoSignIn = true;

    /*
     * Whether user has specifically requested that the sign-in process begin.
     * If mUserInitiatedSignIn is false, we're in the automatic sign-in attempt
     * that we try once the Activity is started -- if true, then the user has
     * already clicked a "Sign-In" button or something similar
     */
    boolean mUserInitiatedSignIn = false;

    // The connection result we got from our last attempt to sign-in.
    ConnectionResult mConnectionResult = null;

    // Whether our sign-in attempt resulted in an error. In this case,
    // mConnectionResult
    // indicates what was the error we failed to resolve.
    boolean mSignInError = false;

    // Whether we launched the sign-in dialog flow and therefore are expecting
    // an
    // onActivityResult with the result of that.
    boolean mExpectingActivityResult = false;

    // Are we signed in?
    boolean mSignedIn = false;

    // Print debug logs?
    boolean mDebugLog = true;//false;

    // Messages (can be set by the developer).
    String mSigningInMessage = "";
    String mSigningOutMessage = "";
    String mUnknownErrorMessage = "";

    // If we got an invitation id when we connected to the games client, it's
    // here.
    // Otherwise, it's null.
    String mInvitationId;

    public void setLocalizedMessages(String signingIn, String signingOut, String unknownError) {
        debugLog("setLocalizedMessages");
        mSigningInMessage = signingIn;
        mSigningOutMessage = signingOut;
        mUnknownErrorMessage = unknownError;
    }

//
// Static Jni Entrypoints
//
    /** static jni entry point that returns whether or not the user is signed in. */
    //public static boolean isSignedInJni() {
    //    getInstance().debugLog("isSignedInJni");
    //    return getInstance().isSignedIn();
    //}

    //public static void beginUserInitiatedSignInJni() {
    //  final GPGSWrapper instance = getInstance();
    //  if(instance != null) {
    //    instance.debugLog("beginUserInitiatedSignInJni");
    //    instance.mActivity.runOnUiThread(new Runnable() {
    //      public void run() {
    //        if(!instance.isSignedIn()) {
    //          instance.beginUserInitiatedSignIn();
    //        } else {
    //          instance.debugLog("responseCode != RESULT_OK, so not reconnecting.");
    //        }
    //      }
    //    });
    //  }
    //  return;
    //}

    //public static void showAchievementJni()
    //{
    //  getInstance().debugLog("showAchievementJni");
    //  getInstance().mActivity.runOnUiThread(new Runnable() {
    //      public void run() {
    //        if(getInstance().isSignedIn()) {
    //          getInstance().mActivity.startActivityForResult(getInstance().getGamesClient().getAchievementsIntent(), /*RC_UNUSED_GPGS*/9002);
    //        } else {
    //          getInstance().debugLog("You must sign in to use achievements");
    //        }
    //      }
    //  });
    //  return;
    //}

    //public static void showLeaderboardJni()
    //{
    //  getInstance().debugLog("showLeaderboardJni");
    //  getInstance().mActivity.runOnUiThread(new Runnable() {
    //      public void run() {
    //        if(getInstance().isSignedIn()) {
    //          getInstance().mActivity.startActivityForResult(getInstance().getGamesClient().getAllLeaderboardsIntent(), /*RC_UNUSED_GPGS*/9002);
    //        } else {
    //          getInstance().debugLog("You must sign in to use leaderboards");
    //        }
    //      }
    //  });
    //  return;
    //}

    //public static void reportAchievementJni(final String category, final int portion) {
    //  getInstance().debugLog("reportAchievementJni:("+category+", "+portion+")");
    //  getInstance().mActivity.runOnUiThread(new Runnable() {
    //      public void run() {
    //        if(getInstance().isSignedIn()) {
    //          if(portion == 0) {
    //            getInstance().getGamesClient().unlockAchievement(category);
    //          } else {
    //            getInstance().getGamesClient().incrementAchievement(category, portion);
    //          }
    //        } else {
    //          getInstance().debugLog("NL: You must sign in to report achievements");
    //        }
    //      }
    //  });
    //}

    //public static void submitScoreJni(final String category, final String stringScore)
    //{
    //  getInstance().debugLog("submitScoreJni:"+stringScore+" toCategory:"+category);
    //  getInstance().mActivity.runOnUiThread(new Runnable() {
    //      public void run() {
    //        if(getInstance().isSignedIn()) {
    //          long score = Long.parseLong(stringScore);
    //          getInstance().getGamesClient().submitScore(category, score);
    //        } else {
    //          getInstance().debugLog("NL: You must sign in to submit scores");
    //        }
    //      }
    //  });
    //  return;
    //}

    /**
     * Sets the message that appears onscreen when there is an unknown error
     * (rare!)
     */
    public void setUnknownErrorMessage(String message) {
        mUnknownErrorMessage = message;
    }

    /**
     * Same as calling @link{setup(int)} requesting only the
     * CLIENT_GAMES client.
     */
    public void setup() {
        setup(CLIENT_GAMES);
    }

    /**
     * Performs setup on this GPGSWrapper object. Call this from the onCreate()
     * method of your Activity. This will create the clients and do a few other
     * initialization tasks. Next, call @link{#onStart} from the onStart()
     * method of your Activity.
     *
     * @param listener The listener to be notified of sign-in events.
     * @param clientsToUse The clients to use. Use a combination of
     *            CLIENT_GAMES, CLIENT_PLUS and CLIENT_APPSTATE, or CLIENT_ALL
     *            to request all clients.
     */
    public void setup(int clientsToUse) {
        mRequestedClients = clientsToUse;

        Vector<String> scopesVector = new Vector<String>();
        if (0 != (clientsToUse & CLIENT_GAMES)) {
            scopesVector.add(Scopes.GAMES);
        }
        if (0 != (clientsToUse & CLIENT_PLUS)) {
            scopesVector.add(Scopes.PLUS_LOGIN);
        }
        if (0 != (clientsToUse & CLIENT_APPSTATE)) {
            scopesVector.add(Scopes.APP_STATE);
        }

        mScopes = new String[scopesVector.size()];
        scopesVector.copyInto(mScopes);

        if (0 != (clientsToUse & CLIENT_GAMES)) {
            if(getContext() == null) {
                LogD("getContext = null");
            }
            mGamesClient = new GamesClient.Builder(getContext(), this, this)
            .setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
            .setScopes(mScopes)
            .create();
        }

        if (0 != (clientsToUse & CLIENT_PLUS)) {
            debugLog("onCreate: creating GamesPlusClient");
            mPlusClient = new PlusClient.Builder(getContext(), this, this)
                    .setScopes(mScopes)
                    .build();
        }

        if (0 != (clientsToUse & CLIENT_APPSTATE)) {
            debugLog("onCreate: creating AppStateClient");
            mAppStateClient = new AppStateClient.Builder(getContext(), this, this)
                    .setScopes(mScopes)
                    .create();
        }
    }

    /**
     * Returns the GamesClient object. In order to call this method, you must have
     * called @link{setup} with a set of clients that includes CLIENT_GAMES.
     */
    public GamesClient getGamesClient() {
        if (mGamesClient == null) {
            throw new IllegalStateException("No GamesClient. Did you request it at setup?");
        }
        return mGamesClient;
    }

    /**
     * Returns the AppStateClient object. In order to call this method, you must have
     * called @link{#setup} with a set of clients that includes CLIENT_APPSTATE.
     */
    public AppStateClient getAppStateClient() {
        if (mAppStateClient == null) {
            throw new IllegalStateException("No AppStateClient. Did you request it at setup?");
        }
        return mAppStateClient;
    }

    /**
     * Returns the PlusClient object. In order to call this method, you must have
     * called @link{#setup} with a set of clients that includes CLIENT_PLUS.
     */
    public PlusClient getPlusClient() {
        if (mPlusClient == null) {
            throw new IllegalStateException("No PlusClient. Did you request it at setup?");
        }
        return mPlusClient;
    }

    /** Returns whether or not the user is signed in. */
    public boolean isSignedIn() {
        return mSignedIn;
    }

    /**
     * Returns whether or not there was a (non-recoverable) error during the
     * sign-in process.
     */
    public boolean hasSignInError() {
        return mSignInError;
    }

    /**
     * Returns the error that happened during the sign-in process, null if no
     * error occurred.
     */
    public ConnectionResult getSignInError() {
        return mSignInError ? mConnectionResult : null;
    }

    /** Call this method from your Activity's onStart(). */
    public void onStart(Activity act) {
        mActivity = act;

        debugLog("onStart.");
        if (mExpectingActivityResult) {
            // this Activity is starting because the UI flow we launched to
            // resolve a connection problem has just returned. In this case,
            // we should NOT automatically reconnect the client, since
            // onActivityResult will handle that.
            debugLog("onStart: won't connect because we're expecting activity result.");
        } else if (!mAutoSignIn) {
            // The user specifically signed out, so don't attempt to sign in
            // automatically. If the user wants to sign in, they will click
            // the sign-in button, at which point we will try to sign in.
            debugLog("onStart: not signing in because user specifically signed out.");
        } else {
            // Attempt to connect the clients.
            debugLog("onStart: connecting clients.");
            startConnections();
        }
    }

    /** Call this method from your Activity's onStop(). */
    public void onStop() {
        debugLog("onStop: disconnecting clients.");

        // disconnect the clients -- this is very important (prevents resource
        // leaks!)
        killConnections(CLIENT_ALL);

        // no longer signed in
        mSignedIn = false;
        mSignInError = false;

        // destroy progress dialog -- we create it again when needed
        dismissDialog();
        mProgressDialog = null;

        // let go of the Activity reference
        mActivity = null;
    }

    /** Convenience method to show a toast. */
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    /** Convenience method to show an alert dialog. */
    public void showAlert(String title, String message) {
        (new AlertDialog.Builder(getContext())).setTitle(title).setMessage(message)
                .setNeutralButton(android.R.string.ok, null).create().show();
    }

    /** Convenience method to show an alert dialog. */
    public void showAlert(String message) {
        (new AlertDialog.Builder(getContext())).setMessage(message)
                .setNeutralButton(android.R.string.ok, null).create().show();
    }

    /**
     * Returns the invitation ID received through an invitation notification.
     * This should be called from onSignInSucceeded method, to check if there's an
     * invitation available. In that case, accept the invitation.
     * @return The id of the invitation, or null if none was received.
     */
    public String getInvitationId() {
        return mInvitationId;
    }

    /** Enables debug logging, with the given logcat tag. */
    public void enableDebugLog(boolean enabled) {
        mDebugLog = enabled;
    }

    /**
     * Returns the current requested scopes. This is not valid until setup() has
     * been called.
     *
     * @return the requested scopes, including the oauth2: prefix
     */
    public String getScopes() {
        StringBuilder scopeStringBuilder = new StringBuilder();
        int clientsToUse = mRequestedClients;
        // GAMES implies PLUS_LOGIN
        if (0 != (clientsToUse & CLIENT_GAMES)) {
            addToScope(scopeStringBuilder, Scopes.GAMES);
        }
        if (0 != (clientsToUse & CLIENT_PLUS)) {
            addToScope(scopeStringBuilder, Scopes.PLUS_LOGIN);
        }
        if (0 != (clientsToUse & CLIENT_APPSTATE)) {
            addToScope(scopeStringBuilder, Scopes.APP_STATE);
        }
        return scopeStringBuilder.toString();
    }

    /** Sign out and disconnect from the APIs. */
    public void signOut() {
        mConnectionResult = null;
        mAutoSignIn = false;
        mSignedIn = false;
        mSignInError = false;

        if (mPlusClient != null && mPlusClient.isConnected()) {
            mPlusClient.clearDefaultAccount();
        }
        if (mGamesClient != null && mGamesClient.isConnected()) {
            showProgressDialog(false);
            mGamesClient.signOut(this);
        }

        // kill connects to all clients but games, which must remain
        // connected til we get onSignOutComplete()
        killConnections(CLIENT_ALL & ~CLIENT_GAMES);
    }

    /**
     * Handle activity result. Call this method from your Activity's
     * onActivityResult callback. If the activity result pertains to the sign-in
     * process, processes it appropriately.
     */
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        LogD("onActivityResult("+requestCode+", "+responseCode+", data)");
        if (requestCode == RC_RESOLVE_GPGS) {
            // We're coming back from an activity that was launched to resolve a
            // connection
            // problem. For example, the sign-in UI.
            mExpectingActivityResult = false;
            debugLog("onActivityResult, req " + requestCode + " response " + responseCode);
            if (responseCode == Activity.RESULT_OK) {
                // Ready to try to connect again.
                debugLog("responseCode == RESULT_OK. So connecting.");
                connectCurrentClient();
            } else {
                // Whatever the problem we were trying to solve, it was not
                // solved.
                // So give up and show an error message.
                debugLog("responseCode != RESULT_OK, so not reconnecting.");
                giveUp();
            }
        }
    }

    /**
     * Starts a user-initiated sign-in flow. This should be called when the user
     * clicks on a "Sign In" button. As a result, authentication/consent dialogs
     * may show up. At the end of the process,
     * onSignInSucceeded() or onSignInFailed() methods will be called.
     */
    public void beginUserInitiatedSignIn() {
        if (mSignedIn)
            return; // nothing to do

        // reset the flag to sign in automatically on onStart() -- now a
        // wanted behavior
        mAutoSignIn = true;

        // Is Google Play services available?
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        debugLog("isGooglePlayServicesAvailable returned " + result);
        if (result != ConnectionResult.SUCCESS) {
            // Nope.
            debugLog("Google Play services not available. Show error dialog.");
            Dialog errorDialog = getErrorDialog(result);
            errorDialog.show();
            //onSignInFailed();
            SocialWrapper.onSocialResult(this, SocialWrapper.SOCIAL_SIGNIN_FAILED, "connection result != SUCCESS");
            return;
        }

        mUserInitiatedSignIn = true;
        if (mConnectionResult != null) {
            // We have a pending connection result from a previous failure, so
            // start with that.
            debugLog("beginUserInitiatedSignIn: continuing pending sign-in flow.");
            showProgressDialog(true);
            resolveConnectionResult();
        } else {
            // We don't have a pending connection result, so start anew.
            debugLog("beginUserInitiatedSignIn: starting new sign-in flow.");
            startConnections();
        }
    }

    Context getContext() {
        return mActivity;
    }

    void addToScope(StringBuilder scopeStringBuilder, String scope) {
        if (scopeStringBuilder.length() == 0) {
            scopeStringBuilder.append("oauth2:");
        } else {
            scopeStringBuilder.append(" ");
        }
        scopeStringBuilder.append(scope);
    }

    void startConnections() {
        mConnectedClients = CLIENT_NONE;
        mInvitationId = null;
        connectNextClient();
    }

    void showProgressDialog(boolean signIn) {
        String message = signIn ? mSigningInMessage : mSigningOutMessage;

        if (mProgressDialog == null) {
            if (getContext() == null)
                return;
            mProgressDialog = new ProgressDialog(getContext());
        }

        mProgressDialog.setMessage(message == null ? "" : message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    void dismissDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        mProgressDialog = null;
    }

    void connectNextClient() {
        // do we already have all the clients we need?
        int pendingClients = mRequestedClients & ~mConnectedClients;
        if (pendingClients == 0) {
            debugLog("All clients now connected. Sign-in successful.");
            succeedSignIn();
            return;
        }

        showProgressDialog(true);

        // which client should be the next one to connect?
        if (mGamesClient != null && (0 != (pendingClients & CLIENT_GAMES))) {
            debugLog("Connecting GamesClient.");
            mClientCurrentlyConnecting = CLIENT_GAMES;
        } else if (mPlusClient != null && (0 != (pendingClients & CLIENT_PLUS))) {
            debugLog("Connecting PlusClient.");
            mClientCurrentlyConnecting = CLIENT_PLUS;
        } else if (mAppStateClient != null && (0 != (pendingClients & CLIENT_APPSTATE))) {
            debugLog("Connecting AppStateClient.");
            mClientCurrentlyConnecting = CLIENT_APPSTATE;
        } else {
            throw new AssertionError("Not all clients connected, yet no one is next. R="
                    + mRequestedClients + ", C=" + mConnectedClients);
        }

        connectCurrentClient();
    }

    void connectCurrentClient() {
        switch (mClientCurrentlyConnecting) {
            case CLIENT_GAMES:
                mGamesClient.connect();
                break;
            case CLIENT_APPSTATE:
                mAppStateClient.connect();
                break;
            case CLIENT_PLUS:
                mPlusClient.connect();
                break;
        }
    }

    void killConnections(int whatClients) {
        if ((whatClients & CLIENT_GAMES) != 0 && mGamesClient != null
                && mGamesClient.isConnected()) {
            mConnectedClients &= ~CLIENT_GAMES;
            mGamesClient.disconnect();
        }
        if ((whatClients & CLIENT_PLUS) != 0 && mPlusClient != null
                && mPlusClient.isConnected()) {
            mConnectedClients &= ~CLIENT_PLUS;
            mPlusClient.disconnect();
        }
        if ((whatClients & CLIENT_APPSTATE) != 0 && mAppStateClient != null
                && mAppStateClient.isConnected()) {
            mConnectedClients &= ~CLIENT_APPSTATE;
            mAppStateClient.disconnect();
        }
    }

    public void reconnectClients(int whatClients) {
        showProgressDialog(true);

        if ((whatClients & CLIENT_GAMES) != 0 && mGamesClient != null
                && mGamesClient.isConnected()) {
            mConnectedClients &= ~CLIENT_GAMES;
            mGamesClient.reconnect();
        }
        if ((whatClients & CLIENT_APPSTATE) != 0 && mAppStateClient != null
                && mAppStateClient.isConnected()) {
            mConnectedClients &= ~CLIENT_APPSTATE;
            mAppStateClient.reconnect();
        }
        if ((whatClients & CLIENT_PLUS) != 0 && mPlusClient != null
                && mPlusClient.isConnected()) {
            mConnectedClients &= ~CLIENT_PLUS;
            mPlusClient.disconnect();
            mPlusClient.connect();
        }
    }

    /** Called when we successfully obtain a connection to a client. */
    @Override
    public void onConnected(Bundle connectionHint) {
        debugLog("onConnected: connected! client=" + mClientCurrentlyConnecting);

        // Mark the current client as connected
        mConnectedClients |= mClientCurrentlyConnecting;

        // If this was the games client and it came with an invite, store it for
        // later retrieval.
        if (mClientCurrentlyConnecting == CLIENT_GAMES && connectionHint != null) {
            debugLog("onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint.getParcelable(GamesClient.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // accept invitation
                debugLog("onConnected: connection hint has a room invite!");
                mInvitationId = inv.getInvitationId();
                debugLog("Invitation ID: " + mInvitationId);
            }
        }

        // connect the next client in line, if any.
        connectNextClient();
    }

    void succeedSignIn() {
        debugLog("All requested clients connected. Sign-in succeeded!");
        mSignedIn = true;
        mSignInError = false;
        mAutoSignIn = true;
        mUserInitiatedSignIn = false;
        dismissDialog();
        //onSignInSucceeded();
        SocialWrapper.onSocialResult(this, SocialWrapper.SOCIAL_SIGNIN_SUCCESS, "");
    }

    /** Handles a connection failure reported by a client. */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // save connection result for later reference
        mConnectionResult = result;
        debugLog("onConnectionFailed: result " + result.getErrorCode());
        dismissDialog();
        //NFHACK GIVING UP RIGHT AWAY
        //giveUp();

        if (!mUserInitiatedSignIn) {
            // If the user didn't initiate the sign-in, we don't try to resolve
            // the connection problem automatically -- instead, we fail and wait
            // for the user to want to sign in. That way, they won't get an
            // authentication (or other) popup unless they are actively trying
            // to
            // sign in.
            debugLog("onConnectionFailed: since user didn't initiate sign-in, failing now.");
            mConnectionResult = result;
            //onSignInFailed();
            //SocialWrapper.onSocialResult(this, SocialWrapper.SOCIAL_SIGNIN_FAILED, "User did not initiate sign in failing");
            return;
        }

        debugLog("onConnectionFailed: since user initiated sign-in, trying to resolve problem.");

        // Resolve the connection result. This usually means showing a dialog or
        // starting an Activity that will allow the user to give the appropriate
        // consents so that sign-in can be successful.
        resolveConnectionResult();
    }

    /**
     * Attempts to resolve a connection failure. This will usually involve
     * starting a UI flow that lets the user give the appropriate consents
     * necessary for sign-in to work.
     */
    void resolveConnectionResult() {
        // Try to resolve the problem
        debugLog("resolveConnectionResult: trying to resolve result: " + mConnectionResult);
        if (mConnectionResult.hasResolution()) {
            // This problem can be fixed. So let's try to fix it.
            debugLog("result has resolution. Starting it.");
            try {
                // launch appropriate UI flow (which might, for example, be the
                // sign-in flow)
                mExpectingActivityResult = true;
                mConnectionResult.startResolutionForResult(mActivity, RC_RESOLVE_GPGS);
            } catch (SendIntentException e) {
                // Try connecting again
                debugLog("SendIntentException.");
                connectCurrentClient();
            }
        } else {
            // It's not a problem what we can solve, so give up and show an
            // error.
            debugLog("resolveConnectionResult: result has no resolution. Giving up.");
            giveUp();
        }
    }

    /**
     * Give up on signing in due to an error. Shows the appropriate error
     * message to the user, using a standard error dialog as appropriate to the
     * cause of the error. That dialog will indicate to the user how the problem
     * can be solved (for example, re-enable Google Play Services, upgrade to a
     * new version, etc).
     */
    void giveUp() {
        debugLog("giveUp and display dialog");
        mSignInError = true;
        mAutoSignIn = false;
        dismissDialog();
        debugLog("giveUp: giving up on connection. " +
                ((mConnectionResult == null) ? "(no connection result)" :
                        ("Status code: " + mConnectionResult.getErrorCode())));

        Dialog errorDialog = null;
        if (mConnectionResult != null) {
            // get error dialog for that specific problem
            errorDialog = getErrorDialog(mConnectionResult.getErrorCode());
            errorDialog.show();
            //onSignInFailed();
            SocialWrapper.onSocialResult(this, SocialWrapper.SOCIAL_SIGNIN_FAILED, "no connection result");
        } else {
            // this is a bug
            Log.e("GPGSWrapper", "giveUp() called with no mConnectionResult");
        }
    }

    /** Called when we are disconnected from a client. */
    @Override
    public void onDisconnected() {
        debugLog("onDisconnected.");
        mConnectionResult = null;
        mAutoSignIn = false;
        mSignedIn = false;
        mSignInError = false;
        mInvitationId = null;
        mConnectedClients = CLIENT_NONE;
        //onSignInFailed();
        SocialWrapper.onSocialResult(this, SocialWrapper.SOCIAL_SIGNIN_FAILED, "disconnected");
    }

    /** Returns an error dialog that's appropriate for the given error code. */
    Dialog getErrorDialog(int errorCode) {
        debugLog("Making error dialog for error: " + errorCode);
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, mActivity,
                RC_UNUSED_GPGS, null);

        if (errorDialog != null)
            return errorDialog;

        // as a last-resort, make a sad "unknown error" dialog.
        return (new AlertDialog.Builder(getContext())).setMessage(mUnknownErrorMessage)
                .setNeutralButton(android.R.string.ok, null).create();
    }

    void debugLog(String message) {
        if (mDebugLog)
            Log.e(TAG, message);
    }

    @Override
    public void onSignOutComplete() {
        dismissDialog();
        if (mGamesClient.isConnected())
            mGamesClient.disconnect();
    }

    @Override
    public String getPluginVersion() {
        return "0.0.1";
    }

    @Override
    public String getSDKVersion() {
        return "20130821";
    }


}
