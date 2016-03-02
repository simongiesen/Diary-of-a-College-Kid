package science.anthonyalves.diaryofacollegekid.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import science.anthonyalves.diaryofacollegekid.EntryActivity;
import science.anthonyalves.diaryofacollegekid.R;
import science.anthonyalves.diaryofacollegekid.Utils.Constants;
import science.anthonyalves.diaryofacollegekid.db.Entry;
import science.anthonyalves.diaryofacollegekid.db.EntryDAO;

/**
 * RecyclerView adapter holding journal entries
 */
public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.ViewHolder> {

    ArrayList<Entry> mData = null;
    EntryDAO mEntryDAO;

    Context mContext;

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView entryImage;
        ImageView entryAudio;
        ImageView entryPhoto;
        ImageView entryGallery;
        ImageView entryVideo;
        ImageView entryGps;

        TextView entryTitle;
        TextView entryBody;
        TextView entryDow;
        TextView entryMonth;
        TextView entryDay;


        public ViewHolder(View itemView) {
            super(itemView);
            entryAudio = (ImageView) itemView.findViewById(R.id.entry_row_mic);
            entryPhoto = (ImageView) itemView.findViewById(R.id.entry_row_photo);
            entryGallery = (ImageView) itemView.findViewById(R.id.entry_row_gallery);
            entryVideo = (ImageView) itemView.findViewById(R.id.entry_row_video);
            entryGps = (ImageView) itemView.findViewById(R.id.entry_row_gps);

            entryTitle = (TextView) itemView.findViewById(R.id.entry_row_title);
            entryMonth = (TextView) itemView.findViewById(R.id.entry_row_month);
            entryDay = (TextView) itemView.findViewById(R.id.entry_row_day);
            entryDow = (TextView) itemView.findViewById(R.id.entry_row_dow);
            entryBody = (TextView) itemView.findViewById(R.id.entry_row_preview);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Entry temp = mData.get(getAdapterPosition());
            Intent i = new Intent(mContext, EntryActivity.class);
            i.putExtra(Constants.ENTRY_INTENT, temp);
            mContext.startActivity(i);
        }
    }


    public EntryAdapter(Context context, ArrayList<Entry> data) {
        mData = data;
        mContext = context;
        notifyDataSetChanged();
        mEntryDAO = new EntryDAO(mContext);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.none_entry_row, parent, false);

        switch (viewType) {
            case Constants.PHOTO:
            case Constants.VIDEO:
                // TODO image = visible
                break;
            case Constants.AUDIO:
            case Constants.NONE:
            default:
                break;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.entryGps.setVisibility(View.VISIBLE);
        holder.entryPhoto.setVisibility(View.VISIBLE);
        holder.entryVideo.setVisibility(View.VISIBLE);
        holder.entryGallery.setVisibility(View.VISIBLE);
        holder.entryAudio.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemViewType(int position) {
        int type;
        if (mData.get(position).getPhotoPath() != null) {
            type = Constants.PHOTO;
        } else if (mData.get(position).getVideoPath() != null) {
            type = Constants.VIDEO;
        } else if (mData.get(position).getAudioPath() != null) {
            type = Constants.AUDIO;
        } else {
            type = Constants.NONE;
        }
        return type;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Entry temp = mData.get(position);

        if (temp.getPhotoPath() == null)
            holder.entryPhoto.setVisibility(View.GONE);
        else {
            /*
            new Thread() { // load image into view
                @Override
                public void run() {
                    InputStream imageStream = null;
                    try {
                        imageStream = mContext.getContentResolver().openInputStream(Uri.parse(temp.getPhotoPath()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    final Bitmap thumbnail = BitmapFactory.decodeStream(imageStream);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            holder.entryImage.setImageBitmap(thumbnail);
                        }
                    });
                }
            }.start();
            */

        }

        if (temp.getVideoPath() == null)
            holder.entryVideo.setVisibility(View.GONE);

        if (temp.getGalleryPath() == null)
            holder.entryGallery.setVisibility(View.GONE);

        if (temp.getAudioPath() == null)
            holder.entryAudio.setVisibility(View.GONE);

        if (temp.getLocation() == null)
            holder.entryGps.setVisibility(View.GONE);

        Calendar date = temp.getDate();
        holder.entryDow.setText(new DateFormatSymbols().getWeekdays()[date.get(Calendar.DAY_OF_WEEK)]);
        holder.entryDay.setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        holder.entryMonth.setText(new DateFormatSymbols().getMonths()[date.get(Calendar.MONTH)]);

        holder.entryTitle.setText(temp.getTitle());
        holder.entryBody.setText(temp.getBody());


    }

    public boolean removeEntry(int pos) {
        boolean itemRemoved = false;
        boolean successfullyDeleted = mEntryDAO.deleteEntry(mData.get(pos));
        if (successfullyDeleted) {
            mData.remove(pos);
            notifyItemRemoved(pos);
            itemRemoved = true;
        }

        return itemRemoved;
    }

    public Entry getEntry(int pos) {
        return mData.get(pos);
    }

    public boolean addEntry(int pos, Entry temp) {
        boolean entryAdded = false;
        boolean successfullyCreated = mEntryDAO.createEntry(temp);
        if (successfullyCreated) {
            mData.add(pos, temp);
            notifyItemInserted(pos);
            entryAdded = true;
        }

        return entryAdded;
    }

    public void sortTitle(boolean asc) { // asc = a-z
        Comparator<Entry> comparator = Entry.titleCompare;
        if (!asc) {
            comparator = Collections.reverseOrder(comparator);
        }
        sort(comparator);
    }

    public void sortDate(boolean asc) {
        Comparator<Entry> comparator = Entry.dateCompare;
        if (!asc) {
            comparator = Collections.reverseOrder(comparator);
        }
        sort(comparator);
    }

    public void sortId(boolean asc) {
        Comparator<Entry> comparator = Entry.idCompare;
        if (!asc) {
            comparator = Collections.reverseOrder(comparator);
        }
        sort(comparator);
    }

    private void sort(Comparator<Entry> comparator) {
        Collections.sort(mData, comparator);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    private void log(String s) {
        Log.d(getClass().getSimpleName(), s);
    }

}
