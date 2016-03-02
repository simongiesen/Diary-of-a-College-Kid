package science.anthonyalves.diaryofacollegekid.Utils;


import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Class to playback recorded audio within the app
 * Handles the onclick methods for the Play, Pause and Stop buttons
 */
public class AudioPlayer {

    MediaPlayer mMediaPlayer;

    int mPosition = 0;

    String audioPath;
    ImageView mPlayButton;
    ImageView mPauseButton;
    ImageView mStopButton;

    public AudioPlayer(String audioPath, ImageView playButton, ImageView pauseButton, ImageView stopButton) {
        this.audioPath = audioPath;
        this.mPlayButton = playButton;
        this.mPauseButton = pauseButton;
        this.mStopButton = stopButton;

        this.mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    play();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        this.mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });

        this.mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
    }

    public void play() throws IOException {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) { // resuming playback from a pause
            mMediaPlayer.seekTo(mPosition);
            mMediaPlayer.start();
            mStopButton.setVisibility(View.VISIBLE);
            mPauseButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.GONE);
        } else {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop();
                    mStopButton.setVisibility(View.GONE);
                    mPauseButton.setVisibility(View.GONE);
                    mPlayButton.setVisibility(View.VISIBLE);
                }
            });
            mMediaPlayer.setDataSource(audioPath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            mStopButton.setVisibility(View.VISIBLE);
            mPauseButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.GONE);
        }
    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPosition = mMediaPlayer.getCurrentPosition();
            mPauseButton.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.VISIBLE);
        }
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;

            mStopButton.setVisibility(View.GONE);
            mPauseButton.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.VISIBLE);
        }
    }
}
