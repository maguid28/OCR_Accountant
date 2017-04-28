package com.finalyearproject.dan.ocraccountingapp.nosql.nosql;

import android.content.Context;

public abstract class NoSQLTableBase {

    /**
     * @return the name of the table.
     */
    public abstract String getTableName();

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
