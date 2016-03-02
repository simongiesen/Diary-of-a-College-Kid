package science.anthonyalves.diaryofacollegekid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import science.anthonyalves.diaryofacollegekid.Utils.AudioPlayer;
import science.anthonyalves.diaryofacollegekid.Utils.Constants;
import science.anthonyalves.diaryofacollegekid.db.Entry;
import science.anthonyalves.diaryofacollegekid.db.EntryDAO;
import science.anthonyalves.diaryofacollegekid.fragments.DatePickerFragment;
import science.anthonyalves.diaryofacollegekid.fragments.MicDialogFragment;
import science.anthonyalves.diaryofacollegekid.fragments.ScrollMapFragment;
import science.anthonyalves.diaryofacollegekid.fragments.TimePickerFragment;

public class EntryActivity extends AppCompatActivity implements ActionMenuView.OnMenuItemClickListener, View.OnClickListener, View.OnLongClickListener, OnMapReadyCallback {

    Toolbar mBottomToolbar;
    Menu mBottomMenu;
    Menu mMainMenu;

    ImageView mEntryImage;
    VideoView mEntryVideo;
    String mCurrentPhotoPath;
    String mCurrentVideoPath;
    String mCurrentAudioPath;
    String mCurrentGalleryPath;
    Location mLastLocation;

    CardView mDateHolder;
    LinearLayout mAudioHolder;

    EditText mTitle;
    EditText mBody;
    EditText mKeywords;

    Calendar mDate;

    ScrollMapFragment mMapFragment;
    GoogleMap mMap;

    EntryDAO mEntryDAO;

    AudioPlayer mAudioPlayer;

    boolean exit = false;

    boolean readOnly = false;
    boolean edited = false;

    Entry mEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mTitle = (EditText) findViewById(R.id.entry_title);
        mBody = (EditText) findViewById(R.id.entry_body);
        mKeywords = (EditText) findViewById(R.id.entry_keywords);

        mEntryVideo = (VideoView) findViewById(R.id.entry_video);
        mEntryVideo.setMediaController(new MediaController(this));

        mEntryImage = (ImageView) findViewById(R.id.entry_image);
        mEntryImage.setOnClickListener(this);

        mDateHolder = (CardView) findViewById(R.id.entry_date_holder);
        mDateHolder.setOnClickListener(this);

        mAudioHolder = (LinearLayout) findViewById(R.id.entry_audio_playback_holder);
        mAudioHolder.setVisibility(View.GONE);

        mBottomToolbar = (Toolbar) findViewById(R.id.bottom_toolbar);
        ActionMenuView actionMenuView = (ActionMenuView) findViewById(R.id.bot_tb_menu);
        actionMenuView.setOnMenuItemClickListener(this);
        mBottomMenu = actionMenuView.getMenu();


