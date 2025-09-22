package com.shareconnect

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

/**
 * Utility class for showing confirmation and error dialogs with proper localization
 */
object DialogUtils {

    /**
     * Show a confirmation dialog with Yes/No options
     * @param context The context to use
     * @param title The dialog title resource ID
     * @param message The dialog message resource ID
     * @param positiveListener The listener for positive button click
     * @param negativeListener The listener for negative button click (optional)
     */
    fun showConfirmDialog(
        context: Context, title: Int, message: Int,
        positiveListener: DialogInterface.OnClickListener,
        negativeListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.yes, positiveListener)
        builder.setNegativeButton(
            R.string.no
        ) { dialog, which -> dialog.dismiss() }
        builder.setCancelable(true)
        builder.show()
    }

    /**
     * Show a confirmation dialog with OK/Cancel options
     * @param context The context to use
     * @param title The dialog title resource ID
     * @param message The dialog message resource ID
     * @param positiveListener The listener for positive button click
     * @param negativeListener The listener for negative button click (optional)
     */
    fun showOkCancelDialog(
        context: Context, title: Int, message: Int,
        positiveListener: DialogInterface.OnClickListener,
        negativeListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok, positiveListener)
        builder.setNegativeButton(
            R.string.cancel
        ) { dialog, which -> dialog.dismiss() }
        builder.setCancelable(true)
        builder.show()
    }

    /**
     * Show an error dialog with OK button
     * @param context The context to use
     * @param title The dialog title resource ID
     * @param message The dialog message resource ID
     */
    fun showErrorDialog(context: Context, title: Int, message: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.setCancelable(true)
        builder.show()
    }

    /**
     * Show an error dialog with a custom message
     * @param context The context to use
     * @param title The dialog title resource ID
     * @param message The custom error message
     */
    fun showErrorDialog(context: Context, title: Int, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.setCancelable(true)
        builder.show()
    }
}