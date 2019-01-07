package nss.mobile.video.card.provider;

import android.graphics.SurfaceTexture;

public interface SurfaceTextureDetailListener {
    void onSurfaceTextureAvailable(SurfaceTexture surface,
                                   int width, int height);
    boolean onSurfaceTextureDestroyed(SurfaceTexture surface, int width, int height);
}
