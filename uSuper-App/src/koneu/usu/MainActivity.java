package koneu.usu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import android.app.ListActivity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends ListActivity {
	final private static int HIDE_SYSTEM = 3;
	final private static int PURGE_PERMISSIONS = 4;
	final private static int REFRESH_LIST = 1;
	final private static int SHOW_SYSTEM = 2;

	ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

	ArrayList<Object> permList = new ArrayList<Object>();

	private SimpleAdapter adapter;
	private boolean permChange = false;
	private boolean sysExclude = true;

	final static private boolean isSystemPackage(ApplicationInfo appInfo) {
		return ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
				: false;
	}

	@Override
	final public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, REFRESH_LIST, Menu.NONE, R.string.refresh_list);
		menu.add(0, SHOW_SYSTEM, Menu.NONE, R.string.show_system);
		menu.add(0, HIDE_SYSTEM, Menu.NONE, R.string.hide_system);
		menu.add(0, PURGE_PERMISSIONS, Menu.NONE, R.string.purge_permissions);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	final public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case SHOW_SYSTEM:
			sysExclude = false;
			refreshList();
			break;
		case HIDE_SYSTEM:
			sysExclude = true;
			refreshList();
			break;
		case PURGE_PERMISSIONS:
			permList.clear();
			permChange = true;
		case REFRESH_LIST:
			refreshList();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	final void addApp(CharSequence label, int userID) {
		HashMap<String, Object> appLabels = new HashMap<String, Object>();
		appLabels.put("AppLabel", label);
		appLabels.put("UID", userID);
		if (permList.contains(userID)) {
			appLabels.put("enablabel", "ENABLED");
		} else {
			appLabels.put("disablabel", "DISABLED");
		}
		list.add(appLabels);
	}

	final void refreshList() {
		PackageManager packageManager = getPackageManager();
		list.clear();
		if (!sysExclude) {
			addApp("Android Debugging Bridge", 2000);
		}
		for (ApplicationInfo appInfo : packageManager
				.getInstalledApplications(PackageManager.GET_META_DATA)) {
			if (!isSystemPackage(appInfo) || !sysExclude) {
				addApp(appInfo.loadLabel(packageManager), appInfo.uid);
			}
		}
		adapter.notifyDataSetChanged();
	}

	final void refreshPermList() {
		permList.clear();
		try {
			File permFile = new File(getFilesDir(), "permissions");
			permFile.createNewFile();
			Scanner permScanner = new Scanner(permFile);
			permScanner.useDelimiter("\n");
			while (permScanner.hasNext()) {
				permList.add(permScanner.nextInt());
			}
		} catch (IOException e) {
		}
	}

	final void writePermFile() {
		if (permChange) {
			try {
				File permFile = new File(getFilesDir(), "permissions");
				permFile.delete();
				OutputStreamWriter osw = new OutputStreamWriter(
						new FileOutputStream(permFile));
				for (Object app : permList) {
					osw.write(app.toString() + "\n");
				}
				osw.flush();
				osw.close();
				permFile.setReadable(true, false);
				permFile.setExecutable(false, false);
				permFile.setWritable(false, false);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			permChange = false;
		}
	}

	@Override
	final protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFilesDir().mkdirs();
		setContentView(R.layout.activity_main);
		adapter = new SimpleAdapter(this, list, R.layout.listitem,
				new String[] { "AppLabel", "UID", "enablabel", "disablabel" },
				new int[] { R.id.AppLabel, R.id.AppUID, R.id.enablabel,
						R.id.disablabel });
		setListAdapter(adapter);
	}

	@Override
	final protected void onListItemClick(ListView l, View v, int position,
			long id) {
		super.onListItemClick(l, v, position, id);
		HashMap<String, Object> hashMap = list.get(position);
		Object userID = hashMap.get("UID");
		if (permList.contains(userID)) {
			permList.remove(userID);
			hashMap.remove("enablabel");
			hashMap.put("disablabel", "DISABLED");
		} else {
			permList.add(userID);
			hashMap.remove("disablabel");
			hashMap.put("enablabel", "ENABLED");
		}
		permChange = true;
		list.set(position, hashMap);
		adapter.notifyDataSetChanged();
	}

	@Override
	final protected void onPause() {
		super.onPause();
		writePermFile();
	}

	@Override
	final protected void onStart() {
		super.onStart();
		refreshPermList();
		refreshList();
	}

	@Override
	final protected void onStop() {
		super.onStop();
		list.clear();
		permList.clear();
	}
}
