package com.finalyearproject.dan.ocraccountingapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Regions;
import com.finalyearproject.dan.ocraccountingapp.mobile.AWSConfiguration;
import com.finalyearproject.dan.ocraccountingapp.mobile.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.mobile.content.ContentItem;
import com.finalyearproject.dan.ocraccountingapp.mobile.content.ContentProgressListener;
import com.finalyearproject.dan.ocraccountingapp.mobile.content.UserFileManager;
import com.finalyearproject.dan.ocraccountingapp.mobile.util.ThreadUtils;
import com.finalyearproject.dan.ocraccountingapp.testing.demo.nosql.DynamoDBUtils;
import com.finalyearproject.dan.ocraccountingapp.testing.demo.nosql.NoSQLOperation;
import com.finalyearproject.dan.ocraccountingapp.testing.demo.nosql.NoSQLResult;
import com.finalyearproject.dan.ocraccountingapp.testing.demo.nosql.NoSQLResultListAdapter;
import com.finalyearproject.dan.ocraccountingapp.testing.demo.nosql.NoSQLShowResultsDemoFragment;
import com.finalyearproject.dan.ocraccountingapp.testing.demo.nosql.NoSQLTableBase;
import com.finalyearproject.dan.ocraccountingapp.testing.demo.nosql.NoSQLTableFactory;
import com.finalyearproject.dan.ocraccountingapp.testing.util.ContentHelper;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FragmentContent2 extends Fragment {

    private static final String LOG_TAG = NoSQLShowResultsDemoFragment.class.getSimpleName();

    /** Bundle key for retrieving the table name from the fragment'startDate arguments. */
    public static final String BUNDLE_ARGS_TABLE_TITLE = "tableTitle";

    // The NoSQL Table demo operations will be run against.
    private NoSQLTableBase table;

    // The Application context
    private Context appContext;

    // The Runnable posted to show the spinner.
    private SpinnerRunner spinnerRunner;

    // The delay that must pass before showing a spinner
    private static final int SPINNER_DELAY_MS = 300;

    // Table name that we will be querying
    final String tableName = "receiptData";

    // An executor to handle getting more results in the background
    private final Executor singleThreadedExecutor = Executors.newSingleThreadExecutor();

    // A flag indicating all results have been retrieved
    private volatile boolean doneRetrievingResults = false;

    // The list view showing the results
    private ListView resultsList;

    private NoSQLResultListAdapter resultsListAdapter;

    private NoSQLOperation noSQLOp = null;

    private FragmentStatePagerAdapter adapterViewPager;

    /** The user file manager. */
    private UserFileManager userFileManager;

    private final CountDownLatch userFileManagerCreatingLatch = new CountDownLatch(1);

    String date1;
    String date2;

    private static final String startDate = "startDate";
    private static final String endDate = "endDate";

    public static FragmentContent newInstance(String date1, String date2) {
        FragmentContent fragmentFirst = new FragmentContent();
        Bundle args = new Bundle();
        args.putString(startDate, date1);
        args.putString(endDate, date2);

        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appContext = getActivity().getApplicationContext();
        date1 = getArguments().getString(startDate);
        date2 = getArguments().getString(endDate);


        final Context context = getActivity();
        if (context != null) {
            table = NoSQLTableFactory.instance(getContext()
                    .getApplicationContext()).getNoSQLTableByTableName(tableName);

            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_content, container, false);

        spinnerRunner = new SpinnerRunner();
        Log.e("ONCREATEVIEW EXECUTED", "A");

        // get the list
        resultsList = (ListView) view.findViewById(R.id.nosql_show_results_list);
        // create the list adapter
        resultsListAdapter = new NoSQLResultListAdapter(getContext());


/*

        MyTaskParams params = new MyTaskParams(date1, date2);
        LongOperation longOperation = new LongOperation();

        try {
            String wait = longOperation.execute(params).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

       */


        if(noSQLOp!=null) {
            getNextResults(noSQLOp);
        }
        else {
            createNoSQLOperation(date1, date2);
        }

        return view;
    }

    @Override
    public void onViewCreated(final View fragmentView, final Bundle savedInstanceState) {

        resultsList.setOnCreateContextMenuListener(this);


        //resultsList.setAdapter(resultsListAdapter);
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

                                FragmentContent2.this.userFileManager = userFileManager;
                                userFileManagerCreatingLatch.countDown();
                            }
                        });

        // if(noSQLOp!=null) {
        //    getNextResults(noSQLOp);
        //}

    }








    private static class MyTaskParams {
        String date1, date2;

        MyTaskParams(String date1, String date2) {
            this.date1 = date1;
            this.date2 = date2;
        }
    }


    private class LongOperation extends AsyncTask<MyTaskParams, Void, String> {

        @Override
        protected String doInBackground(MyTaskParams... params) {

            final String date1 = params[0].date1;
            final String date2 = params[0].date2;

            table.getSupportedOperations(appContext, new NoSQLTableBase.SupportedOperationsHandler() {
                @Override
                public void onSupportedOperationsReceived(final NoSQLOperation noSQLOperation) {
                    showSpinner();
                    boolean foundResults = false;
                    try {
                        foundResults = noSQLOperation.executeOperation(date1, date2);
                    } catch (final AmazonClientException ex) {
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(LOG_TAG,
                                        String.format("Failed executing selected DynamoDB table (%startDate) noSQLOperation (%startDate) : %startDate",
                                                table.getTableName(), noSQLOperation.getTitle(), ex.getMessage()), ex);
                                DynamoDBUtils.showErrorDialogForServiceException(getActivity(),
                                        getString(R.string.nosql_dialog_title_failed_operation_text), ex);
                            }
                        });
                        return;
                    } finally {
                        dismissSpinner();
                    }

                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (noSQLOperation.isScan()) {
                                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setTitle(R.string.nosql_dialog_title_scan_warning_text);
                                dialogBuilder.setMessage(R.string.nosql_dialog_message_scan_warning_text);
                                dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
                                dialogBuilder.show();
                            }
                        }
                    });

                    if (foundResults) {
                        //showResultsForOperation(noSQLOperation);
                        noSQLOp = noSQLOperation;
                    }
                }
            });
            return "executed";
        }




        @Override
        protected void onPostExecute(String result) {

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }













    public void createNoSQLOperation(final String startDate, final String endDate) {
        table.getSupportedOperations(appContext, new NoSQLTableBase.SupportedOperationsHandler() {
            @Override
            public void onSupportedOperationsReceived(final NoSQLOperation noSQLOperation) {

                showSpinner();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean foundResults = false;
                        try {
                            foundResults = noSQLOperation.executeOperation(startDate, endDate);
                        } catch (final AmazonClientException ex) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(LOG_TAG,
                                            String.format("Failed executing selected DynamoDB table (%startDate) noSQLOperation (%startDate) : %startDate",
                                                    table.getTableName(), noSQLOperation.getTitle(), ex.getMessage()), ex);
                                    DynamoDBUtils.showErrorDialogForServiceException(getActivity(),
                                            getString(R.string.nosql_dialog_title_failed_operation_text), ex);
                                }
                            });
                            return;
                        } finally {
                            dismissSpinner();
                        }

                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (noSQLOperation.isScan()) {
                                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                    dialogBuilder.setTitle(R.string.nosql_dialog_title_scan_warning_text);
                                    dialogBuilder.setMessage(R.string.nosql_dialog_message_scan_warning_text);
                                    dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
                                    dialogBuilder.show();
                                }
                            }
                        });

                        if (foundResults) {
                            showResultsForOperation(noSQLOperation);
                        }
                    }
                }).start();
            }
        });
    }



    private void showResultsForOperation(final NoSQLOperation noSQLOperation) {

        // Reset the results in case of screen rotation.
        noSQLOperation.resetResults();

        // Needs to run on the UI thread to access since we are accessing a UI element
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {


                Log.e("show results for op:", resultsList.toString());
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
                            getNextResults(noSQLOperation);
                        }
                    }
                });

                noSQLOp = noSQLOperation;
                getNextResults(noSQLOperation);
            }
        });

    }

    private class SpinnerRunner implements Runnable {
        /** A Handler for showing a spinner if service call latency becomes too long. */
        private Handler spinnerHandler;
        private volatile boolean isCanceled = false;
        private volatile ProgressDialog progressDialog = null;

        private SpinnerRunner() {
            spinnerHandler = new Handler();
        }

        @Override
        public synchronized void run() {
            if (isCanceled) {
                return;
            }
            final FragmentActivity activity = getActivity();
            if (activity != null) {
                progressDialog = ProgressDialog.show(activity,
                        getString(R.string.nosql_dialog_title_pending_results_text),
                        getString(R.string.nosql_dialog_message_pending_results_text));
            }
        }

        private void schedule() {
            isCanceled = false;
            // Post delayed runnable so that the spinner will be shown if the delay
            // expires and results haven't come back.
            spinnerHandler.postDelayed(this, SPINNER_DELAY_MS);
        }

        private synchronized void cancelOrDismiss() {
            isCanceled = true;
            // Cancel showing the spinner if it hasn't been shown yet.
            spinnerHandler.removeCallbacks(this);

            if (progressDialog != null) {
                // if the spinner has been shown, dismiss it.
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    private void showSpinner() {

        spinnerRunner.schedule();
    }

    private void dismissSpinner() {
        spinnerRunner.cancelOrDismiss();
    }




    private void getNextResults(final NoSQLOperation operation) {
        // if there are more results to retrieve.
        if (!doneRetrievingResults) {
            doneRetrievingResults = true;
            // Get next results group in the background.
            singleThreadedExecutor.execute(new Runnable() {
                List<NoSQLResult> results = null;
                @Override
                public void run() {
                    try {
                        results = operation.getNextResultGroup();
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
                            resultsListAdapter.clear();
                            resultsListAdapter.addAll(results);
                            resultsListAdapter.notifyDataSetChanged();

                            Log.e("count::::::", String.valueOf(resultsList.getCount()));
                            Log.e("count of adaptor::::::", String.valueOf(resultsListAdapter.getCount()));
                        }
                    });
                }
            });
        }
    }

    /*--------------------------------------CONTEXT MENU------------------------------------------*/

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View view,
                                    final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.add(0, R.id.nosql_context_menu_entry_view_image, 0,
                R.string.nosql_context_menu_entry_view_item_text);
        menu.add(0, R.id.nosql_context_menu_entry_update, 0,
                R.string.nosql_context_menu_entry_update_item_text);
        menu.add(0, R.id.nosql_context_menu_entry_delete, 0,
                R.string.nosql_context_menu_entry_delete_item_text);

        menu.setHeaderTitle(R.string.nosql_context_menu_title_for_results_text);
    }



    void promptToDeleteItemAt(int position) {
        final NoSQLResultListAdapter listAdapter =
                (NoSQLResultListAdapter) resultsList.getAdapter();
        final NoSQLResult result = listAdapter.getItem(position);
        final String file = result.getFilePath();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.nosql_dialog_title_confirm_delete_item_text)
                .setNegativeButton(android.R.string.cancel, null);
        builder.setMessage(R.string.nosql_dialog_message_confirm_delete_item_text);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // deletes the item from the DB
                            result.deleteItem();
                            // deletes the file accociated with the DB entry
                            deleteFile(file);

                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listAdapter.remove(result);
                                    listAdapter.notifyDataSetChanged();
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


    private void deleteFile(String file) {
        //final ProgressDialog dialog = getProgressDialog(
        //        R.string.user_files_browser_progress_dialog_message_delete_item, file);

        // Directory where all saved receipts are stored
        String fileDir = "/data/user/0/com.finalyearproject.dan.ocraccountingapp/files/" +
                "s3_ocraccountingapp-userfiles-mobilehub-1024067420/private/" +
                "eu-west-1:020b39f2-0c66-4758-becc-d68957f19f07/content/";
        final String filepath = fileDir + file;
        userFileManager.deleteRemoteContent(file, new UserFileManager.ResponseHandler() {
            @Override
            public void onSuccess() {

                userFileManager.removeLocal(filepath);
                Toast.makeText(getContext(), "Delete successful", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(final AmazonClientException ex) {
                //dialog.dismiss();
                showError(R.string.user_files_browser_error_message_delete_item, ex.getMessage());
            }
        });
    }

    private ProgressDialog getProgressDialog(final int resId, Object... args) {
        return ProgressDialog.show(getActivity(),
                getString(R.string.content_progress_dialog_title_wait),
                getString(resId, (Object[]) args));
    }


    void promptToUpdateItemAt(int position) {
        final NoSQLResultListAdapter listAdapter = (NoSQLResultListAdapter) resultsList.getAdapter();
        final NoSQLResult result = listAdapter.getItem(position);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.nosql_dialog_title_confirm_update_item_text)
                .setNegativeButton(android.R.string.cancel, null);
        builder.setMessage(R.string.nosql_dialog_message_confirm_update_item_text);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            result.updateItem();

                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listAdapter.notifyDataSetChanged();
                                }
                            });
                        } catch (final AmazonClientException ex) {
                            Log.e(LOG_TAG, "Failed saving updated item.", ex);
                            DynamoDBUtils.showErrorDialogForServiceException(getActivity(),
                                    getString(R.string.nosql_dialog_title_failed_update_item_text), ex);
                        }
                    }
                }).start();
            }
        });
        builder.show();
    }



    void viewItem(int position) {
        //final NoSQLResultListAdapter listAdapter = (NoSQLResultListAdapter) resultsList.getAdapter();
        int count = resultsList.getCount();
        Log.e("COUNT: ", String.valueOf(count));
        final NoSQLResult result = resultsListAdapter.getItem(position);
        final String file = result.getFilePath();
        Log.e("FilePath: ", file);

        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(R.string.content_progress_dialog_title_wait);
        dialog.setMessage(
                getString(R.string.progress_dialog_message_fetch_file));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax((int) new File(file).length());
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
                showError(R.string.error_message_download_file, ex.getMessage());
            }
        });
    }

    private void showError(final int resId, Object... args) {
        new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(getString(resId, (Object[]) args))
                .setPositiveButton(android.R.string.ok, null)
                .show();
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
            // Download the content.
            viewItem(info.position);
            return true;
        }
        return false;
    }


}