package science.anthonyalves.diaryofacollegekid.db;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;

import science.anthonyalves.diaryofacollegekid.Utils.Constants;

/**
 * Journal entry object class
 */
public class Entry implements Parcelable {

    String title;
    String body;
    String keywords;

    String photoPath;
    String videoPath;
    String audioPath;
    String galleryPath;

    public String getGalleryPath() {
        return galleryPath;
    }

    public void setGalleryPath(String galleryPath) {
        this.galleryPath = galleryPath;
    }

    Location location;

    Calendar date;

    long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Entry(String title, String body, String keywords, String galleryPath, String photoPath, String videoPath, String audioPath, Location location, Calendar date) {
        this.title = title;
        this.body = body;
        this.keywords = keywords;
        this.photoPath = photoPath;
        this.videoPath = videoPath;
        this.audioPath = audioPath;
        this.location = location;
        this.date = date;
        this.galleryPath = galleryPath;
    }

    public Entry() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    @Override
    public String toString() {
        String temp = "_id: " + id + "\n";
        temp += " - title: " + title + "\n";
        temp += " - body: " + body + "\n";
        temp += " - keywords: " + keywords + "\n";
        temp += " - galleryPath: " + galleryPath + "\n";
        temp += " - photoPath: " + photoPath + "\n";
        temp += " - videoPath: " + videoPath + "\n";
        temp += " - audioPath: " + audioPath + "\n";

        try {
            temp += " - location: " + location.getLongitude() + ", " + location.getLatitude() + "\n";
        } catch (NullPointerException e) {
            temp += " - location: " + 0 + ", " + 0 + "\n";
        }

        SimpleDateFormat parser = new SimpleDateFormat(Constants.SDF_FORMAT);
        String time = parser.format(date.getTime());
        temp += " - date: " + time;

        return temp;
    }

    public static Comparator<Entry> titleCompare = new Comparator<Entry>() {

        @Override
        public int compare(Entry lhs, Entry rhs) {
            return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
        }
    };

    public static Comparator<Entry> idCompare = new Comparator<Entry>() {

        @Override
        public int compare(Entry lhs, Entry rhs) {
            long leftId = lhs.id;
            long rightId = rhs.id;
            if (leftId > rightId) {
                return 1;
            } else if (leftId == rightId) {
                return 0;
            } else {
                return -1;
            }
        }
    };

    public static Comparator<Entry> dateCompare = new Comparator<Entry>() {

        @Override
        public int compare(Entry lhs, Entry rhs) {
            return lhs.date.getTime().compareTo(rhs.date.getTime());
        }
    };


    public Entry(Parcel in) {
        title = in.readString();
        body = in.readString();
        keywords = in.readString();
        photoPath = in.readString();
        videoPath = in.readString();
        audioPath = in.readString();
        galleryPath = in.readString();
        location = (Location) in.readValue(Location.class.getClassLoader());
        date = (Calendar) in.readValue(Calendar.class.getClassLoader());
        id = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(body);
        dest.writeString(keywords);
        dest.writeString(photoPath);
        dest.writeString(videoPath);
        dest.writeString(audioPath);
        dest.writeString(galleryPath);
        dest.writeValue(location);
        dest.writeValue(date);
        dest.writeLong(id);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {
        @Override
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };
}
