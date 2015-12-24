/*
 *  Example 10:  Activities
 */

package com.graphics.openglgui;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

public class MeshViewActivity extends Activity {
  GLView view;

  @Override protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.activity_fullscreen);
    view = (GLView)findViewById(R.id.gl_view);
  }

  @Override protected void onPause() {
    super.onPause();
    view.onPause();
  }

  @Override protected void onResume() {
    super.onResume();
    view.onResume();
  }

  @Override public void onConfigurationChanged(Configuration conf) {
    super.onConfigurationChanged(conf);
  }

  public void Reset(View v) {
    view.Reset();
  }
}
