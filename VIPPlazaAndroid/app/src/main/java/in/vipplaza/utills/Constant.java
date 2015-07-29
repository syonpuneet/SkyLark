package in.vipplaza.utills;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.model.GraphUser;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import in.vipplaza.R;


public class Constant {

	public static String TAG_SUCESS = "success";

	// public static final String	 MAIN_URL="http://192.168.0.10/hdb/api/";

	//public static final String MAIN_URL = "http://192.168.0.10/vipplaza/restapi/index/";
	public static final String MAIN_URL = "http://syonserver.com/vipplaza/restapi/index/";
	public static final String FAQ_URL = "http://syonserver.com/vipplaza/faq";
	public static final String PRIVACY_URL = "http://syonserver.com/vipplaza/privacy";
	public static final String ABOUT_URL = "http://syonserver.com/vipplaza/about";



	public static int tabs_indicator_color = 0xFFffffff;
	
	public static int map_line_color= 0xFF2E9CC0;

	public static String  type="success";

	public static String user_type = "user_type";
	public static String user_email = "email";
	
	public static String user_id = "id";
	public static String entity_id = "entity_id";
	public static String signupid_encrypted = "signupid_encrypted";
	public static String user_name = "username";
	public static String user_image = "user_image";
	public static String full_image = "full_image";
	public static String user_passord = "user_password";
	public static String isLogin = "isLogin";
	public static String isLoginRemember = "isLoginRemember";
	public static String firstName = "first_name";
	public static String lastName = "first_name";
	public static String gender = "gender";
	public static String profile_status = "profile_status";

	public static String city = "city";
	public static String interest_to_meet = "interest_to_meet";
	public static String interest_to = "interest_to";

	public static String action_latest_member = "showAllLatestMember";
	public static String action_hot_list = "showAllHotMember";
	public static String action_my_match = "showAllMatch";
	public static String action_userLike = "userLike";
	public static String action_addFavorite = "addFavorite";
	public static String is_varified = "is_varified";
	public static String varified_by = "varified_by";
	public static String age = "age";
	public static String creadit_count = "creadit_count";
	public static String isFirstInstall = "isFirstInstall";
	public static String language = "language";
	public static String english = "en";
	public static String burmish = "my-rMM";


	//constants for Facebook
	public static Session mCurrentSession;
	public static GraphUser userDetails;


	public static String terms_url="http://syonserver.com/hdb/terms-and-condition";

	public static final String GOOGLE_PROJECT_ID = "1085333525074";
	public static final String MESSAGE_KEY = "message";
	public static final String REG_ID = "regId";
	public static final String APP_VERSION = "appVersion";

