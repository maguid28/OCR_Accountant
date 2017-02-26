package com.finalyearproject.dan.ocraccountingapp.testing.demo.nosql;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import com.finalyearproject.dan.ocraccountingapp.mobile.util.ThreadUtils;
import com.finalyearproject.dan.ocraccountingapp.R;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NoSQLSelectOperationDemoFragment extends Fragment {

    private static final String LOG_TAG = NoSQLSelectOperationDemoFragment.class.getSimpleName();

    /** Bundle key for retrieving the table name from the fragment's arguments. */
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

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        appContext = getActivity().getApplicationContext();
        table = NoSQLTableFactory.instance(getContext()
                .getApplicationContext()).getNoSQLTableByTableName(tableName);
        // Inflate the layout for this fragment.
        final View fragmentView = inflater.inflate(R.layout.fragment_demo_nosql_show_results, container, false);

        return fragmentView;
    }


    @Override
    public void onViewCreated(final View fragmentView, final Bundle savedInstanceState) {
        spinnerRunner = new SpinnerRunner();
        // get the list
        resultsList = (ListView) getActivity().findViewById(R.id.nosql_show_results_list);
        createNoSQLOperation();
        resultsList.setOnCreateContextMenuListener(this);
    }




    public void createNoSQLOperation() {
        table.getSupportedOperations(appContext, new NoSQLTableBase.SupportedOperationsHandler() {
            @Override
            public void onSupportedOperationsReceived(final NoSQLOperation noSQLOperation) {

                    showSpinner();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean foundResults = false;
                            try {
                                foundResults = noSQLOperation.executeOperation("20170227", "20170305");
                            } catch (final AmazonClientException ex) {
                                ThreadUtils.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e(LOG_TAG,
                                                String.format("Failed executing selected DynamoDB table (%s) noSQLOperation (%s) : %s",
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

                            if (!foundResults) {
                                handleNoResultsFound();
                            } else {
                                showResultsForOperation(noSQLOperation);
                            }
                        }
                    }).start();
                }
        });
    }



    private void handleNoResultsFound() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setTitle(R.string.nosql_dialog_title_no_results_text);
                dialogBuilder.setMessage(R.string.nosql_dialog_message_no_results_text);
                dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
                dialogBuilder.show();
            }
        });
    }





    private void showResultsForOperation(final NoSQLOperation noSQLOperation) {

        // Reset the results in case of screen rotation.
        noSQLOperation.resetResults();

        // Needs to run on the UI thread to access since we are accessing a UI element
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

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
                            getNextResults(noSQLOperation);
                        }
                    }
                });

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
                            resultsListAdapter.addAll(results);
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
                            result.deleteItem();

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
        return false;
    }


}
