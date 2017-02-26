package com.finalyearproject.dan.ocraccountingapp.testing.demo.nosql;

import android.content.Context;

public abstract class NoSQLTableBase {

    /**
     * @return the name of the table.
     */
    public abstract String getTableName();

    /**
     * @return the primary partition key for the table.
     */
    public abstract String getPartitionKeyName();

    /**
     * @return the human readable partition key type.
     */
    public abstract String getPartitionKeyType();
    /**
     * @return the secondary partition key for the table.
     */
    public abstract String getSortKeyName();

    /**
     * @return the human readable sort key type.
     */
    public abstract String getSortKeyType();

    /**
     * @return the number of secondary indexes for the table.
     */
    public abstract int getNumIndexes();

    /**
     * Handler interface to retrieve the supported table operations.
     */
    public interface SupportedOperationsHandler {
        /**
         * @param supportedOperations the list of supported table operations.
         */
        void onSupportedOperationsReceived(NoSQLOperation supportedOperations);
    }

    /**
     * Get a list of supported demo operations.
     * @return a list of support get, query, and scan operations.
     */
    public abstract NoSQLOperation getSupportedOperations(Context context, SupportedOperationsHandler opsHandler);

    public abstract NoSQLOperation getSupportedOperation(final Context context);

}
