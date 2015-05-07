package edu.unt.sell.locmon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import edu.unt.sell.locmon.contentprovider.LocationContentProvider;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Intent mService;

    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mService = new Intent(this, LocationMonitorService.class);

        // disable stop button
        // MenuItem stopItem = (MenuItem) findViewById(R.id.menuStop);
        // stopItem.setEnabled(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        TextView serviceStatus = (TextView) findViewById(R.id.status_text);
        // MenuItem stopMenuItem = (MenuItem) findViewById(R.id.menuStop);
        // MenuItem startMenuItem = (MenuItem) findViewById(R.id.menuStart);

        switch (id) {
            case R.id.menuDelete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // add buttons to the dialog
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked ok button
                        getContentResolver().delete(LocationContentProvider.CONTENT_URI, null, null);
                        Log.d(TAG, "All data deleted... service will now continue");
                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setTitle("Delete all data?");

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.menuStart:
                Log.d(TAG, "Starting service.");
                serviceStatus.setText("Service is running.");
                // startMenuItem.setEnabled(false);
                // stopMenuItem.setEnabled(true);

                // start location requests
                startService(mService);
                return true;
            case R.id.menuStop:
                Log.d(TAG, "Stopping service.");
                serviceStatus.setText("Service is stopped.");
                // startMenuItem.setEnabled(true);
                // stopMenuItem.setEnabled(false);

                // stop location requests
                stopService(mService);
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