        mMapFragment = (ScrollMapFragment) getSupportFragmentManager().findFragmentById(R.id.entry_map);
        final ScrollView entryScrollView = (ScrollView) findViewById(R.id.entry_scrollview);
        mMapFragment.setListener(new ScrollMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                entryScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });
        mMapFragment.getView().setVisibility(View.GONE);
        mMapFragment.getMapAsync(EntryActivity.this);

        mEntryDAO = new EntryDAO(this);

        Intent intent = getIntent();
        if (intent != null) {
            Entry temp = (Entry) intent.getParcelableExtra(Constants.ENTRY_INTENT);
            if (temp != null) {
                setupReadOnly(temp);
            }
        }
    }

    private void setupReadOnly(Entry entry) {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        readOnly = true;
        mEntry = entry;

        mCurrentAudioPath = entry.getAudioPath();
        mCurrentPhotoPath = entry.getPhotoPath();
        mCurrentVideoPath = entry.getVideoPath();
        mCurrentGalleryPath = entry.getGalleryPath();
        mDate = entry.getDate();

        if (mDate == null) {
            mDate = Calendar.getInstance();
        }

        mLastLocation = entry.getLocation();

        TextView title = (TextView) findViewById(R.id.entry_title);
        TextView body = (TextView) findViewById(R.id.entry_body);
        TextView keywords = (TextView) findViewById(R.id.entry_keywords);
        title.setText(entry.getTitle());
        body.setText(entry.getBody());
        keywords.setText(entry.getKeywords());
        title.setEnabled(false);
        body.setEnabled(false);
        keywords.setEnabled(false);
        mDateHolder.setEnabled(false);
    }

    private void setupUi() {
        setupDate();

        if (mCurrentAudioPath != null) {
            enableAudioPlayBack();
        }

        if (mCurrentPhotoPath != null) {
            setEntryMedia(Uri.parse(mCurrentPhotoPath), Constants.CAMERA_PHOTO);
        }

        if (mCurrentVideoPath != null) {
            setEntryMedia(Uri.parse(mCurrentVideoPath), Constants.CAMERA_VIDEO);
        }

        if (mCurrentGalleryPath != null) {
            setEntryMedia(Uri.parse(mCurrentGalleryPath), Constants.GALLERY_MEDIA);
        }

        if (mLastLocation != null) {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMapFragment.getView().setVisibility(View.VISIBLE);

            mMap.addMarker(new MarkerOptions()
                    .title("Entry Location")
                    .position(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));


            mBottomMenu.findItem(R.id.action_gps).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_my_location_enabled_24dp));
        }

    }

    private void enableAudioPlayBack() {
        mBottomMenu.findItem(R.id.action_audio).setIcon(ContextCompat.getDrawable(EntryActivity.this, R.drawable.ic_mic_enabled_24dp));
        mAudioPlayer = new AudioPlayer(mCurrentAudioPath, (ImageView) mAudioHolder.findViewById(R.id.audio_play),
                (ImageView) mAudioHolder.findViewById(R.id.audio_pause),
                (ImageView) mAudioHolder.findViewById(R.id.audio_stop));
        mAudioHolder.setVisibility(View.VISIBLE);
        ((TextView) mAudioHolder.findViewById(R.id.audio_file_name)).setText(mCurrentAudioPath.substring(mCurrentAudioPath.lastIndexOf("/") + 1));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("sdf");
        // remove the images and video to stop leaks
        mEntryImage.setImageBitmap(null);
        mEntryVideo.setVideoURI(null);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void setupDate() {
        SimpleDateFormat parser = new SimpleDateFormat("hh:mm a");
        if (mDate == null)
            mDate = Calendar.getInstance();

        String time = parser.format(mDate.getTime());

        ((TextView) EntryActivity.this.findViewById(R.id.time)).setText(time);
        ((TextView) EntryActivity.this.findViewById(R.id.day)).setText(String.valueOf(mDate.get(Calendar.DAY_OF_MONTH)));
        ((TextView) EntryActivity.this.findViewById(R.id.month)).setText(new DateFormatSymbols().getMonths()[mDate.get(Calendar.MONTH)]);
        ((TextView) EntryActivity.this.findViewById(R.id.year)).setText(String.valueOf(mDate.get(Calendar.YEAR)));
        ((TextView) EntryActivity.this.findViewById(R.id.weekday)).setText(new DateFormatSymbols().getWeekdays()[mDate.get(Calendar.DAY_OF_WEEK)]);
        ((TextView) EntryActivity.this.findViewById(R.id.time)).setText(time);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("mDate", mDate);
        if (mLastLocation != null) {
            outState.putDouble("longitude", mLastLocation.getLongitude());
            outState.putDouble("latitude", mLastLocation.getLatitude());
        }
        outState.putString("mCurrentAudioPath", mCurrentAudioPath);
        outState.putString("mCurrentPhotoPath", mCurrentPhotoPath);
        outState.putString("mCurrentVideoPath", mCurrentVideoPath);
        outState.putString("mCurrentGalleryPath", mCurrentGalleryPath);
        outState.putBoolean("edited", edited);
        outState.putBoolean("readOnly", readOnly);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentAudioPath = savedInstanceState.getString("mCurrentAudioPath", null);
        mCurrentPhotoPath = savedInstanceState.getString("mCurrentPhotoPath", null);
        mCurrentVideoPath = savedInstanceState.getString("mCurrentVideoPath", null);
        mCurrentGalleryPath = savedInstanceState.getString("mCurrentGalleryPath", null);
        mDate = (Calendar) savedInstanceState.getSerializable("mDate");

        if (mDate == null) {
            mDate = Calendar.getInstance();
        }

        double longitude = savedInstanceState.getDouble("longitude", 0.0);
        double latitude = savedInstanceState.getDouble("latitude", 0.0);
        if (!(longitude == 0.0 && latitude == 0.0)) {
            mLastLocation = new Location("");
            mLastLocation.setLongitude(longitude);
            mLastLocation.setLatitude(latitude);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        mMainMenu = menu;
        // use amvMenu here
        inflater.inflate(R.menu.bottom_toolbar, mBottomMenu);
        inflater.inflate(R.menu.menu_entry, mMainMenu);

        if (readOnly) {
            mMainMenu.findItem(R.id.action_edit).setVisible(true);
            mMainMenu.findItem(R.id.action_save).setVisible(false);
            mBottomToolbar.setVisibility(View.GONE);
        }
        setupUi();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_save:
                Toast.makeText(EntryActivity.this, "save", Toast.LENGTH_SHORT).show();
                save();
                return true;
            case R.id.action_edit:
                findViewById(R.id.entry_title).setEnabled(true);
                findViewById(R.id.entry_body).setEnabled(true);
                findViewById(R.id.entry_keywords).setEnabled(true);
                mDateHolder.setEnabled(true);
                mBottomToolbar.setVisibility(View.VISIBLE);
                edited = true;
                readOnly = false;
                mMainMenu.findItem(R.id.action_edit).setVisible(false);
                mMainMenu.findItem(R.id.action_save).setVisible(true);
                return true;
            case android.R.id.home:
                if (readOnly) {
                    NavUtils.navigateUpFromSameTask(this);
                } else if (!exit) {
                    discardOrSave(item);
                } else {
                    deleteMedia();
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {
        saveMedia();
        String title = mTitle.getText().toString();
        String body = mBody.getText().toString();
        String keywords = mKeywords.getText().toString();

        String galleryPath = mCurrentGalleryPath;
        String photoPath = mCurrentPhotoPath;
        String videoPath = mCurrentVideoPath;
        String audioPath = mCurrentAudioPath;

        Entry temp = new Entry(title, body, keywords, galleryPath, photoPath, videoPath, audioPath, mLastLocation, mDate);

        boolean saved = false;
        if (edited) {
            temp.setId(mEntry.getId());
            saved = mEntryDAO.updateEntry(temp);
        } else {
            saved = mEntryDAO.createEntry(temp);
        }

        String message = "Entry saved successfully";
        if (!saved) {
            message = "Unable to save entry";
        }
        Toast.makeText(EntryActivity.this, message, Toast.LENGTH_SHORT).show();
        NavUtils.navigateUpFromSameTask(this);
    }

    private void saveMedia() {
        File file = null;

        if (mCurrentVideoPath != null) {
            file = new File(mCurrentVideoPath.replace("file:", ""));
        } else if (mCurrentPhotoPath != null) {
            file = new File(mCurrentPhotoPath.replace("file:", ""));
        }

        if (file != null) {
            Log.d("sdf", file.getAbsolutePath());
            Uri contentUri = Uri.fromFile(file);
            Log.d("sdf", contentUri.toString());

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
            sendBroadcast(mediaScanIntent);
        }
    }

    private void deleteMedia() {
        if (mCurrentPhotoPath != null) {
            File photo = new File(mCurrentPhotoPath);
            if (photo.exists())
                photo.delete();
        }

        if (mCurrentVideoPath != null) {
            File video = new File(mCurrentVideoPath);
            if (video.exists())
                video.delete();
        }
    }

    private boolean discardOrSave(final MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
        builder.setMessage(getString(R.string.discard_entry_dialog_message))
                .setTitle(getString(R.string.discard_entry_dialog_title));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                exit = true;

                if (item != null) { // parent home button was pressed
                    EntryActivity.this.onOptionsItemSelected(item);
                } else { // back button was pressed
                    EntryActivity.this.onBackPressed();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        return false;
    }

    @Override
    public void onBackPressed() {
        if (readOnly) {
            super.onBackPressed();
        } else if (!exit) {
            discardOrSave(null);
        } else {
            deleteMedia();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_video:
                handleVideo();
                return true;
            case R.id.action_camera:
                handleCamera();
                return true;
            case R.id.action_gallery:
                handleGallery();
                return true;
            case R.id.action_gps:
                handleGps();
                return true;
            case R.id.action_audio:
                handleAudio();
                return true;
            default:
                break;
        }
        return false;
    }

    private void handleAudio() {
        final MenuItem item = mBottomMenu.findItem(R.id.action_audio);
        if (mCurrentAudioPath != null) { // remove audio
            AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
            builder.setMessage(getString(R.string.remove_audio_dialog_message))
                    .setTitle(getString(R.string.remove_audio_dialog_title));
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mCurrentAudioPath = null;
                    mAudioHolder.setVisibility(View.GONE);
                    item.setIcon(ContextCompat.getDrawable(EntryActivity.this, R.drawable.ic_mic_24dp));
                }
            });
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // nothing
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else { // save new audio
            //  Check for audio permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                        Snackbar itemRemovedSnackbar = Snackbar
                                .make(findViewById(R.id.main_coord_layout), "Record Audio permission is needed to save voice.", Snackbar.LENGTH_LONG)
                                .setAction("Grant Permission", new View.OnClickListener() {
                                    @TargetApi(Build.VERSION_CODES.M)
                                    @Override
                                    public void onClick(View view) {
                                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, Constants.RECORD_AUDIO_PERMISSION);
                                    }
                                });
                        itemRemovedSnackbar.show();
                    }
                    return;
                }
            }

            if (!hasWritePermGranted()) {
                return;
            }

            MicDialogFragment audioDialog = new MicDialogFragment(new MicDialogFragment.MicDialogListener() {

                @Override
                public void onPositiveClick(String audioFilePath) {
                    if (audioFilePath != null) {
                        mCurrentAudioPath = audioFilePath;
                        enableAudioPlayBack();
                    }
                }

                @Override
                public void onNegativeClick() {

                }
            });
            audioDialog.setCancelable(false);
            audioDialog.show(getFragmentManager(), "audioCapture");
        }
    }

    private boolean hasWritePermGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Snackbar itemRemovedSnackbar = Snackbar
                            .make(findViewById(R.id.main_coord_layout), "Unable to create files on storage", Snackbar.LENGTH_LONG)
                            .setAction("Grant Permission", new View.OnClickListener() {
                                @TargetApi(Build.VERSION_CODES.M)
                                @Override
                                public void onClick(View view) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.WRITE_EXTERNAL_STORAGE_PERMISSION);
                                }
                            });
                    itemRemovedSnackbar.show();
                }
                return false;
            }
        }
        return true;
    }

    private void handleGps() {
        final MenuItem item = mBottomMenu.findItem(R.id.action_gps);

        if (mLastLocation != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
            builder.setMessage(getString(R.string.remove_gps_dialog_message))
                    .setTitle(getString(R.string.remove_gps_dialog_title));
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    item.setIcon(ContextCompat.getDrawable(EntryActivity.this, R.drawable.ic_my_location_24dp));
                    mLastLocation = null;
                    mMap.clear();
                    mMapFragment.getView().setVisibility(View.GONE);
                }
            });
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // nothing
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            mLastLocation = getLocation();
            if (mLastLocation == null) {
                return;
            }
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMapFragment.getView().setVisibility(View.VISIBLE);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            mMap.addMarker(new MarkerOptions()
                    .title("Entry Location")
                    .position(latLng));

            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_my_location_enabled_24dp));
            String location = mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude();
            Toast.makeText(this, location, Toast.LENGTH_SHORT).show();
        }
    }

    private Location getLocation() {

        //  Check for gps permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Snackbar itemRemovedSnackbar = Snackbar
                            .make(findViewById(R.id.main_coord_layout), "Location permission is needed to save your current location.", Snackbar.LENGTH_LONG)
                            .setAction("Grant Permission", new View.OnClickListener() {
                                @TargetApi(Build.VERSION_CODES.M)
                                @Override
                                public void onClick(View view) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, Constants.GPS_PERMISSION);
                                }
                            });
                    itemRemovedSnackbar.show();
                }
                return null;
            }
        }

        // Check if gps is enabled
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        String gpsProvider = LocationManager.GPS_PROVIDER;
        String networkProvider = LocationManager.NETWORK_PROVIDER;

        boolean isGpsEnabled = locationManager.isProviderEnabled(gpsProvider);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(networkProvider);

        if (!isGpsEnabled && !isNetworkEnabled) {
            Snackbar gpsDisabledSnackbar = Snackbar
                    .make(findViewById(R.id.main_coord_layout), "GPS is disabled on your device", Snackbar.LENGTH_LONG)
                    .setAction("Enable GPS", new View.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
            gpsDisabledSnackbar.show();
            return null;
        }

        Location location = null;


        //  Network -> GPS


        if (isNetworkEnabled) { // Failed to get gps location, try network
            location = locationManager.getLastKnownLocation(networkProvider);
        }

        if (isGpsEnabled && location == null) {
            location = locationManager.getLastKnownLocation(gpsProvider);
        }

        if (location == null) {// Failed to get network location, notify user
            final Snackbar retrieving = Snackbar.make(findViewById(R.id.main_coord_layout), "Retrieving location...", Snackbar.LENGTH_INDEFINITE);
            LocationListener myListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    handleGps();
                    retrieving.dismiss();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            if (isNetworkEnabled) {
                locationManager.requestSingleUpdate(networkProvider, myListener, Looper.myLooper());
                retrieving.show();
            } else if (isGpsEnabled) {
                locationManager.requestSingleUpdate(gpsProvider, myListener, Looper.myLooper());
                retrieving.show();
            } else {
                Snackbar.make(findViewById(R.id.main_coord_layout), "Unable to retrieve location", Snackbar.LENGTH_LONG).show();
            }
        }

        return location;
    }

    private void handleGallery() {
        final MenuItem item = mBottomMenu.findItem(R.id.action_gallery);
        if (mCurrentGalleryPath != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
            builder.setMessage(getString(R.string.remove_image_dialog_message))
                    .setTitle(getString(R.string.remove_image_dialog_title));
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    removeImageView();
                    removeVideoView();
                    item.setIcon(ContextCompat.getDrawable(EntryActivity.this, R.drawable.ic_photo_library_24dp));
                    mCurrentGalleryPath = null;
                }
            });
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // nothing
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            if (mCurrentPhotoPath != null || mCurrentVideoPath != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
                builder.setMessage(getString(R.string.replace_media_dialog_message))
                        .setTitle(getString(R.string.replace_media_dialog_title));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        chooseMedia();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                chooseMedia();
            }
        }
    }

    private void handleCamera() {
        final MenuItem item = mBottomMenu.findItem(R.id.action_camera);
        if (mCurrentPhotoPath != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
            builder.setMessage(getString(R.string.remove_image_dialog_message))
                    .setTitle(getString(R.string.remove_image_dialog_title));
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    removeImageView();
                    item.setIcon(ContextCompat.getDrawable(EntryActivity.this, R.drawable.ic_photo_camera_24dp));
                }
            });
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // nothing
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            if (mCurrentGalleryPath != null || mCurrentVideoPath != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
                builder.setMessage(getString(R.string.replace_media_dialog_message))
                        .setTitle(getString(R.string.replace_media_dialog_title));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        capturePhoto();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                capturePhoto();
            }
        }
    }

    private void handleVideo() {
        final MenuItem item = mBottomMenu.findItem(R.id.action_video);
        if (mCurrentVideoPath != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
            builder.setMessage(getString(R.string.remove_video_dialog_message))
                    .setTitle(getString(R.string.remove_video_dialog_title));
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    removeVideoView();
                    item.setIcon(ContextCompat.getDrawable(EntryActivity.this, R.drawable.ic_videocam_24dp));
                }
            });
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // nothing
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            if (mCurrentGalleryPath != null || mCurrentPhotoPath != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
                builder.setMessage(getString(R.string.replace_media_dialog_message))
                        .setTitle(getString(R.string.replace_media_dialog_title));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        captureVideo();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                captureVideo();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.GPS_PERMISSION:
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        handleGps();
                        break;
                    }
                }
                break;
            case Constants.RECORD_AUDIO_PERMISSION:
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        handleAudio();
                        break;
                    }
                }
                break;
        }
    }

    private void captureVideo() {

        if (!hasWritePermGranted()) {
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File videoFile = null;
            try {
                videoFile = getMediaPath(Constants.CAMERA_VIDEO);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (videoFile != null) {
                Log.d("sdf", Uri.fromFile(videoFile).toString());
                mCurrentVideoPath = "file:" + videoFile.getAbsolutePath();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
                startActivityForResult(intent, Constants.CAMERA_VIDEO);
            }
        }
    }

    private void chooseMedia() {
        Intent selectPhoto = new Intent(Intent.ACTION_PICK);
        selectPhoto.setType("video/*, images/*");
        startActivityForResult(selectPhoto, Constants.GALLERY_MEDIA);
    }

    private void capturePhoto() {

        if (!hasWritePermGranted()) {
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = getMediaPath(Constants.CAMERA_PHOTO);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                mCurrentPhotoPath = "file:" + photoFile.getAbsolutePath();
                Log.d("sdf", mCurrentPhotoPath);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(intent, Constants.CAMERA_PHOTO);
            }
        }
    }

    private File getMediaPath(int type) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss").format(new Date());
        String imageFileName = "journal_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Journal");
        storageDir.mkdirs();

        String fileType = "";
        switch (type) {
            case Constants.CAMERA_PHOTO:
                fileType = ".jpg";
                break;
            case Constants.CAMERA_VIDEO:
                fileType = ".mp4";
                break;
            default:
                throw new IOException("Unknown media type");
        }

        return File.createTempFile(imageFileName, fileType, storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CAMERA_VIDEO:
                if (resultCode == RESULT_OK) {
                    setEntryMedia(Uri.parse(mCurrentVideoPath), Constants.CAMERA_VIDEO);
                } else {
                    // TODO snackbar
                    Toast.makeText(this, "video not taken", Toast.LENGTH_SHORT).show();
                    mCurrentVideoPath = null;
                }
                break;
            case Constants.CAMERA_PHOTO:
                if (resultCode == RESULT_OK) {
                    setEntryMedia(Uri.parse(mCurrentPhotoPath), Constants.CAMERA_PHOTO);
                } else {
                    // TODO snackbar
                    Toast.makeText(this, "image not taken", Toast.LENGTH_SHORT).show();
                    mCurrentPhotoPath = null;
                }
                break;
            case Constants.GALLERY_MEDIA:
                if (resultCode == RESULT_OK) {
                    setEntryMedia(data.getData(), Constants.GALLERY_MEDIA);
                } else {
                    // TODO snackbar
                    Toast.makeText(this, "image not selected", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }
    }

    private void setEntryMedia(Uri mediaUri, int mediaLocation) {

        if (mediaUri.toString().toLowerCase().contains("video") || mediaUri.toString().toLowerCase().contains("mp4")) {
            mEntryVideo.setVisibility(View.VISIBLE);
            mEntryVideo.setVideoURI(mediaUri);
            mEntryVideo.requestFocus();
            if (mediaLocation == Constants.GALLERY_MEDIA) { // Video from gallery
                mCurrentGalleryPath = "content://media" + mediaUri.getPath();
                mCurrentPhotoPath = null;
                mCurrentVideoPath = null;
                mBottomMenu.findItem(R.id.action_gallery).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_photo_library_enabled_24dp));
                mBottomMenu.findItem(R.id.action_video).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_videocam_24dp));
                mBottomMenu.findItem(R.id.action_camera).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_photo_camera_24dp));
            } else if (mediaLocation == Constants.CAMERA_VIDEO) { // Video from camera
                mCurrentGalleryPath = null;
                mCurrentPhotoPath = null;
                mBottomMenu.findItem(R.id.action_gallery).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_photo_library_24dp));
                mBottomMenu.findItem(R.id.action_video).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_videocam_enabled_24dp));
                mBottomMenu.findItem(R.id.action_camera).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_photo_camera_24dp));
            }
            removeImageView();
        } else {
            try {
                InputStream imageStream = getContentResolver().openInputStream(mediaUri);
                Bitmap thumbnail = BitmapFactory.decodeStream(imageStream);
                mEntryImage.setVisibility(View.VISIBLE);
                mEntryImage.setImageBitmap(thumbnail);
                if (mediaLocation == Constants.GALLERY_MEDIA) { //Photo from gallery
                    mCurrentGalleryPath = "content://media" + mediaUri.getPath();
                    mCurrentPhotoPath = null;
                    mCurrentVideoPath = null;
                    mBottomMenu.findItem(R.id.action_gallery).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_photo_library_enabled_24dp));
                    mBottomMenu.findItem(R.id.action_video).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_videocam_24dp));
                    mBottomMenu.findItem(R.id.action_camera).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_photo_camera_24dp));
                } else if (mediaLocation == Constants.CAMERA_PHOTO) { //Photo from camera
                    mCurrentGalleryPath = null;
                    mCurrentVideoPath = null;
                    mBottomMenu.findItem(R.id.action_gallery).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_photo_library_24dp));
                    mBottomMenu.findItem(R.id.action_video).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_videocam_24dp));
                    mBottomMenu.findItem(R.id.action_camera).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_photo_camera_enabled_24dp));
                }
                removeVideoView();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeImageView() {
        if (mEntryImage.getVisibility() == View.VISIBLE) {
            mEntryImage.setImageBitmap(null);
            mEntryImage.setVisibility(View.GONE);
            mCurrentPhotoPath = null;
        }
    }

    private void removeVideoView() {
        if (mEntryVideo.getVisibility() == View.VISIBLE) {
            mEntryVideo.stopPlayback();
            mEntryVideo.setVideoURI(null);
            mEntryVideo.setVisibility(View.GONE);
            mCurrentVideoPath = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.entry_date_holder:
                final TimePickerFragment timeFrag = new TimePickerFragment(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        mDate.set(Calendar.MINUTE, minute);

                        setupDate();
                    }
                });

                DatePickerFragment dateFrag = new DatePickerFragment(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        mDate = Calendar.getInstance();
                        mDate.set(year, month, day);

                        timeFrag.show(getSupportFragmentManager(), "timePicker");
                    }
                });

                dateFrag.show(getSupportFragmentManager(), "datePicker");
                break;
            case R.id.entry_image:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri uri;
                if (mCurrentPhotoPath != null) {
                    uri = Uri.parse(mCurrentPhotoPath);
                } else {
                    uri = Uri.parse(mCurrentGalleryPath);
                }
                intent.setDataAndType(uri, "image/*");
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.entry_video:
                AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
                builder.setMessage(getString(R.string.remove_image_dialog_message))
                        .setTitle(getString(R.string.remove_image_dialog_title));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeVideoView();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // nothing
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.entry_image:
                builder = new AlertDialog.Builder(EntryActivity.this);
                builder.setMessage(getString(R.string.remove_image_dialog_message))
                        .setTitle(getString(R.string.remove_image_dialog_title));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeImageView();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // nothing
                    }
                });

                dialog = builder.create();
                dialog.show();
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }

    private void log(String s) {
        Log.d(getClass().getSimpleName(), s);
    }
}
