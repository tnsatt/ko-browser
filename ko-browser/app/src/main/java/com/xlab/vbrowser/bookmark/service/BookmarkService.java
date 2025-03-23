package com.xlab.vbrowser.bookmark.service;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xlab.vbrowser.fragment.BrowserFragment;
import com.xlab.vbrowser.z.Toast;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.architecture.NonNullLiveData;
import com.xlab.vbrowser.architecture.NonNullMutableLiveData;
import com.xlab.vbrowser.bookmark.db.BookmarkDb;
import com.xlab.vbrowser.bookmark.entity.Bookmark;
import com.xlab.vbrowser.session.Session;
import com.xlab.vbrowser.session.SessionManager;
import com.xlab.vbrowser.utils.BackgroundTask;
import com.xlab.vbrowser.utils.IBackgroundTask;

/**
 * Created by nguyenducthuan on 3/25/18.
 */

public class BookmarkService {
    public static int ERROR_RESULT = -9999;
    //This value is used as a event to notify client when history is cleared
    private final static NonNullMutableLiveData<String> addBookmarkEvent = new NonNullMutableLiveData<>("");
    private final static NonNullMutableLiveData<String> clearBookmarkEvent = new NonNullMutableLiveData<>("");
    private final static NonNullMutableLiveData<Long> clearAllBookmarksEvent = new NonNullMutableLiveData<>(0l);

    private static Bookmark[] getBookmarkByUrl(Context context, String url){
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return null;
        }

