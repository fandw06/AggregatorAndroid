package inertia.http;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;

/**
 * An AsyncTask that make a Http request.
 *
 * @author David
 * @see android.os.AsyncTask
 *
 */
public class HttpRequestTask extends AsyncTask<HttpPackage, Integer, JSONObject> {

    private static String TAG = "HttpRequestTask";

    @Override
    protected JSONObject doInBackground(HttpPackage... params) {

        byte command = params[0].command;
        JSONObject res = null;

        switch(command){
            case HttpPackage.HTTP_POST:
                Log.d(TAG, "HTTP_POST");
                try {
                    URL url = new URL(params[0].url);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestMethod("POST");

                    OutputStream out = conn.getOutputStream();
                    String content = params[0].data.toString();
                    out.write(content.getBytes("UTF-8"));
                    out.flush();
                    int responseCode = conn.getResponseCode();// Don't need to call conn.connect()
                    if (responseCode ==  HttpURLConnection.HTTP_OK) {
                        InputStream is = conn.getInputStream();
                        String state = is.toString();
                        Log.d(TAG, "Content: "+state);
                    } else {
                        Log.i(TAG, "Connection failed! " + responseCode);
                    }
                    out.close();
                    conn.disconnect();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case HttpPackage.HTTP_GET:
                Log.d(TAG, "HTTP_GET");
                try {
                    URL url = new URL(params[0].url);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    int response = conn.getResponseCode();
                    Log.d(TAG, "The response is: " + response);
                    InputStream is = conn.getInputStream();

                    // Convert the InputStream into a string
                    Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                    String sRes = s.hasNext() ? s.next() : "";
                    Log.d(TAG, sRes);
                    try {
                        res = new JSONObject(sRes);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    is.close();
                    s.close();
                    conn.disconnect();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

        return res;
    }
    @Override
    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected void onPostExecute(JSONObject result) {

    }

}

