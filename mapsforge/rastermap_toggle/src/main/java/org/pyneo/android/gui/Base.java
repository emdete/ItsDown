package org.pyneo.android.gui;

import android.app.Fragment;
import android.os.Bundle;

abstract public class Base extends Fragment {
	abstract public void inform(int event, Bundle extra);
}
