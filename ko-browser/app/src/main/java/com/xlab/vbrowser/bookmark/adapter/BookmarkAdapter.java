package com.xlab.vbrowser.bookmark.adapter;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.bookmark.BookmarkActionListener;
import com.xlab.vbrowser.bookmark.entity.Bookmark;
import com.xlab.vbrowser.bookmark.service.BookmarkService;
import com.xlab.vbrowser.favicon.FaviconService;
import com.xlab.vbrowser.trackers.GaReport;
import com.xlab.vbrowser.utils.BackgroundTask;
import com.xlab.vbrowser.utils.IBackgroundTask;
import com.xlab.vbrowser.utils.UrlUtils;
import com.xlab.vbrowser.z.utils.Toast;
import com.xlab.vbrowser.z.menu.BookmarkContextMenu;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private final List<BookmarkData> mBookmarks = new ArrayList<>();
    private String mLastAccessTime = "";
    private BookmarkActionListener mBookmarkActionListener;


    public BookmarkAdapter(BookmarkActionListener bookmarkActionListener) {
        this.mBookmarkActionListener = bookmarkActionListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bookmark_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        try {
            updateView(holder, position);
        }
        catch (Exception e){}
    }

    private void updateView(final ViewHolder holder, final  int position) {
        final Context context = holder.itemView.getContext();

        final BookmarkData bookmarkData = mBookmarks.get(position);

        //Set time header if needed
        if (bookmarkData.isHeader) {
            holder.headerView.setVisibility(View.VISIBLE);
            holder.dataView.setVisibility(View.GONE);
            holder.headerView.setText(bookmarkData.accessTime);
            return;
        }

        holder.headerView.setVisibility(View.GONE);
        holder.dataView.setVisibility(View.VISIBLE);

        holder.titleTextView.setText(TextUtils.isEmpty(bookmarkData.bookmark.title) ? "Default Title" : bookmarkData.bookmark.title);
        holder.urlTextView.setText(bookmarkData.bookmark.url);

        holder.view.setSelected(bookmarkData.isSelected);

        if(bookmarkData.bookmark.isFolder){
            holder.urlTextView.setVisibility(View.GONE);
            holder.iconImageView.setImageDrawable(context.getDrawable(R.drawable.ic_folder_with_files_s));
        }else {
            holder.urlTextView.setVisibility(View.VISIBLE);
            String faviconPath = FaviconService.getFavicon(context, bookmarkData.bookmark.url);

            if (faviconPath == null) {
                holder.iconImageView.setImageDrawable(context.getDrawable(R.drawable.ic_star_full_s));
            } else {
                holder.iconImageView.setImageURI(Uri.fromFile(new File(faviconPath)));
            }
        }
        holder.openNewTab.setVisibility(bookmarkData.bookmark.isFolder?View.GONE:View.VISIBLE);

        //Set view action
        holder.dataView.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                if(bookmarkData.bookmark.isFolder){
                    mBookmarkActionListener.onOpenFolder(bookmarkData.bookmark);
                    return;
                }
//                mBookmarkActionListener.onOpenBookmark(bookmarkData.bookmark.url);
//                GaReport.sendReportEvent(context, "ON_OPEN_BOOKMARK_URL", BookmarkAdapter.class.getName());

                for(BookmarkData bm: mBookmarks){
                    bm.isSelected = false;
                }
                bookmarkData.isSelected = true;
                notifyDataSetChanged();
            }
        });

        holder.openNewTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBookmarkActionListener.onOpenBookmark(bookmarkData.bookmark.url);
                GaReport.sendReportEvent(context, "ON_OPEN_BOOKMARK_URL", BookmarkAdapter.class.getName());
            }
        });

        //Set long press action
        holder.dataView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                BookmarkContextMenu.show(context, bookmarkData.bookmark.url,  new BookmarkContextMenu.IActionMenu() {
                    @Override
                    public void onOpen() {
                        holder.dataView.performClick();
                    }

                    @Override
                    public void onOpenInNewTab() {
                        mBookmarkActionListener.onOpenBookmarkInNewTab(bookmarkData.bookmark.url);
                        GaReport.sendReportEvent(context, "ON_OPEN_BOOKMARK_IN_NEW_TAB", BookmarkAdapter.class.getName());
                    }

                    @Override
                    public void onDelete() {
                        new BackgroundTask(new IBackgroundTask() {
                            int res = -1;
                            @Override
                            public void run() {
                                res = BookmarkService.countChildren(context, bookmarkData.bookmark.id);
                            }

                            @Override
                            public void onComplete() {
                                if(res==BookmarkService.ERROR_RESULT){
                                    Toast.makeText(context, "Delete Error", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if(res > 0){
                                    Toast.makeText(context, "Folder not empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                new android.os.Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int index = 0; index < mBookmarks.size(); index++) {
                                            final BookmarkData data = mBookmarks.get(index);

                                            if (data == bookmarkData) {
                                                //Notify client
//                                        BookmarkService.notifyClearBookmarkEvent(data.bookmark.url);
                                                BookmarkService.notifyUrl(context, data.bookmark.url);

                                                mBookmarkActionListener.onRemoveBookmark(data.bookmark);
                                                mBookmarks.remove(index);
                                                notifyItemRemoved(index);
                                                deleteHeaderIfNeeded(data);
                                                GaReport.sendReportEvent(context, "ON_DELETE_BOOKMARK", BookmarkAdapter.class.getName());
                                            }
                                        }
                                    }
                                });
                            }
                        }).execute();
                    }


                    @Override
                    public void onShareLink() {
                        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, bookmarkData.bookmark.url);
                        context.startActivity(Intent.createChooser(shareIntent, context.getString(com.xlab.vbrowser.R.string.share_dialog_title)));
                    }

                    @Override
                    public void onCopyLink() {
                        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

                        if (clipboard == null) {
                            return;
                        }


                        final Uri uri = UrlUtils.parse(bookmarkData.bookmark.url);

                        if (uri == null) {
                            return;
                        }

                        final ClipData clip = ClipData.newUri(context.getContentResolver(), "URI", uri);
                        clipboard.setPrimaryClip(clip);
                    }

                    @Override
                    public void onEdit() {
                        Bookmark bookmark = bookmarkData.bookmark;
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.SDialog);
                        builder.setTitle("Edit Bookmark").setIcon(R.drawable.ic_star_bookmark_s);
                        View view = LayoutInflater.from(context).inflate(R.layout.dialog_bookmark_edit, null);
                        builder.setView(view);
                        final EditText titleInput = view.findViewById(R.id.title);
                        final EditText urlInput = view.findViewById(R.id.url);
                        titleInput.setText(bookmark.title);
                        urlInput.setText(bookmark.url);
                        if(bookmark.isFolder){
                            urlInput.setVisibility(View.GONE);
                        }
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String prevurl = bookmark.url;
                                String title = titleInput.getText().toString();
                                bookmark.title = title;
                                if(!bookmark.isFolder) {
                                    String url = urlInput.getText().toString();
                                    bookmark.url = url;
                                }
                                BookmarkService.updateBookmarkData(context, bookmark, new Runnable() {
                                    @Override
                                    public void run() {
                                        BookmarkAdapter.this.notifyDataSetChanged();
                                        BookmarkService.notifyUrl(context, bookmark.url);
                                        if(prevurl!=null && !prevurl.equals(bookmark.url)){
                                            BookmarkService.notifyUrl(context, prevurl);
                                        }
                                    }
                                });
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
                return true;
            }
        });
    }
    public void addBookmark(final Bookmark bookmark){
        addBookmark(bookmark, false);
    }
    public void addBookmark(final Bookmark bookmark, boolean isHeader) {
        if (bookmark == null) {
            return;
        }

        Date accessTime = new Date();
        accessTime.setTime(bookmark.accessTime);
        DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        String strAccessTime = format.format(accessTime);

        //Add time header if needed
        if (isHeader && !strAccessTime.equals(mLastAccessTime)) {
            final BookmarkData headerData = new BookmarkData();
            headerData.accessTime = strAccessTime;
            headerData.isHeader = true;
            mBookmarks.add(headerData);
            notifyItemInserted(mBookmarks.size() - 1);
            mLastAccessTime = strAccessTime;
        }

        final BookmarkData bookmarkData = new BookmarkData();
        bookmarkData.isHeader = false;
        bookmarkData.accessTime = strAccessTime;
        bookmarkData.bookmark = bookmark;
        mBookmarks.add(bookmarkData);
        notifyItemInserted(mBookmarks.size() - 1);
    }

    @Override
    public int getItemCount() {
        return mBookmarks.size();
    }

    public void deleteHeaderIfNeeded(final BookmarkData bookmarkData) {
        boolean hasItem = false;
        int posOfHeader = -1;

        for (int position = 0; position < mBookmarks.size(); position++) {
            BookmarkData data = mBookmarks.get(position);

            if (data.accessTime.equals(bookmarkData.accessTime)) {
                if (!data.isHeader) {
                    hasItem = true;
                }
                else {
                    posOfHeader = position;
                }
            }
        }

        if (!hasItem && posOfHeader >= 0) {
            mBookmarks.remove(posOfHeader);
            notifyItemRemoved(posOfHeader);
        }
    }

    public void clearAll() {
        mLastAccessTime = "";
        mBookmarks.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView headerView;
        public final View dataView;
        public final TextView titleTextView;
        public final TextView urlTextView;
        public final ImageView iconImageView;
        public final ImageView openNewTab;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            headerView = (TextView)itemView.findViewById(R.id.headerView);
            dataView = itemView.findViewById(R.id.dataView);
            iconImageView = (ImageView) itemView.findViewById(R.id.iconImageView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            urlTextView = (TextView) itemView.findViewById(R.id.urlTextView);
            openNewTab = itemView.findViewById(R.id.openNewTab);
        }

    }

    public static class BookmarkData {
        public boolean isHeader;
        public String accessTime;
        public Bookmark bookmark;
        public boolean isSelected = false;
    }

}
