package edu.unt.sell.locmon;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.unt.sell.locmon.contentprovider.LocationContentProvider;
import edu.unt.sell.locmon.db.LocationDatabaseHelper;
import edu.unt.sell.locmon.util.TextWriter;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Intent mService;

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

        switch (id) {
            case R.id.menuExport:
                // check if database contains any records
                // if database does not contain any records, inform the user that there is
                // nothing to export
                LocationDatabaseHelper database = new LocationDatabaseHelper(this);
                boolean databaseEmpty = database.checkDatabaseEmpty();
                if (databaseEmpty) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Log.d(TAG, "Nothing to export from database.");
                        }
                    });
                    builder.setTitle("Database Empty!");

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // create json file to be exported via email
                    // json file name should be locations + current timestamp

                    // make sure the device's external storage is actually available
                    if (isExternalStorageWritable()) {
                        String jsonData = database.composeJSONfromSQLite();
                        String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
                        String filename = "locations_" + timestamp + ".json";
                        TextWriter textWriter = new TextWriter(filename);
                        textWriter.writeText(jsonData);

                        // retrieve the newly written file so it can be sent as an email attachment
                        File dataFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

                        // handle the situation where, for some reason, the file cannot be found
                        if (!dataFile.exists()){
                            Toast noFileToast = Toast.makeText(MainActivity.this, "Could not find data file to export.", Toast.LENGTH_SHORT);
                            noFileToast.show();
                            return true;
                        }

                        Uri dataFileUri = Uri.fromFile(dataFile);

                        // easiest way to send this file as an attachment is to simply use an intent
                        // to open an email application with the file attached
                        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("application/octet-stream");
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sensor Data from " + deviceId);
                        emailIntent.putExtra(Intent.EXTRA_STREAM, dataFileUri);
                        try {
                            startActivity(Intent.createChooser(emailIntent, "Export to Email"));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Log.d(TAG, "External storage is not writable.");
                            }
                        });
                        builder.setTitle("Export Error!");

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }

                return true;
            case R.id.menuDelete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // add buttons to the dialog
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked ok button
                        getContentResolver().delete(LocationContentProvider.CONTENT_URI, null, null);
                        Toast deleteConfirm = Toast.makeText(MainActivity.this, "All data deleted.", Toast.LENGTH_SHORT);
                        deleteConfirm.show();
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

    private boolean isExternalStorageWritable() {
        /*
        * Checks if external storage is available for read and write
         */

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }

        return false;
    }
}