	public static final int PER_PAGE_RECORD=100;
	
//	1-assign driver
//	2-driver reached at customer location
//	3-customer handover the parsel
//	4-driver pick the handover
//	5-driver reached at location
//	6-driver handover the package
//	7-reciver take the package
//	8-complete
	
	
	public static Boolean write(String fname, String fcontent){
	      try {
	        String fpath = Environment.getExternalStorageDirectory()+fname+".txt";
	        File file = new File(fpath);
	        // If file does not exists, then create it
	        if (!file.exists()) {
	          file.createNewFile();
	        }
	        FileWriter fw = new FileWriter(file.getAbsoluteFile());
	        BufferedWriter bw = new BufferedWriter(fw);
	        bw.write(fcontent);
	        bw.close();
	        Log.d("Suceess","Sucess");
	        return true;
	      } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	      }
	   }

	public static String printKeyHash(Activity context) {
		PackageInfo packageInfo;
		String key = null;
		try {
			//getting application package name, as defined in manifest
		String packageName = context.getApplicationContext().getPackageName();
			//String packageName="com.syonserver.couponspring";
			//Retriving package info
			packageInfo = context.getPackageManager().getPackageInfo(packageName,
					PackageManager.GET_SIGNATURES);
				
			//Log.e("Package Name=", context.getApplicationContext().getPackageName());
			
			for (Signature signature : packageInfo.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				key = new String(Base64.encode(md.digest(), 0));
			
				// String key = new String(Base64.encodeBytes(md.digest()));
				Log.e("Key Hash=", key);
			}
		} catch (NameNotFoundException e1) {
			Log.e("Name not found", e1.toString());
		}
		catch (NoSuchAlgorithmException e) {
			Log.e("No such an algorithm", e.toString());
		} catch (Exception e) {
			Log.e("Exception", e.toString());
		}

		return key;
	}


	public static Document getDomElement(String xml) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);

		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		}

		return doc;
	}
	
	
	public static void makeCall(String phone_no, Activity mActivity) {
		Log.i("Make call", "");

		Intent phoneIntent = new Intent(Intent.ACTION_CALL);
		phoneIntent.setData(Uri.parse("tel:" + phone_no));

		try {
			mActivity.startActivity(phoneIntent);
			// finish();
			Log.i("Finished making a call...", "");
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(mActivity, "Call faild, please try again later.",
					Toast.LENGTH_SHORT).show();
		}
	}

	public static void getOverflowMenu(Context mContext) {

		try {
			ViewConfiguration config = ViewConfiguration.get(mContext);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendIntent(Context mContext, Class<?> cls) {
		Intent intent = new Intent(mContext, cls);
		mContext.startActivity(intent);
	}

	public static void showAlertDialog(Context mContext, String message,
			boolean isCancelable) {

		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle(mContext.getResources().getString(R.string.app_name));
		alert.setCancelable(isCancelable);
		alert.setMessage(message);
		alert.setMessage(Html.fromHtml(message));
		alert.setNeutralButton(R.string.ok, null);
		alert.show();

	}

	@SuppressWarnings("deprecation")
	public static String post(String path, Map<String, String> params)
			throws IOException {

		String response = "";
		final int MAX_RETRIES = 3;
		int numTries = 0;
		int responseCode = 0;
		HttpURLConnection urlConnection = null;
		// final long startTime = System.currentTimeMillis();

		while (numTries < MAX_RETRIES) {

			if (numTries != 0) {
				// LOGV(TAG, "Retry n�" + numTries);
			}

			// Create (POST) object on server
			try {

				StringBuilder bodyBuilder = new StringBuilder();
				Iterator<Entry<String, String>> iterator = params.entrySet()
						.iterator();
				// constructs the POST body using the parameters
				while (iterator.hasNext()) {
					Entry<String, String> param = iterator.next();
					bodyBuilder.append(param.getKey()).append('=')
							.append(param.getValue());
					if (iterator.hasNext()) {
						bodyBuilder.append('&');
					}
				}
				String body = bodyBuilder.toString();
				Log.i("Reording app", "Posting '" + body + "' to " + path);

				byte[] bytes = body.getBytes();
				URL url = new URL(path);
				/*
				 * urlConnection = (HttpURLConnection) url.openConnection();
				 * urlConnection.setDoOutput(true);
				 * urlConnection.setFixedLengthStreamingMode(bytes.length);
				 * urlConnection.setRequestProperty("Content-Type",
				 * "application/json;charset=utf-8");
				 */

				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setDoOutput(true);
				urlConnection.setUseCaches(false);
				urlConnection.setFixedLengthStreamingMode(bytes.length);
				urlConnection.setRequestMethod("POST");
				urlConnection.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded;charset=UTF-8");
				// LOGV(TAG, "HTTP POST " + url.toString());
				OutputStream out = urlConnection.getOutputStream();
				out.write(bytes);
				out.close();
				responseCode = urlConnection.getResponseCode();

				Log.i("", "response code====" + responseCode);
				/*
				 * LOGV(TAG, "HTTP POST response code: " + responseCode + " (" +
				 * (System.currentTimeMillis() - startTime) + "ms)");
				 */

				DataInputStream inputStream = new DataInputStream(
						urlConnection.getInputStream());

				// String response="";
				String serverResponseMessage = urlConnection
						.getResponseMessage();
				while ((serverResponseMessage = inputStream.readLine()) != null) {
					response = response + serverResponseMessage;
				}

				Log.i("", "response====" + response + "       " + responseCode);
				return response;

			} catch (UnsupportedEncodingException e) {
				// LOGV(TAG, "Unsupported encoding exception");
			} catch (MalformedURLException e) {
				// LOGV(TAG, "Malformed URL exception");
			} catch (IOException e) {
				// LOGV(TAG, "IO exception: " + e.toString());
				// e.printStackTrace();
			} finally {

				if (urlConnection != null)
					urlConnection.disconnect();
			}

			if (response.equals(""))
				numTries++;
		}

		// LOGV(TAG, "Max retries reached. Giving up...");

		return response;

	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@SuppressLint("NewApi")
	public static String getPath(Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

}
