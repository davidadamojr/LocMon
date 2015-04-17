package edu.unt.sell.contextmon;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;

import edu.unt.sell.contextmon.db.BroadcastDatabaseHelper;


public class SyncService extends IntentService {

    private static final String TAG = SyncService.class.getSimpleName();

    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        BroadcastDatabaseHelper database = new BroadcastDatabaseHelper(getApplicationContext());
        AsyncHttpClient client = new SyncHttpClient();
        RequestParams params = new RequestParams();
        String broadcastJSON = database.composeJSONfromSQLite();
        if (!broadcastJSON.equals("[]")) {
            params.put("broadcasts_json", database.composeJSONfromSQLite());
            client.post("http://10.0.3.2/contextmon_sync/endpoint.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.i(TAG, responseBody.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    if (statusCode == 404) {
                        Log.d(TAG, "Requested resource not found");
                    } else if (statusCode == 500) {
                        Log.d(TAG, "Something went wrong at the server end");
                    } else {
                        Log.d(TAG, "Unexpected error occurred! Device might not be connected to the Internet");
                    }
                }
            });
        } else {
            Log.i(TAG, "There is no data in the sqlite DB");
        }
    }

}
