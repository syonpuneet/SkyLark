package in.vipplaza.utills;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.internal.SessionTracker;
import com.facebook.internal.Utility;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

import org.json.JSONObject;

import java.util.Arrays;

public class FacebookUserDetails {

	public static SessionTracker mSessionTracker;

	private Context context;


	public FacebookUserDetails(Context context) {
		this.context = context;
	}

	public void signInWithFacebook() {
		mSessionTracker = new SessionTracker(context, new StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {}
		}, null, false);

		String applicationId = Utility.getMetadataApplicationId(context);
		Constant.mCurrentSession = mSessionTracker.getSession();

		if (Constant.mCurrentSession == null || Constant.mCurrentSession.getState().isClosed()) {
			mSessionTracker.setSession(null);
			Session session = new Session.Builder(context).setApplicationId(applicationId).build();
			Session.setActiveSession(session);
			Constant.mCurrentSession = session;
		} else {
			mSessionTracker.setSession(null);
			Session session = new Session.Builder(context).setApplicationId(applicationId).build();
			Session.setActiveSession(session);
			Constant.mCurrentSession = session;
		}

		if (!Constant.mCurrentSession.isOpened()) {
			Session.OpenRequest openRequest = null;
			openRequest = new Session.OpenRequest((Activity) context);

			if (openRequest != null) {
				openRequest.setDefaultAudience(SessionDefaultAudience.FRIENDS);
				openRequest.setPermissions(Arrays.asList("user_birthday", "email", "user_location","user_friends"));
				openRequest.setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);

				Constant.mCurrentSession.openForRead(openRequest);
				accessFacebookUserInfo();
			}
		} else {
			accessFacebookUserInfo();
		}
	}

	public void accessFacebookUserInfo() {
		if (Session.getActiveSession() != null || Session.getActiveSession().isOpened()) {
			Bundle bundle = new Bundle();
			bundle.putString("fields", "picture");
			final Request request = new Request(Session.getActiveSession(), "me", bundle, HttpMethod.GET, new Request.Callback() {
				@SuppressWarnings("unused")
				@Override
				public void onCompleted(Response response) {
					GraphObject graphObject = response.getGraphObject();
					if (graphObject != null) {
						try {
							JSONObject jsonObject = new JSONObject();
							jsonObject = graphObject.getInnerJSONObject();

							Log.i("","faacebook response==="+jsonObject.toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
			Request.executeBatchAsync(request);

//			Request r = new Request(Session.getActiveSession(), "/me/friends",
//					null, HttpMethod.GET, new Request.Callback() {
//						@Override
//						public void onCompleted(Response response) {
//							try {
//								GraphObject graphObj = response.getGraphObject();
//								if (graphObj != null) {
//									JSONObject jsonObj = graphObj.getInnerJSONObject();
//									FacebookFriendsTemp friendsTemp = (FacebookFriendsTemp) Util.getJsonToClassObject(jsonObj.toString(), FacebookFriendsTemp.class);
//									Constants.fbFriendList = friendsTemp.data;
//									if (!Constants.isFBFromLogin) {
//										PostCameraActivity activity = new PostCameraActivity();
//										activity.settxtFBFriendText(context);
//									}
//								}
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					});
//			r.executeAsync();

			Request meRequest = Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					try {
						Constant.userDetails = user;
						Log.i("","user======"+user);

						if(user!=null)
						{

							//LoginRegister.getFacebookData(context, user);
						}

					} catch (Exception jex) {
						jex.printStackTrace();
					}
				}
			});

			RequestBatch requestBatch = new RequestBatch(meRequest);
			requestBatch.addCallback(new RequestBatch.Callback() {
				@Override
				public void onBatchCompleted(RequestBatch batch) {
					Log.d("asdf", "asdf");
				}
			});
			requestBatch.executeAsync();
		}
	}
}
