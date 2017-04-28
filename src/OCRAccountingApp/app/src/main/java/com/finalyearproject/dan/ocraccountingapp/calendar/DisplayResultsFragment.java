package com.finalyearproject.dan.ocraccountingapp.calendar;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.amazonaws.AmazonClientException;
import com.finalyearproject.dan.ocraccountingapp.nosql.noSQLObj;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.ContentItem;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.ContentProgressListener;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.UserFileManager;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSConfiguration;
import com.finalyearproject.dan.ocraccountingapp.amazon.util.ThreadUtils;
import com.amazonaws.regions.Regions;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.DynamoDBUtils;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLOperation;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLResult;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLResultListAdapter;
import com.finalyearproject.dan.ocraccountingapp.util.ContentHelper;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DisplayResultsFragment extends Fragment {
    private static final String LOG_TAG = DisplayResultsFragment.class.getSimpleName();

    // The performed DB operation
    private static NoSQLOperation noSQLOperation;

    // Executor to handle getting more results in the background.
    private final Executor singleThreadedExecutor = Executors.newSingleThreadExecutor();

    // A flag indicating all results have been retrieved
    private volatile boolean doneRetrievingResults = false;

    // List view showing the results
    private ListView resultsList;

    private NoSQLResultListAdapter resultsListAdapter;

    // The user file manager
    private UserFileManager userFileManager;

    private final CountDownLatch userFileManagerCreatingLatch = new CountDownLatch(1);


    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        return inflater.inflate(R.layout.fragment_calendar_display_results, container, false);
    }

    private void getNextResults() {
        // if there are more results to retrieve.
        if (!doneRetrievingResults) {
            doneRetrievingResults = true;
            // Get next results group in the background.
            singleThreadedExecutor.execute(new Runnable() {
                List<NoSQLResult> results = null;
                @Override
                public void run() {
                    try {
                        results = noSQLOperation.getNextResultGroup();
                    } catch (final AmazonClientException ex) {
                        Log.e(LOG_TAG, "Failed loading additional results.", ex);
                        DynamoDBUtils.showErrorDialogForServiceException(getActivity(),
                            getString(R.string.nosql_dialog_title_failed_loading_more_results), ex);
                    }
                    if (results == null) {
                        return;
                    }
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            doneRetrievingResults = false;
                            resultsListAdapter.addAll(results);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        final String identityId = AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID();

        // The s3 bucket
        String bucket = AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET;
        // The s3 Prefix where the UserFileManager is rooted
        String prefix = "private/" + identityId + "/";
        // The S3 bucket region
        Regions region = AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET_REGION;

        AWSMobileClient.defaultMobileClient()
                .createUserFileManager(bucket, prefix, region,
                        new UserFileManager.BuilderResultHandler() {
                            @Override
                            public void onComplete(final UserFileManager userFileManager) {
                                if (!isAdded()) {
                                    userFileManager.destroy();
                                    return;
                                }

                                DisplayResultsFragment.this.userFileManager = userFileManager;
                                userFileManagerCreatingLatch.countDown();
                            }
                        });


        // Reset the results in case of screen rotation.
        noSQLOperation.resetResults();

        // get the list
        resultsList = (ListView) view.findViewById(R.id.nosql_show_results_list);
        // create the list adapter
        resultsListAdapter = new NoSQLResultListAdapter(getContext());

        // set the adapter.
        resultsList.setAdapter(resultsListAdapter);

        resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                resultsList.showContextMenuForChild(view);
            }
        });

        // set up a listener to load more items when they scroll to the bottom.
        resultsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
            }

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    getNextResults();
                }
            }
        });

        resultsList.setOnCreateContextMenuListener(this);

        getNextResults();
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View view,
                                    final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.add(0, R.id.nosql_context_menu_entry_view_image, 0, R.string.nosql_context_menu_entry_view_item_text);
        menu.add(0, R.id.nosql_context_menu_entry_update, 0, R.string.nosql_context_menu_entry_update_item_text);
        menu.add(0, R.id.nosql_context_menu_entry_delete, 0, R.string.nosql_context_menu_entry_delete_item_text);

        menu.setHeaderTitle(R.string.nosql_context_menu_title_for_results_text);
    }

    void promptToDeleteItemAt(int position) {
        final NoSQLResultListAdapter listAdapter = (NoSQLResultListAdapter) resultsList.getAdapter();
        final NoSQLResult result = listAdapter.getItem(position);

        assert result != null;
        final String file = result.getFilePath();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.nosql_dialog_title_confirm_delete_item_text)
            .setNegativeButton(android.R.string.cancel, null);
        builder.setMessage("Are you sure ou want to delete " + file + "?");

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            result.deleteItem();
                            userFileManager.deleteRemoteContent(result.getFilePath(), new UserFileManager.ResponseHandler() {
                                @Override
                                public void onSuccess() {
                                    dialog.dismiss();
                                    ThreadUtils.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            listAdapter.remove(result);
                                            listAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }

                                @Override
                                public void onError(final AmazonClientException ex) {
                                    dialog.dismiss();
                                }
                            });

                        } catch (final AmazonClientException ex) {
                            Log.e(LOG_TAG, "Failed deleting item.", ex);
                            DynamoDBUtils.showErrorDialogForServiceException(getActivity(),
                                getString(R.string.nosql_dialog_title_failed_delete_item_text), ex);
                        }
                    }
                }).start();
            }
        });
        builder.show();
    }

    void promptToUpdateItemAt(int position) {
        final NoSQLResultListAdapter listAdapter = (NoSQLResultListAdapter) resultsList.getAdapter();
        final NoSQLResult result = listAdapter.getItem(position);

        Log.e("COUNTLISTADAPTER: ", String.valueOf(listAdapter.getCount()));
        assert result != null;
        final String file = result.getFilePath();
        Log.e("FilePath: ", file);
/*
        final ProgressDialog dialog = new ProgressDialog(getActivity(), R.style.Dialog1);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
*/
        // Directory where all saved receipts are stored
        String fileDir = "/data/user/0/com.finalyearproject.dan.ocraccountingapp/files/" +
                "s3_ocraccountingapp-userfiles-mobilehub-1024067420/private/" +
                "eu-west-1:020b39f2-0c66-4758-becc-d68957f19f07/content/";
        // Path to file
        final String filePath = fileDir + file;
        final String recTitle = result.getFriendlyName();
        final String recTotal = result.getTotal();
        final String recDate = result.getDate();
        final String recCatagory = result.getCategory();


        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.nosql_dialog_title_confirm_update_item_text)
            .setNegativeButton(android.R.string.cancel, null);
        builder.setMessage(R.string.nosql_dialog_message_confirm_update_item_text);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {

                // fetch file
                userFileManager.getContent(file, new ContentProgressListener() {
                    @Override
                    public void onSuccess(final ContentItem contentItem) {
                        dialog.dismiss();
                    }
                    @Override
                    public void onProgressUpdate(final String fileName, final boolean isWaiting,
                                                 final long bytesCurrent, final long bytesTotal) {
                    }

                    @Override
                    public void onError(final String fileName, final Exception ex) {
                        dialog.dismiss();
                        //showError(R.string.error_message_download_file, ex.getMessage());
                    }
                });

                // create instance of noSQLObj
                noSQLObj noSqlObj = new noSQLObj();
                noSqlObj.setObj(result);


                Bundle args = new Bundle();
                args.putSerializable("noSQLObj", noSqlObj);
                args.putString("file_path_arg", filePath);
                args.putString("TITLE", recTitle);
                args.putString("TOTAL", recTotal);
                args.putString("DATE", recDate);
                args.putString("CATAGORY", recCatagory);

                Fragment fragment = new EditFragment();
                fragment.setArguments(args);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.main_fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        builder.show();
    }


    void promptToViewItemAt(int position) {
        final NoSQLResultListAdapter listAdapter = (NoSQLResultListAdapter) resultsList.getAdapter();
        final NoSQLResult result = listAdapter.getItem(position);

        Log.e("COUNTLISTADAPTER: ", String.valueOf(listAdapter.getCount()));
        assert result != null;
        final String file = result.getFilePath();
        Log.e("FilePath: ", file);

        final ProgressDialog dialog = new ProgressDialog(getActivity(), R.style.Dialog1);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Please wait...");
        dialog.setCancelable(false);
        dialog.show();

        // Directory where all saved receipts are stored
        String fileDir = "/data/user/0/com.finalyearproject.dan.ocraccountingapp/files/" +
                "s3_ocraccountingapp-userfiles-mobilehub-1024067420/private/" +
                "eu-west-1:020b39f2-0c66-4758-becc-d68957f19f07/content/";
        // Path to file
        final String filePath = fileDir + file;
        userFileManager.getContent(file, new ContentProgressListener() {
            @Override
            public void onSuccess(final ContentItem contentItem) {
                ContentHelper.openFileViewer(getActivity(), new File(filePath));
                dialog.dismiss();
            }
            @Override
            public void onProgressUpdate(final String fileName, final boolean isWaiting,
                                         final long bytesCurrent, final long bytesTotal) {
                dialog.setProgress((int) bytesCurrent);
            }

            @Override
            public void onError(final String fileName, final Exception ex) {
                dialog.dismiss();
                //showError(R.string.error_message_download_file, ex.getMessage());
            }
        });
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.nosql_context_menu_entry_update) {
            promptToUpdateItemAt(info.position);
            return true;

        } else if (item.getItemId() == R.id.nosql_context_menu_entry_delete) {
            // pop confirmation dialog.
            promptToDeleteItemAt(info.position);
            return true;
        }
        else if (item.getItemId() == R.id.nosql_context_menu_entry_view_image) {
            // pop confirmation dialog.
            promptToViewItemAt(info.position);
            return true;
        }
        return false;
    }

    public void setOperation(final NoSQLOperation operation) {
        noSQLOperation = operation;
    }



}
