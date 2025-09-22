package com.shareconnect;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Utility class for showing confirmation and error dialogs with proper localization
 */
public class DialogUtils {
    
    /**
     * Show a confirmation dialog with Yes/No options
     * @param context The context to use
     * @param title The dialog title resource ID
     * @param message The dialog message resource ID
     * @param positiveListener The listener for positive button click
     * @param negativeListener The listener for negative button click (optional)
     */
    public static void showConfirmDialog(Context context, int title, int message, 
                                          DialogInterface.OnClickListener positiveListener,
                                          DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.yes, positiveListener);
        builder.setNegativeButton(R.string.no, negativeListener != null ? negativeListener : 
                                  new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which) {
                                          dialog.dismiss();
                                      }
                                  });
        builder.setCancelable(true);
        builder.show();
    }
    
    /**
     * Show a confirmation dialog with OK/Cancel options
     * @param context The context to use
     * @param title The dialog title resource ID
     * @param message The dialog message resource ID
     * @param positiveListener The listener for positive button click
     * @param negativeListener The listener for negative button click (optional)
     */
    public static void showOkCancelDialog(Context context, int title, int message,
                                          DialogInterface.OnClickListener positiveListener,
                                          DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, positiveListener);
        builder.setNegativeButton(R.string.cancel, negativeListener != null ? negativeListener :
                                  new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which) {
                                          dialog.dismiss();
                                      }
                                  });
        builder.setCancelable(true);
        builder.show();
    }
    
    /**
     * Show an error dialog with OK button
     * @param context The context to use
     * @param title The dialog title resource ID
     * @param message The dialog message resource ID
     */
    public static void showErrorDialog(Context context, int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(true);
        builder.show();
    }
    
    /**
     * Show an error dialog with a custom message
     * @param context The context to use
     * @param title The dialog title resource ID
     * @param message The custom error message
     */
    public static void showErrorDialog(Context context, int title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(true);
        builder.show();
    }
}