/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch.util;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.TodoException;
import com.todotxt.todotxttouch.remote.RemoteConflictException;

public class Util {

	private static String TAG = Util.class.getSimpleName();

	private static final int CONNECTION_TIMEOUT = 120000;

	private static final int SOCKET_TIMEOUT = 120000;

	private Util() {
	}

	public static HttpParams getTimeoutHttpParams() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
		return params;
	}

	public static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
				stream = null;
			} catch (IOException e) {
				Log.w(TAG, "Close stream exception", e);
			}
		}
	}

	public static InputStream getInputStreamFromUrl(String url)
			throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(url);
		DefaultHttpClient client = new DefaultHttpClient(getTimeoutHttpParams());
		HttpResponse response = client.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			Log.e(TAG, "Failed to get stream for: " + url);
			throw new IOException("Failed to get stream for: " + url);
		}
		return response.getEntity().getContent();
	}

	public static String fetchContent(String url)
			throws ClientProtocolException, IOException {
		InputStream input = getInputStreamFromUrl(url);
		try {
			int c;
			byte[] buffer = new byte[8192];
			StringBuilder sb = new StringBuilder();
			while ((c = input.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, c));
			}
			return sb.toString();
		} finally {
			closeStream(input);
		}
	}

	public static String readStream(InputStream is) {
		if (is == null) {
			return null;
		}
		try {
			int c;
			byte[] buffer = new byte[8192];
			StringBuilder sb = new StringBuilder();
			while ((c = is.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, c));
			}
			return sb.toString();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			closeStream(is);
		}
		return null;
	}

	public static void writeFile(InputStream is, File file)
			throws ClientProtocolException, IOException {
		FileOutputStream os = new FileOutputStream(file);
		try {
			int c;
			byte[] buffer = new byte[8192];
			while ((c = is.read(buffer)) != -1) {
				os.write(buffer, 0, c);
			}
		} finally {
			closeStream(is);
			closeStream(os);
		}
	}

	public static void showToastLong(Context cxt, int resid) {
		Toast.makeText(cxt, resid, Toast.LENGTH_LONG).show();
	}

	public static void showToastShort(Context cxt, int resid) {
		Toast.makeText(cxt, resid, Toast.LENGTH_SHORT).show();
	}

	public static void showToastLong(Context cxt, String msg) {
		Toast.makeText(cxt, msg, Toast.LENGTH_LONG).show();
	}

	public static void showToastShort(Context cxt, String msg) {
		Toast.makeText(cxt, msg, Toast.LENGTH_SHORT).show();
	}

	public interface OnMultiChoiceDialogListener {
		void onClick(boolean[] selected);
	}

	public static Dialog createMultiChoiceDialog(Context cxt,
			CharSequence[] keys, boolean[] values, Integer titleId,
			Integer iconId, final OnMultiChoiceDialogListener listener) {
		final boolean[] res;
		if (values == null) {
			res = new boolean[keys.length];
		} else {
			res = values;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		if (iconId != null) {
			builder.setIcon(iconId);
		}
		if (titleId != null) {
			builder.setTitle(titleId);
		}
		builder.setMultiChoiceItems(keys, values,
				new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton, boolean isChecked) {
						res[whichButton] = isChecked;
					}
				});
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						listener.onClick(res);
					}
				});
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();
	}

	public static void showDialog(Context cxt, int titleid, int msgid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		builder.setTitle(titleid);
		builder.setMessage(msgid);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setCancelable(true);
		builder.show();
	}

	public static void showDialog(Context cxt, int titleid, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		builder.setTitle(titleid);
		builder.setMessage(msg);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setCancelable(true);
		builder.show();
	}

	public static void showConfirmationDialog(Context cxt, int msgid,
			OnClickListener oklistener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		// builder.setTitle(cxt.getPackageName());
		builder.setMessage(msgid);
		builder.setPositiveButton(android.R.string.ok, oklistener);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setCancelable(true);
		builder.show();
	}

	public static void showConfirmationDialog(Context cxt, int msgid,
			OnClickListener oklistener, int titleid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		// builder.setTitle(cxt.getPackageName());
		builder.setTitle(titleid);
		builder.setMessage(msgid);
		builder.setPositiveButton(android.R.string.ok, oklistener);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setCancelable(true);
		builder.show();
	}

	public static void showDeleteConfirmationDialog(Context cxt,
			OnClickListener oklistener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		builder.setTitle(R.string.delete_task_title);
		builder.setMessage(R.string.delete_task_message);
		builder.setPositiveButton(R.string.delete_task_confirm, oklistener);
		builder.setNegativeButton(R.string.cancel, null);
		builder.show();
	}

	public static boolean isDeviceWritable() {
		String sdState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(sdState)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isDeviceReadable() {
		String sdState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(sdState)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdState)) {
			return true;
		} else {
			return false;
		}
	}

	public interface InputDialogListener {
		void onClick(String input);
	}

	public static void showInputDialog(Context cxt, int titleid, int msgid,
			String defaulttext, int lines,
			final InputDialogListener oklistener, int icon) {
		LayoutInflater factory = LayoutInflater.from(cxt);
		final View textEntryView = factory.inflate(R.layout.inputdialog, null);
		final TextView textinput = (TextView) textEntryView
				.findViewById(R.id.input);
		textinput.setText(defaulttext);
		textinput.setLines(lines);
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		if (icon > 0) {
			builder.setIcon(icon);
		}
		builder.setTitle(titleid);
		builder.setMessage(msgid);
		builder.setView(textEntryView);
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String input = textinput.getText().toString();
						oklistener.onClick(input);
					}
				});
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
		builder.setCancelable(true);
		builder.show();
	}

	public interface LoginDialogListener {
		void onClick(String username, String password);
	}

	public static void createParentDirectory(File dest) throws TodoException {
		if (dest == null) {
			throw new TodoException("createParentDirectory: dest is null");
		}
		File dir = dest.getParentFile();
		if (dir != null && !dir.exists()) {
			createParentDirectory(dir);
		}
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				Log.e(TAG, "Could not create dirs: " + dir.getAbsolutePath());
				throw new TodoException("Could not create dirs: "
						+ dir.getAbsolutePath());
			}
		}
	}

	public static void renameFile(File origFile, File newFile, boolean overwrite) {
		if (!origFile.exists()) {
			Log.e(TAG, "Error renaming file: " + origFile + " does not exist");
			throw new TodoException("Error renaming file: " + origFile
					+ " does not exist");
		}

		createParentDirectory(newFile);

		if (overwrite && newFile.exists()) {
			if (!newFile.delete()) {
				Log.e(TAG, "Error renaming file: failed to delete " + newFile);
				throw new TodoException(
						"Error renaming file: failed to delete " + newFile);
			}
		}

		if (!origFile.renameTo(newFile)) {
			Log.e(TAG, "Error renaming " + origFile + " to " + newFile);
			throw new TodoException("Error renaming " + origFile + " to "
					+ newFile);
		}
	}

	public static ArrayAdapter<String> newSpinnerAdapter(Context cxt,
			List<String> items) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(cxt,
				android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	public static void setGray(SpannableString ss, List<String> items) {
		String data = ss.toString();
		for (String item : items) {
			int i = data.indexOf("@" + item);
			if (i != -1) {
				ss.setSpan(new ForegroundColorSpan(Color.GRAY), i,
						i + 1 + item.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			int j = data.indexOf("+" + item);
			if (j != -1) {
				ss.setSpan(new ForegroundColorSpan(Color.GRAY), j,
						j + 1 + item.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isConnected();
	}
	
	public static String threeWayMerge(String base, String local, String remote) {
		String merged;
		boolean mergeSucceeded = true;

		diff_match_patch dmp = new diff_match_patch();
		// TODO: Set appropriate configuration values
		dmp.Patch_DeleteThreshold = 0.3f;
		dmp.Match_Distance = 200;
		LinkedList<Diff> diffs = dmp.diff_main(base, local);
		dmp.diff_cleanupSemantic(diffs);
		LinkedList<Patch> patches = dmp.patch_make(base, diffs);
		Object[] patch_results = dmp.patch_apply(patches, remote);
		merged = (String) patch_results[0];
		boolean[] resultsArray = (boolean[]) patch_results[1];
		// If there is at least one diff, at least one must apply correctly to count as a successful patch.
		if (resultsArray.length > 0) {
			boolean patchSucceeded = false;
			for (int i = 0; i < resultsArray.length; i++) {
				if (resultsArray[i] == true) {
					patchSucceeded = true;
					break;
				}
			}
			mergeSucceeded &= patchSucceeded;
		}
		
		// One-off fix for the common scenario of marking a task complete in both files
		// This might be a little ambitious; a speedier but less accurate approach would
		// be to use simple character sequences.
		Pattern twiceCompletedPattern = Pattern.compile("^x (?:[-0-9]{1,10} )?x ", Pattern.MULTILINE);
		Matcher m = twiceCompletedPattern.matcher(merged);
		merged = m.replaceAll("x ");

		// If the merge goes ballistic and/or the files are totally different, show
		// the old conflict dialog

		// TODO
		
		// Use line count as a heuristic
		int baseLinesCount  = Util.countLinesInString(base);
		int localLinesCount  = Util.countLinesInString(local);
		int remoteLinesCount = Util.countLinesInString(remote);
		int mergedLinesCount = Util.countLinesInString(merged);

		// Throw an exception if the merged file has a bigger increase
		// than both the local and remote files combined, as compared with
		// the base file.
		mergeSucceeded &= (mergedLinesCount <= localLinesCount + remoteLinesCount - baseLinesCount);
		Log.w("merge",
				String.format("Merge warning: %d lines in merge from files: %d (base) -> %d (local) and %d (remote)",
						mergedLinesCount, baseLinesCount, localLinesCount, remoteLinesCount));

		if (!mergeSucceeded) {
			Log.v("merge", "--- Merge problem; info follows ---");
			Log.v("merge", "Base:  \n" + local );
			Log.v("merge", "Local:  \n" + local );
			Log.v("merge", "Remote: \n" + remote);
			Log.v("merge", "Diffs: \n" + diffs.toString());
			Log.v("merge", "Patches: \n" + patches.toString());
			Log.v("merge", "Merged: \n" + merged);
			Log.v("merge", "Results: \n" + Arrays.toString(resultsArray));

			throw new RemoteConflictException(
					"Merged file is too long; not enough commonality in local and remote lists.");
		}
		return merged;
	}
	
	public static String readFiletoString(File file) throws FileNotFoundException {
		InputStream is = new FileInputStream(file);
		return readStream(is);
	}

	public static void writeStringToFile(String str, File file) throws IOException {
		InputStream is = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
		writeFile(is, file);
	}
	
	public static int countLinesInString(String s) {
		int count = 1;
		int length = s.length();
		for (int i = 0; i < length; i++) {
			if (s.charAt(i) == '\n') {
				count++;
			}
		}
		return count;
	}

}