        return bookmarkDb.bookmarkDao().getBookmarkByUrl(url);
    }
    private static int countBookmarkByUrl(Context context, String url) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return ERROR_RESULT;
        }

        return bookmarkDb.bookmarkDao().countBookmarkByUrl(url);
    }

    private static long insertBookmark(Context context, Bookmark bookmark) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return ERROR_RESULT;
        }

        return bookmarkDb.bookmarkDao().insertBookmark(bookmark);
    }

    private static void updateBookmark(Context context, Bookmark bookmark) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return;
        }

        bookmarkDb.bookmarkDao().updateBookmark(bookmark);
    }

    private static void deleteByUrl(Context context, String url) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return;
        }

        bookmarkDb.bookmarkDao().deleteByUrl(url);
    }

    private static void deleteById(Context context, int id) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return;
        }

        bookmarkDb.bookmarkDao().deleteById(id);
    }

    public static void clearAll(final Context context) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return;
        }

        bookmarkDb.bookmarkDao().clear();
    }

    public static void deleteBookmark(final Context context, final Bookmark bookmark) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return;
        }

        bookmarkDb.bookmarkDao().deleteBookmarks(bookmark);
    }

    public static Bookmark[] loadBookmarks(final Context context, final long lastAccesstime, String queryText, final int limitRecords) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return new Bookmark[0];
        }

        if (TextUtils.isEmpty(queryText.trim())) {
            return bookmarkDb.bookmarkDao().loadBookmarks(lastAccesstime, limitRecords);
        }
        else {
            queryText = "%" + queryText + "%";
            return bookmarkDb.bookmarkDao().loadBookmarks(lastAccesstime, queryText, limitRecords);
        }
    }

    /**
     * Determine the url is added to bookmark or not, and then update bookmarkView
     */
    public static void loadBookmark(final Context context, final String url, final ImageButton bookmarkView, final ImageView earthView) {
        bookmarkView.setEnabled(false);

        new BackgroundTask(new IBackgroundTask() {
            long mResult = -1;

            @Override
            public void run() {
                mResult = countBookmarkByUrl(context, url);
             }

            @Override
            public void onComplete() {
                if (mResult == BookmarkService.ERROR_RESULT) {
                    bookmarkView.setVisibility(View.GONE);
                    bookmarkView.setEnabled(true);
                    return;
                }

                Session session = SessionManager.getInstance().getCurrentSession();

                if (session != null && session.getUrl() != null && url.equals(session.getUrl().getValue())) {
                    //This tag is used when user press the button to know bookmark is added or not yet.
                    bookmarkView.setTag(mResult > 0);
                    bookmarkView.setImageResource(mResult > 0 ? R.drawable.ic_star_s_enabled : R.drawable.ic_star_s);
                    bookmarkView.setVisibility(View.VISIBLE);
                    bookmarkView.setEnabled(true);
                    earthView.setVisibility(View.GONE);
                }
            }
        }).execute();
    }

    public static void addOrRemoveBookmark(final Context context, final String title, final String url, final ImageButton bookmarkView) {
        final boolean isAdded = (boolean) bookmarkView.getTag();
        bookmarkView.setEnabled(false);

        new BackgroundTask(new IBackgroundTask() {
            long result = - 1;

            @Override
            public void run() {
                if (isAdded) {
                    deleteByUrl(context, url);
                }
                else {
                    Bookmark bookmark = new Bookmark();
                    bookmark.accessTime = System.currentTimeMillis();
                    bookmark.isFolder = false;
                    bookmark.parentId = 0;
                    bookmark.title = title;
                    bookmark.url = url;
                    result = insertBookmark(context, bookmark);
                }
            }

            @Override
            public void onComplete() {
                if (result == BookmarkService.ERROR_RESULT) {
                    return;
                }

                updateView(context, url, bookmarkView, !isAdded);
            }
        }).execute();
    }
    private static void updateView(Context context, String url, ImageView bookmarkView, boolean isAdded){
        bookmarkView.setTag(isAdded);
        bookmarkView.setEnabled(true);
        bookmarkView.setImageResource(isAdded ? R.drawable.ic_star_s_enabled : R.drawable.ic_star_s);
        Toast.makeText(bookmarkView.getContext(),
                isAdded ? bookmarkView.getContext().getString(R.string.bookmarkAddedInfo)
                        : bookmarkView.getContext().getString(R.string.bookmarkRemovedInfo), Toast.LENGTH_SHORT).show();
        updateAllBookmarks(context, url);
    }
    private static void showBookmark(Context context, String url, ImageButton bookmarkView){
        new BackgroundTask(new IBackgroundTask() {
            public Bookmark bookmark = null;
            @Override
            public void run() {
                Bookmark[] bookmarks = getBookmarkByUrl(context, url);
                if(bookmarks.length>0){
                    bookmark = bookmarks[0];
                }
            }

            @Override
            public void onComplete() {
                if(bookmark == null){
                    updateView(context, url, bookmarkView, false);
                    return;
                }
                showBookmarkDialog(context, bookmark, bookmarkView);
            }
        }).execute();
    }
    private static void showBookmarkDialog(Context context, Bookmark bookmark, ImageView bookmarkView){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LinearLayout view = new LinearLayout(context);
        view.setPadding(20, 20, 20, 20);
        view.setOrientation(LinearLayout.VERTICAL);
        EditText title = new EditText(context);
        EditText url = new EditText(context);
        title.setText(bookmark.title);
        url.setText(bookmark.url);
        view.addView(title);
        view.addView(url);
        LinearLayout buttons = new LinearLayout(context);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        view.addView(buttons);
        Button delete = new Button(context);
        delete.setText("Delete");
        delete.setBackgroundColor(Color.RED);
        Button cancel = new Button(context);
        cancel.setText("Cancel");
        Button save = new Button(context);
        save.setText("Save");
        save.setBackgroundColor(Color.GREEN);
        TextView temp = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, // width 0dp
                LinearLayout.LayoutParams.WRAP_CONTENT, // height
                1.0f // weight
        );
        temp.setLayoutParams(params);
        buttons.addView(delete);
        buttons.addView(temp);
        buttons.addView(cancel);
        buttons.addView(save);

        builder.setView(view)
                .setTitle(bookmark.title);
        AlertDialog dialog = builder.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                updateView(context, bookmark.url, bookmarkView, true);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                new BackgroundTask(new IBackgroundTask() {
                    @Override
                    public void run() {
                        deleteBookmark(context, bookmark);
                    }

                    @Override
                    public void onComplete() {
                        updateView(context, bookmark.url, bookmarkView, false);
                    }
                }).execute();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                new BackgroundTask(new IBackgroundTask() {
                    private String prevurl = null;
                    @Override
                    public void run() {
                        prevurl = bookmark.url;
                        String newurl = url.getText().toString();
                        bookmark.title = title.getText().toString();
                        bookmark.url = newurl;
                        updateBookmark(context, bookmark);
                    }

                    @Override
                    public void onComplete() {
                        if(prevurl!=null && !prevurl.equals(bookmark.url)){
                            updateAllBookmarks(context, prevurl);
                            updateAllBookmarks(context, bookmark.url);
                        }else {
                            updateView(context, bookmark.url, bookmarkView, true);
                        }
                    }
                }).execute();
            }
        });
    }
    public static void addOrRemoveBookmark2(final Context context, final String title, final String url, final ImageButton bookmarkView) {
        final boolean isAdded = (boolean) bookmarkView.getTag();
        bookmarkView.setEnabled(false);

        new BackgroundTask(new IBackgroundTask() {
            long result = - 1;

            @Override
            public void run() {
                if (isAdded) {
                    showBookmark(context, url, bookmarkView);
                }
                else {
                    Bookmark bookmark = new Bookmark();
                    bookmark.accessTime = System.currentTimeMillis();
                    bookmark.isFolder = false;
                    bookmark.parentId = 0;
                    bookmark.title = title;
                    bookmark.url = url;
                    result = insertBookmark(context, bookmark);
                }
            }

            @Override
            public void onComplete() {
                if (result == BookmarkService.ERROR_RESULT) {
                    return;
                }
                if(isAdded) return;

                updateView(context, url, bookmarkView, !isAdded);
            }
        }).execute();
    }

    private static void updateAllBookmarks(Context context, String url){
        new BackgroundTask(new IBackgroundTask() {
            long mResult = -1;

            @Override
            public void run() {
                mResult = countBookmarkByUrl(context, url);
            }

            @Override
            public void onComplete() {
                if (mResult == BookmarkService.ERROR_RESULT) {
                    return;
                }
                updateAllFragment(context, url, mResult > 0);
            }
        }).execute();
    }

    public static void notifyUrl(Context context, String url){
        new BackgroundTask(new IBackgroundTask() {
            long mResult = -1;

            @Override
            public void run() {
                mResult = countBookmarkByUrl(context, url);
            }

            @Override
            public void onComplete() {
                if (mResult == BookmarkService.ERROR_RESULT) {
                    return;
                }
                if(mResult > 0){
                    addBookmarkEvent.setValue(url);
                }else{
                    clearBookmarkEvent.setValue(url);
                }
            }
        }).execute();
    }

    private static void updateAllFragment(Context context, String url, Boolean isAdded){
        if(url==null || !(context instanceof FragmentActivity)) return;
        url = url.trim();
        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        for(Fragment f: fragmentManager.getFragments()){
            if(!(f instanceof BrowserFragment)) continue;
            BrowserFragment bf = (BrowserFragment) f;
            String furl = bf.getUrl();
            if(furl==null || !url.equals(furl.trim())) continue;
            ImageButton bookmarkView = bf.getBookmarkView();
            bookmarkView.setTag(isAdded);
            bookmarkView.setEnabled(true);
            bookmarkView.setImageResource(isAdded ? R.drawable.ic_star_s_enabled : R.drawable.ic_star_s);
        }
    }

    public static void notifyClearBookmarkEvent(String url) {
        clearBookmarkEvent.setValue(url);
    }
    public static NonNullLiveData<String> getAddBookmarkEvent() {
        return addBookmarkEvent;
    }
    public static NonNullLiveData<String> getClearBookmarkEvent() {
        return clearBookmarkEvent;
    }

    public static void notifyClearAllBookmarksEvent() {
        clearAllBookmarksEvent.setValue(System.nanoTime());
    }

    public static NonNullLiveData<Long> getClearAllBookmarksEvent() {
        return clearAllBookmarksEvent;
    }

    public static void updateBookmarkData(Context context, Bookmark bookmark, Runnable runnable){
        new BackgroundTask(new IBackgroundTask() {
            @Override
            public void run() {
                updateBookmark(context, bookmark);
            }

            @Override
            public void onComplete() {
                if(runnable!=null) runnable.run();
            }
        }).execute();
    }
    public static void insertBookmarkTask(Context context, Bookmark bookmark){
        new BackgroundTask(new IBackgroundTask() {
            @Override
            public void run() {
                insertBookmark(context, bookmark);
            }

            @Override
            public void onComplete() {

            }
        }).execute();
    }
}
