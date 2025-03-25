package com.xlab.vbrowser.bookmark.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.bookmark.BookmarkActionListener;
import com.xlab.vbrowser.bookmark.adapter.BookmarkAdapter;
import com.xlab.vbrowser.bookmark.entity.Bookmark;
import com.xlab.vbrowser.bookmark.service.BookmarkService;
import com.xlab.vbrowser.locale.LocaleAwareAppCompatActivity;
import com.xlab.vbrowser.session.SessionManager;
import com.xlab.vbrowser.session.Source;
import com.xlab.vbrowser.styles.ThemeUtils;
import com.xlab.vbrowser.trackers.GaReport;
import com.xlab.vbrowser.utils.BackgroundTask;
import com.xlab.vbrowser.utils.IBackgroundTask;
import com.xlab.vbrowser.utils.UrlUtils;
import com.xlab.vbrowser.widget.EndlessRecyclerViewScrollListener;
import com.xlab.vbrowser.z.utils.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by nguyenducthuan on 3/21/18.
 */

public class BookmarkActivity extends LocaleAwareAppCompatActivity
                             implements BookmarkActionListener{
    private static final int NUMBER_ROW_PER_PAGE = 20;

    private View noDataView;
    private View dataView;
    private RecyclerView recyclerView;
    private View progressBarView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView pathView;
    private ImageButton upButton;
    private View topbar;

    private EndlessRecyclerViewScrollListener scrollListener;
    private long lastAccessTime = Long.MAX_VALUE;
    private boolean isLoading = false;
    private String queryText = "";
    private List<Bookmark> stacks = new ArrayList<>();
    private Bookmark currentBookmark = null;
    private int level = 0;
    private boolean isSearch = false;

    private BookmarkAdapter mBookmarkAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        noDataView = findViewById(R.id.noDataView);
        dataView = findViewById(R.id.dataView);
        progressBarView = findViewById(R.id.progressBarView);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        pathView = findViewById(R.id.path);
        upButton = findViewById(R.id.up);
        topbar = findViewById(R.id.topbar);
        progressBarView.setVisibility(View.VISIBLE);
        dataView.setVisibility(View.GONE);
        noDataView.setVisibility(View.GONE);

        View nightModeView = findViewById(R.id.nightModeView);
        ThemeUtils.loadNightmode(nightModeView, settings, this);

        setUpViews();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(true);

        applyLocale();

        loadData(currentBookmark);

        GaReport.sendReportScreen(getBaseContext(), BookmarkActivity.class.getName());
    }
    private void addNewBookmark(boolean isFolder){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.SDialog);
        builder.setTitle(isFolder?"Create Folder":"Create Bookmark")
                .setIcon(isFolder?R.drawable.ic_create_new_folder:R.drawable.ic_bookmark_add);
        LayoutInflater inf = getLayoutInflater();
        View view = inf.inflate(R.layout.dialog_bookmark_edit, null);
        TextView titleView = view.findViewById(R.id.title);
        TextView urlView = view.findViewById(R.id.url);
        Spinner parents = view.findViewById(R.id.parents);
        if(isFolder){
            urlView.setVisibility(View.GONE);
        }else{
            urlView.setVisibility(View.VISIBLE);
            urlView.setText("https://");
        }
        parents.setAdapter(BookmarkService.createBookmarkAdapter(getBaseContext(), new Bookmark[]{new Bookmark()}));
        builder.setView(view);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bookmark bookmark = new Bookmark();
                if(!isFolder){
                    String url = urlView.getText().toString().trim();
                    if(url.isEmpty()){
                        urlView.setError("Require a URL");
                        return;
                    }
                    bookmark.url = url;
                }
                bookmark.isFolder = isFolder;
                String title = titleView.getText().toString();
                bookmark.title = title;
                bookmark.accessTime = bookmark.createAt;
                Bookmark parent = (Bookmark) parents.getSelectedItem();
                bookmark.parentId = parent==null?0:parent.id;
                BookmarkService.goInsertBookmark(getBaseContext(), bookmark, new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "Bookmark created", Toast.LENGTH_SHORT).show();
                        if(isSearch) return;
                        if((currentBookmark==null && bookmark.parentId==0)
                        ||(currentBookmark!=null && currentBookmark.parentId==bookmark.parentId)){
                            loadData(currentBookmark);
                        }
                    }
                });
                dialog.dismiss();
            }
        });
        new BackgroundTask(new IBackgroundTask() {
            Bookmark[] data;
            @Override
            public void run() {
                if(data == null || data.length==0){
                    data = new Bookmark[]{new Bookmark()};
                }
                data = BookmarkService.getAllFolders(BookmarkActivity.this);
            }

            @Override
            public void onComplete() {
                parents.setAdapter(BookmarkService.createBookmarkAdapter(getBaseContext(), data));
            }
        }).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.bookmark_add_folder:
            case R.id.bookmark_add_new:
                addNewBookmark(item.getItemId()==R.id.bookmark_add_folder);
                break;
            case R.id.bookmark_reload:
                loadData(currentBookmark);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
        getMenuInflater().inflate( R.menu.menu_bookmark, menu);

        //Search
        MenuItem myActionMenuItem = menu.findItem( R.id.bookmark_search);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_bookmark_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                if (!queryText.equals(s)) {
                    queryText = s;
                    resetLoader();
                    loadData(currentBookmark);
                }

                GaReport.sendReportEvent(getBaseContext(), "ON_SEARCH_BOOKMARK", BookmarkActivity.class.getName());

                return true;
            }
        });

        return true;
    }

    @Override
    public void applyLocale() {
        setTitle(getString(R.string.bookmark));
    }

    private void setUpViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.d("Bookmark onLoadMore", page + "_" + totalItemsCount );
                loadData(currentBookmark, true);
            }
        };

        recyclerView.addOnScrollListener(scrollListener);

        mBookmarkAdapter = new BookmarkAdapter(this);
        recyclerView.setAdapter(mBookmarkAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(currentBookmark);
            }
        });
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bookmark bookmark = stacks.size()==0?null:stacks.remove(stacks.size()-1);
                loadData(bookmark);
            }
        });
    }

    private void resetLoader() {
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }

        isLoading = false;
        lastAccessTime = Long.MAX_VALUE;

        if (mBookmarkAdapter != null) {
            mBookmarkAdapter.clearAll();
        }

        if (scrollListener != null) {
            scrollListener.resetState();
        }
    }

    private void clearBookmark() {
        if (mBookmarkAdapter == null) {
            return;
        }

        progressBarView.setVisibility(View.VISIBLE);
        dataView.setVisibility(View.GONE);
        noDataView.setVisibility(View.GONE);

        new BackgroundTask(new IBackgroundTask() {
            @Override
            public void run() {
                BookmarkService.clearAll(getBaseContext());
            }

            @Override
            public void onComplete() {
                resetLoader();
                //Notify client
                BookmarkService.notifyClearAllBookmarksEvent();
                progressBarView.setVisibility(View.GONE);
                dataView.setVisibility(View.GONE);
                noDataView.setVisibility(View.VISIBLE);
                GaReport.sendReportEvent(getBaseContext(), "ON_CLEAR_BOOKMARK", BookmarkActivity.class.getName());
            }
        }).execute();
    }

    private void loadData() {
        loadData(null, false);
    }
    private void loadData(Bookmark bookmark){
        loadData(bookmark, false);
    }
    private void loadData(Bookmark bookmark, boolean more){
//        if (isLoading) {
//            return;
//        }
        Log.d("stacks", String.valueOf(stacks));
        level++;
        int lv = level;
        String queryText = this.queryText;
        if(queryText==null || queryText.trim().isEmpty()) this.currentBookmark = bookmark;
        if (lastAccessTime == Long.MAX_VALUE) {
            progressBarView.setVisibility(View.VISIBLE);
            noDataView.setVisibility(View.GONE);
            dataView.setVisibility(View.GONE);
        }

        isLoading = true;


        new BackgroundTask(new IBackgroundTask() {
            Bookmark[] bookmarks = null;
            Boolean isSearch = false;

            @Override
            public void run() {
                if(queryText!=null && !queryText.trim().isEmpty()) {
                    Log.d("bookmark", "search "+queryText);
                    isSearch = true;
                    bookmarks = BookmarkService.loadBookmarks(getBaseContext(), lastAccessTime, queryText, NUMBER_ROW_PER_PAGE);
                }else{
                    Log.d("bookmark", "load "+(bookmark==null?"/":bookmark.toString()));
                    bookmarks = BookmarkService.getChildrens(getBaseContext(), bookmark==null?0:bookmark.id);
                }
            }

            @Override
            public void onComplete() {
                if(lv!=level) return;
                Log.d("bookmark", "result "+bookmarks.length);
                BookmarkActivity.this.isSearch = isSearch;
                progressBarView.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if(queryText!=null && !queryText.trim().isEmpty()){
                    topbar.setVisibility(View.GONE);
                }else{
                    topbar.setVisibility(View.VISIBLE);
                    String path = getPath(bookmark);
                    pathView.setText(path);
                }

                if(!more || !isSearch) mBookmarkAdapter.clearAll();
                if (!more || lastAccessTime == Long.MAX_VALUE) {
                    //If is in first loading
                    if (bookmarks == null || bookmarks.length < 1) {
                        noDataView.setVisibility(View.VISIBLE);
                        dataView.setVisibility(View.GONE);
                        return;
                    }

                    noDataView.setVisibility(View.GONE);
                    dataView.setVisibility(View.VISIBLE);
                }

                for (Bookmark bookmark : bookmarks) {
                    mBookmarkAdapter.addBookmark(bookmark, isSearch);
                    lastAccessTime = bookmark.accessTime;
                }

                isLoading = false;
            }
        }).execute();
    }
    private String getTitleBookmark(Bookmark bookmark){
        if(bookmark==null || bookmark.id==0) return "root";
        return bookmark.title==null||bookmark.title.trim().isEmpty()?"<>":bookmark.title;
    }
    public String getPath(Bookmark bookmark){
        StringJoiner path = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            path = new StringJoiner("/");
            path.add("");
            if(stacks.size()>0){
                for(Bookmark b: stacks){
                    path.add(getTitleBookmark(b));
                }
            }
            if (bookmark != null && bookmark.id!=0) {
                path.add(getTitleBookmark(bookmark));
            }
            return path.length()<=1?"/":path.toString();
        }
        return "/";
    }

    @Override
    public void onRemoveBookmark(final Bookmark bookmark) {
        new BackgroundTask(new IBackgroundTask() {
            @Override
            public void run() {
                BookmarkService.deleteBookmark(getBaseContext(), bookmark);
            }

            @Override
            public void onComplete() {
                if (mBookmarkAdapter.getItemCount() < 1) {
                    progressBarView.setVisibility(View.GONE);
                    noDataView.setVisibility(View.VISIBLE);
                    dataView.setVisibility(View.GONE);
                }
            }
        }).execute();
    }

    @Override
    public void onOpenBookmark(String url) {
        if (url == null || !UrlUtils.isHttpOrHttps(url)) {
            Toast.makeText(this, "Not URL", Toast.LENGTH_SHORT).show();
            return;
        }

//        SessionManager.getInstance().openUrl(url);
        SessionManager.getInstance().createSession(Source.BOOKMARK, url);

        finish();
    }

    @Override
    public void onOpenBookmarkInNewTab(String url) {
        if (url == null || !UrlUtils.isHttpOrHttps(url)) {
            return;
        }

        SessionManager.getInstance().createSession(Source.BOOKMARK, url);

        finish();
    }

    @Override
    public void onOpenFolder(Bookmark bookmark) {
        if(currentBookmark!=null) stacks.add(currentBookmark);
        loadData(bookmark);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        resetLoader();
    }
}
