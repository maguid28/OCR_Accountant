package com.finalyearproject.dan.ocraccountingapp.testing.demo.nosql;

import android.content.Context;

import com.finalyearproject.dan.ocraccountingapp.mobile.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.mobile.util.ThreadUtils;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.finalyearproject.dan.ocraccountingapp.nosql.ReceiptDataDO;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.finalyearproject.dan.ocraccountingapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NoSQLTableReceiptData extends NoSQLTableBase {
    private static final String LOG_TAG = NoSQLTableReceiptData.class.getSimpleName();

    /** Inner classes use this value to determine how many results to retrieve per service call. */
    private static final int RESULTS_PER_RESULT_GROUP = 40;




    public class QueryWithPartitionKeyAndFilter extends NoSQLOperationBase {

        private PaginatedQueryList<ReceiptDataDO> results;
        private Iterator<ReceiptDataDO> resultsIterator;

        QueryWithPartitionKeyAndFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_query_by_partition_and_filter_text),
                    String.format(context.getString(R.string.nosql_operation_example_query_by_partition_and_filter_text),
                            "userId", AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID(),
                            "date", "13-02-2017","date", "19-02-2017"));
        }

        @Override
        public boolean executeOperation(String day1, String day7) {
            final ReceiptDataDO itemToFind = new ReceiptDataDO();
            itemToFind.setUserId(AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID());

            // Use an expression names Map to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#date", "formattedDate");

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            //filterExpressionAttributeValues.put(":Mindate", new AttributeValue().withS(minDate));
            //filterExpressionAttributeValues.put(":Maxdate", new AttributeValue().withS(maxDate));

            filterExpressionAttributeValues.put(":day1", new AttributeValue().withS(day1));
            filterExpressionAttributeValues.put(":day7", new AttributeValue().withS(day7));

            final DynamoDBQueryExpression<ReceiptDataDO> queryExpression = new DynamoDBQueryExpression<ReceiptDataDO>()
                    .withHashKeyValues(itemToFind)
                    .withFilterExpression("#date >= :day1 AND #date <= :day7")
                    .withExpressionAttributeNames(filterExpressionAttributeNames)
                    .withExpressionAttributeValues(filterExpressionAttributeValues)
                    .withConsistentRead(false)
                    .withLimit(RESULTS_PER_RESULT_GROUP);

            results = mapper.query(ReceiptDataDO.class, queryExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<NoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    /**
     * Helper Method to handle retrieving the next group of query results.
     * @param resultsIterator the iterator for all the results (makes a new service call for each result group).
     * @return the next list of results.
     */
    private static List<NoSQLResult> getNextResultsGroupFromIterator(final Iterator<ReceiptDataDO> resultsIterator) {
        if (!resultsIterator.hasNext()) {
            return null;
        }
        List<NoSQLResult> resultGroup = new LinkedList<>();
        int itemsRetrieved = 0;
        do {
            // Retrieve the item from the paginated results.
            final ReceiptDataDO item = resultsIterator.next();
            // Add the item to a group of results that will be displayed later.
            resultGroup.add(new NoSQLReceiptDataResult(item));
            itemsRetrieved++;
        } while ((itemsRetrieved < RESULTS_PER_RESULT_GROUP) && resultsIterator.hasNext());
        return resultGroup;
    }

    /** The DynamoDB object mapper for accessing DynamoDB. */
    private final DynamoDBMapper mapper;

    public NoSQLTableReceiptData() {
        mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
    }

    @Override
    public String getTableName() {
        return "receiptData";
    }

    @Override
    public String getPartitionKeyName() {
        return "Artist";
    }

    public String getPartitionKeyType() {
        return "String";
    }

    @Override
    public String getSortKeyName() {
        return "recName";
    }

    public String getSortKeyType() {
        return "String";
    }

    @Override
    public int getNumIndexes() {
        return 0;
    }

    private List<DemoNoSQLOperationListItem> getSupportedDemoOperations(final Context context) {
        List<DemoNoSQLOperationListItem> noSQLOperationsList = new ArrayList<DemoNoSQLOperationListItem>();

        noSQLOperationsList.add(new DemoNoSQLOperationListHeader(
                context.getString(R.string.nosql_operation_header_primary_queries)));
        noSQLOperationsList.add(new QueryWithPartitionKeyAndFilter(context));

        return noSQLOperationsList;
    }

    @Override
    public NoSQLOperation getSupportedOperation(final Context context) {
        NoSQLOperation noSQLOperation = new QueryWithPartitionKeyAndFilter(context);
        return noSQLOperation;
    }

    // modified function, changed void to NoSQLOperation
    @Override
    public NoSQLOperation getSupportedOperations(final Context context,
                                       final SupportedOperationsHandler opsHandler) {
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                final NoSQLOperation supportedOperation = getSupportedOperation(context);
                //ThreadUtils.runOnUiThread(new Runnable() {
                   // @Override
                   // public void run() {
                        opsHandler.onSupportedOperationsReceived(supportedOperation);
                    //}
                //});
            }
        }).start();
        */
        final NoSQLOperation supportedOperation = getSupportedOperation(context);
        opsHandler.onSupportedOperationsReceived(supportedOperation);
        return supportedOperation;
    }
}
