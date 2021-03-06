package de.emdete.android;

import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

public class TheList implements JSONStreamAware, JSONAware, Iterable<TheDictionary> {
	private static final String TAG = "de.emdete.sample";
	private static boolean DEBUG = false;
	static { DEBUG = Log.isLoggable(TAG, Log.DEBUG); }

	private java.util.AbstractList<TheDictionary> list = new JSONArray();

	public TheList() {
	}

	public TheList(JSONArray arr) {
		for (Object obj: arr) {
			this.list.add(new TheDictionary((JSONObject)obj));
		}
	}

	@Override
	public Iterator<TheDictionary> iterator() {
		return this.list.iterator();
	}

	public int size() {
		return list.size();
	}

	public boolean add(TheDictionary e) {
		return this.list.add(e);
	}

	public String toJSONString() {
		return ((JSONArray)this.list).toJSONString();
	}

	public String toString() {
		return this.list == null ? null : this.list.toString();
	}

	public void writeJSONString(Writer out) throws IOException {
		((JSONArray)this.list).writeJSONString(out);
	}
}
