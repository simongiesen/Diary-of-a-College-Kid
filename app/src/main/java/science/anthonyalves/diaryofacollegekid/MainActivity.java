package science.anthonyalves.diaryofacollegekid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import science.anthonyalves.diaryofacollegekid.adapters.EntryAdapter;
import science.anthonyalves.diaryofacollegekid.db.Entry;
import science.anthonyalves.diaryofacollegekid.db.EntryDAO;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    EntryAdapter mAdapter;

    private SharedPreferences mSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator v = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(100);
                startActivity(new Intent(MainActivity.this, EntryActivity.class));
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.entry_recyclerview);
        initData();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Swipe to delete an entry from the recycler view
     */
    public ItemTouchHelper swipeDelete = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            final Entry temp = mAdapter.getEntry(position);

            Snackbar itemRemovedSnackbarFailed = Snackbar
                    .make(findViewById(R.id.main_coord_layout), "Failed to delete entry: " + temp.getTitle(), Snackbar.LENGTH_LONG);

            final Snackbar itemAddedSnackbarFailed = Snackbar
                    .make(findViewById(R.id.main_coord_layout), "Failed to add entry: " + temp.getTitle(), Snackbar.LENGTH_LONG);

            Snackbar itemRemovedSnackbar = Snackbar
                    .make(findViewById(R.id.main_coord_layout), "Deleted: " + temp.getTitle(), Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!mAdapter.addEntry(position, temp)) {
                                itemAddedSnackbarFailed.show();
                            }
                        }
                    });


            if (mAdapter.removeEntry(position)) {
                itemRemovedSnackbar.show();
            } else {
                itemRemovedSnackbarFailed.show();
            }
        }


    });

    private void initData() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        EntryDAO entryDAO = new EntryDAO(this);
        mAdapter = new EntryAdapter(this, entryDAO.getAll());
        swipeDelete.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        int sortId = mSharedPreferences.getInt("mLastSortMethod", 0);
        if (sortId != 0) {
            sortEntries(sortId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
         if (id == R.id.action_sort) {
            PopupMenu popup = new PopupMenu(this, findViewById(R.id.action_sort));
            popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int sortId = item.getItemId();
                    mSharedPreferences.edit().putInt("mLastSortMethod", sortId).apply();
                    sortEntries(sortId);
                    return true;
                }
            });
            popup.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortEntries(int sortId) {
        if (sortId == R.id.sort_date_asc) {
            mAdapter.sortDate(true);
        } else if (sortId == R.id.sort_date_desc) {
            mAdapter.sortDate(false);
        } else if (sortId == R.id.sort_title_asc) {
            mAdapter.sortTitle(true);
        } else if (sortId == R.id.sort_title_desc) {
            mAdapter.sortTitle(false);
        }
    }

    private void log(String s) {
        Log.d(getClass().getSimpleName(), s);
    }
}
