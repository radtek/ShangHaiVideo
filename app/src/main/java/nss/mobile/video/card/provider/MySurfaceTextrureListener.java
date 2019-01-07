package nss.mobile.video.card.provider;

import android.graphics.SurfaceTexture;
import android.view.TextureView;

public class MySurfaceTextrureListener implements TextureView.SurfaceTextureListener {

    SurfaceTextureDetailListener listener;
    private int width;
    private int height;

    public MySurfaceTextrureListener(SurfaceTextureDetailListener listener){
        this.listener=listener;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.width = width;
        this.height = height;
        listener.onSurfaceTextureAvailable(surface,width,height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        listener.onSurfaceTextureDestroyed(surface, this.width, this.height);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}
