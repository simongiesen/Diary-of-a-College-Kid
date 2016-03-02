package science.anthonyalves.diaryofacollegekid.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import science.anthonyalves.diaryofacollegekid.R;
import science.anthonyalves.diaryofacollegekid.Utils.AudioPlayer;

/**
 * Dialog fragment to record audio from the device Microphone and playback the recorded
 * message.
 *
 * Reference MediaRecorder methods in http://developer.android.com/guide/topics/media/audio-capture.html
 */
public class MicDialogFragment extends DialogFragment implements View.OnClickListener {

    ImageView mMicButton;
    TextView mListeningText;
    ProgressBar mListeningCircle;

    ImageView mPlayButton;
    ImageView mPauseButton;
    ImageView mStopButton;

    AudioPlayer mAudioPlayer;


    View mView;

    MediaRecorder mRecorder;


    Activity mActivity;

    boolean isListening = false;
    public String audioFilePath;
    private int mPosition;

    public interface MicDialogListener {
        void onPositiveClick(String audioFilePath);

        void onNegativeClick();
    }

    @Override
    public void onDestroy() {
        stop();
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        super.onDestroy();
    }

    MicDialogListener mCallback;

    public MicDialogFragment(MicDialogListener callback) {
        mCallback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mActivity = getActivity();

        View view = inflater.inflate(R.layout.audio_input_layout, null);
        mView = view;

        mListeningText = (TextView) view.findViewById(R.id.audio_listening);
        mListeningCircle = (ProgressBar) view.findViewById(R.id.listening_circle);

        mPlayButton = (ImageView) view.findViewById(R.id.audio_play);
        mPauseButton = (ImageView) view.findViewById(R.id.audio_pause);
        mStopButton = (ImageView) view.findViewById(R.id.audio_stop);

        mMicButton = (ImageView) view.findViewById(R.id.audio_mic_image);
        mMicButton.setOnClickListener(this);

        builder.setView(view)
                .setCancelable(false)
                .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isListening) {
                            micClick();
                        }
                        mCallback.onPositiveClick(audioFilePath);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAudioFile();
                        mCallback.onNegativeClick();
                    }
                });
        return builder.create();
    }

    private void deleteAudioFile() {
        if (audioFilePath != null) {
            File temp = new File(audioFilePath);
            temp.delete();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_mic_image:
                micClick();
                break;
        }
    }

    private void stop() {
        if (mAudioPlayer != null)
            mAudioPlayer.stop();
    }

    private void micClick() {
        Vibrator v = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(50);

        if (isListening) {

            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;

            mMicButton.setImageResource(R.drawable.ic_mic_gray_24dp);
            mListeningText.setVisibility(View.GONE);
            mListeningCircle.setVisibility(View.GONE);

            mPlayButton.setVisibility(View.VISIBLE);
            mAudioPlayer = new AudioPlayer(audioFilePath, mPlayButton, mPauseButton, mStopButton);

        } else {
            stop();
            deleteAudioFile();
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(getFileName());
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("SDf", "prepare() failed");
            }

            mMicButton.setImageResource(R.drawable.ic_mic_enabled_24dp);
            mListeningText.setVisibility(View.VISIBLE);
            mListeningCircle.setVisibility(View.VISIBLE);

            mPlayButton.setVisibility(View.GONE);
            mPauseButton.setVisibility(View.GONE);
            mStopButton.setVisibility(View.GONE);

            mRecorder.start();

        }
        isListening = !isListening;
    }

    private String getFileName() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss").format(new Date());
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS + "/Journal");
        storageDir.mkdirs();

        audioFilePath = storageDir.getAbsolutePath() + "/" + timeStamp + ".3gp";
        File file = new File(audioFilePath);
        return file.getAbsolutePath();
    }
}
