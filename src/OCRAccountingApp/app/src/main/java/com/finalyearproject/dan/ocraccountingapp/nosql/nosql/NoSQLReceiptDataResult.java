package com.finalyearproject.dan.ocraccountingapp.nosql.nosql;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.finalyearproject.dan.ocraccountingapp.nosql.ReceiptDataDO;

import java.util.Set;

public class NoSQLReceiptDataResult implements NoSQLResult {
    private static final int KEY_TEXT_COLOR = 0xFF333333;
    private final ReceiptDataDO result;

    NoSQLReceiptDataResult(final ReceiptDataDO result) {
        this.result = result;
    }
    @Override
    public void updateItem(String title, String total, String date, String category) {
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

        result.setFriendlyName(title);
        result.setDate(date);
        result.setTotal(total);
        result.setCategory(category);
        try {
            mapper.save(result);
        } catch (final AmazonClientException ex) {
            // Restore original data if save fails, and re-throw.
            result.setFriendlyName(title);
            result.setDate(date);
            result.setTotal(total);
            result.setCategory(category);

            throw ex;
        }
    }

    @Override
    public void deleteItem() {
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        mapper.delete(result);
    }

    @Override
    public String getDate() {
         return result.getDate();
    }

    @Override
    public String getFormattedDate() {
        return result.getFormattedDate();
    }

    @Override
    public String getTotal() {
        return result.getTotal();
    }

    @Override
    public String getCategory() {
        return result.getCategory();
    }

    @Override
    public String getFriendlyName() {
        return result.getFriendlyName();
    }


    public String getFilePath() {
        return result.getFilepath();
    }

    private void setKeyTextViewStyle(final TextView textView) {
        textView.setTextColor(KEY_TEXT_COLOR);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(dp(5), dp(2), dp(5), 0);
        textView.setLayoutParams(layoutParams);
    }

    /**
     * @param dp number of design pixels.
     * @return number of pixels corresponding to the desired design pixels.
     */
    private int dp(int dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    private void setValueTextViewStyle(final TextView textView) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(dp(15), 0, dp(15), dp(20));
        textView.setLayoutParams(layoutParams);
    }

    private void setKeyAndValueTextViewStyles(final TextView keyTextView, final TextView valueTextView) {
        setKeyTextViewStyle(keyTextView);
        setValueTextViewStyle(valueTextView);
    }

    private static String bytesToHexString(byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("%02X", bytes[0]));
        for(int index = 1; index < bytes.length; index++) {
            builder.append(String.format(" %02X", bytes[index]));
        }
        return builder.toString();
    }

    private static String byteSetsToHexStrings(Set<byte[]> bytesSet) {
        final StringBuilder builder = new StringBuilder();
        int index = 0;
        for (byte[] bytes : bytesSet) {
            builder.append(String.format("%d: ", ++index));
            builder.append(bytesToHexString(bytes));
            if (index < bytesSet.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public View getView(final Context context, final View convertView, int position) {
        final LinearLayout layout;
        final TextView resultNumberTextView;
        final TextView dateKeyTextView;
        final TextView dateValueTextView;
        final TextView totalKeyTextView;
        final TextView totalValueTextView;
        final TextView friendlyNameKeyTextView;
        final TextView friendlyNameValueTextView;
        final TextView categoryKeyTextView;
        final TextView categoryValueTextView;

        if (convertView == null) {
            layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            resultNumberTextView = new TextView(context);
            resultNumberTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(resultNumberTextView);
            resultNumberTextView.setTextColor(Color.WHITE);

            friendlyNameKeyTextView = new TextView(context);
            friendlyNameValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(friendlyNameKeyTextView, friendlyNameValueTextView);
            layout.addView(friendlyNameKeyTextView);
            layout.addView(friendlyNameValueTextView);
            friendlyNameKeyTextView.setTextColor(Color.parseColor("#BFEAE1"));
            friendlyNameValueTextView.setTextSize(15);
            friendlyNameValueTextView.setTextColor(Color.WHITE);

            dateKeyTextView = new TextView(context);
            dateValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(dateKeyTextView, dateValueTextView);
            layout.addView(dateKeyTextView);
            layout.addView(dateValueTextView);
            dateKeyTextView.setTextColor(Color.parseColor("#BFEAE1"));
            dateValueTextView.setTextSize(15);
            dateValueTextView.setTextColor(Color.WHITE);

            categoryKeyTextView = new TextView(context);
            categoryValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(categoryKeyTextView, categoryValueTextView);
            layout.addView(categoryKeyTextView);
            layout.addView(categoryValueTextView);
            categoryKeyTextView.setTextColor(Color.parseColor("#BFEAE1"));
            categoryValueTextView.setTextSize(15);
            categoryValueTextView.setTextColor(Color.WHITE);

            totalKeyTextView = new TextView(context);
            totalValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(totalKeyTextView, totalValueTextView);
            layout.addView(totalKeyTextView);
            layout.addView(totalValueTextView);
            totalKeyTextView.setTextColor(Color.parseColor("#BFEAE1"));
            totalValueTextView.setTextSize(15);
            totalValueTextView.setTextColor(Color.WHITE);


        } else {
            layout = (LinearLayout) convertView;
            resultNumberTextView = (TextView) layout.getChildAt(0);

            friendlyNameKeyTextView = (TextView) layout.getChildAt(1);
            friendlyNameValueTextView = (TextView) layout.getChildAt(2);

            dateKeyTextView = (TextView) layout.getChildAt(3);
            dateValueTextView = (TextView) layout.getChildAt(4);

            categoryKeyTextView = (TextView) layout.getChildAt(5);
            categoryValueTextView = (TextView) layout.getChildAt(6);

            totalKeyTextView = (TextView) layout.getChildAt(7);
            totalValueTextView = (TextView) layout.getChildAt(8);


        }

        resultNumberTextView.setText(String.format("#%d", + position+1));
        dateKeyTextView.setText("Date");
        dateValueTextView.setText(result.getDate());
        totalKeyTextView.setText("Total");
        totalValueTextView.setText(result.getTotal());
        friendlyNameKeyTextView.setText("Receipt Name");
        friendlyNameValueTextView.setText(result.getFriendlyName());
        categoryKeyTextView.setText("Category");
        categoryValueTextView.setText(result.getCategory());
        return layout;
    }
}
