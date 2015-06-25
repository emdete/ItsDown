package org.pyneo.android.gui;

import java.net.MalformedURLException;
import java.net.URL;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.AndroidPreferences;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;
import org.mapsforge.core.model.Tile;

public class Map extends Base {
	static final private String TAG = Sample.TAG;
	static final private boolean DEBUG = Sample.DEBUG;
	protected MapView mapView;
	protected TileDownloadLayer tileLayer;
	protected TileCache tileCache;

	public void inform(int event, Bundle extra) {
		if (DEBUG) { Log.d(TAG, "Map.inform event=" + event); }
	}

	@Override public void onAttach(Activity activity) {
		if (DEBUG) { Log.d(TAG, "Map.onAttach"); }
		super.onAttach(activity);
	}

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) { Log.d(TAG, "Map.onCreate"); }
		super.onCreate(bundle);
		AndroidGraphicFactory.createInstance(getActivity().getApplication());
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) { Log.d(TAG, "Map.onCreateView"); }
		mapView = new MapView(getActivity());
		//mapView.getModel().init(preferencesFacade);
		mapView.setClickable(true);
		mapView.getMapScaleBar().setVisible(true);
		mapView.setBuiltInZoomControls(true);
		mapView.getMapZoomControls().setShowMapZoomControls(true);
		mapView.getMapZoomControls().setZoomLevelMin((byte)2);
		mapView.getMapZoomControls().setZoomLevelMax((byte)18);
		mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		tileCache = AndroidUtil.createTileCache(getActivity(), "mapcache", mapView.getModel().displayModel.getTileSize(),
			1f, mapView.getModel().frameBufferModel.getOverdrawFactor());
		OnlineTileSource onlineTileSource = new OnlineTileSource(new String[]{"otile1.mqcdn.com", "otile2.mqcdn.com", "otile3.mqcdn.com", "otile4.mqcdn.com"}, 80){
			@Override public URL getTileUrl(Tile tile) throws MalformedURLException {
				URL url = super.getTileUrl(tile);
				Log.d(TAG, "getTileUrl url=" + url);
				return url;
			}
		};
		onlineTileSource
			.setAlpha(false)
			.setBaseUrl("/tiles/1.0.0/map/")
			.setExtension("png")
			.setName("MapQuest")
			.setParallelRequestsLimit(8)
			.setProtocol("http")
			.setTileSize(256)
			.setZoomLevelMax((byte) 18)
			.setZoomLevelMin((byte) 2)
			;
		tileLayer = new TileDownloadLayer(tileCache, mapView.getModel().mapViewPosition, onlineTileSource, AndroidGraphicFactory.INSTANCE);
		mapView.getLayerManager().getLayers().add(tileLayer);
		mapView.getModel().mapViewPosition.setZoomLevel((byte)12);
		mapView.getModel().mapViewPosition.setCenter(new LatLong(52.517037, 13.38886));
		return mapView;
	}

	@Override public void onResume() {
		super.onResume();
		tileLayer.onResume();
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) { Log.d(TAG, "Map.onActivityCreated"); }
	}

	void cleanup() {
		tileLayer.onPause();
		tileCache.destroy();
		mapView.getModel().mapViewPosition.destroy();
		mapView.destroy();
		AndroidGraphicFactory.clearResourceMemoryCache();
	}
}