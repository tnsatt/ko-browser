package com.xlab.vbrowser.bookmark.service;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.xlab.vbrowser.fragment.BrowserFragment;
import com.xlab.vbrowser.z.utils.Toast;

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

    public static Bookmark[] getAllFolders(Context context){
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return new Bookmark[]{new Bookmark()};
        }
        Bookmark[] lst = bookmarkDb.bookmarkDao().getAllFolders();
        Bookmark[] data = new Bookmark[lst.length+1];
        data[0] = new Bookmark();
        for(int i = 0; i<lst.length; i++){
            data[i+1] = lst[i];
        }
        return data;
    }

    public static Bookmark[] getChildrens(Context context, int parentId){
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return new Bookmark[0];
        }

        return bookmarkDb.bookmarkDao().getChildrens(parentId);
    }

    public static int countChildren(Context context, int parentId) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return ERROR_RESULT;
        }

        return bookmarkDb.bookmarkDao().countChildren(parentId);
    }

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

    private static int deleteByUrl(Context context, String url) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return 0;
        }

        return bookmarkDb.bookmarkDao().deleteByUrl(url);
    }

    private static int deleteById(Context context, int id) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return 0;
        }

        return bookmarkDb.bookmarkDao().deleteById(id);
    }

    public static void clearAll(final Context context) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return;
        }

        bookmarkDb.bookmarkDao().clear();
    }

    public static int deleteBookmark(final Context context, final Bookmark bookmark) {
        BookmarkDb bookmarkDb = BookmarkDb.getInstance(context);

        if (bookmarkDb == null) {
            return 0;
        }

//        bookmarkDb.bookmarkDao().deleteBookmarks(bookmark);
        return bookmarkDb.bookmarkDao().deleteById(bookmark.id);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.SDialog);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_bookmark_edit, null);
        EditText title = view.findViewById(R.id.title);
        EditText url = view.findViewById(R.id.url);
        title.setText(bookmark.title);
        url.setText(bookmark.url);
        LinearLayout buttons = view.findViewById(R.id.buttons);
        buttons.setVisibility(View.VISIBLE);
        View delete = view.findViewById(R.id.delete);
        View cancel = view.findViewById(R.id.cancel);
        View save = view.findViewById(R.id.save);
        Spinner parents = view.findViewById(R.id.parents);
        builder.setView(view)
                .setTitle("Edit Bookmark").setIcon(R.drawable.ic_star_full_s);
        AlertDialog dialog = builder.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
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
                String newurl = url.getText().toString().trim();
                if(newurl.isEmpty()){
                    url.setError("Require a URL");
                    return;
                }
                dialog.dismiss();
                new BackgroundTask(new IBackgroundTask() {
                    private String prevurl = null;
                    @Override
                    public void run() {
                        prevurl = bookmark.url;
                        String newurl = url.getText().toString();
                        bookmark.title = title.getText().toString();
                        bookmark.url = newurl;
                        Bookmark parent = (Bookmark) parents.getSelectedItem();
                        if(parent!=null) bookmark.parentId = parent.id;
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

        new BackgroundTask(new IBackgroundTask() {
            Bookmark[] bookmarks;
            @Override
            public void run() {
                bookmarks = getAllFolders(context);
            }

            @Override
            public void onComplete() {
                int pos = -1;
                for(int i=0; i< bookmarks.length; i++){
                    if(bookmark.parentId==bookmarks[i].id){
                        pos = i;
                        break;
                    }
                }
                parents.setAdapter(createBookmarkAdapter(context, bookmarks));
                if(pos>=0) parents.setSelection(pos);
            }
        }).execute();
    }
    public static void addOrRemoveBookmark2(final Context context, final String title, final String url, final ImageButton bookmarkView) {
        final boolean isAdded = (boolean) bookmarkView.getTag();
        if(!isAdded) bookmarkView.setEnabled(false);

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
                    clearBookmarkEvent.setValue(url==null?"":url);
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
    public static void goInsertBookmark(Context context, Bookmark bookmark, Runnable run){
        new BackgroundTask(new IBackgroundTask() {
            @Override
            public void run() {
                insertBookmark(context, bookmark);
            }

            @Override
            public void onComplete() {
                if(run!=null) run.run();
            }
        }).execute();
    }
    public static void goInsertBookmark(Context context, Bookmark bookmark){
        goInsertBookmark(context, bookmark, null);
    }

    public static ArrayAdapter<Bookmark> createBookmarkAdapter(Context context, Bookmark[] data){
        ArrayAdapter<Bookmark> lst = new ArrayAdapter<Bookmark>(context, R.layout.support_simple_spinner_dropdown_item, data);
        return lst;
    }
}
