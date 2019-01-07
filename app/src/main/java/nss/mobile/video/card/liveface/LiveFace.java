package nss.mobile.video.card.liveface;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * The metadata for liveface
 */
public final class LiveFace implements Parcelable {

    public static final int INVALID_ID = -1;
    public static final int SCORE_MAX = 100;
    public static final int SCORE_MIN = 0;
    public static final int QUALITY_MAX = 100;
    public static final int QUALITY_MIN = 0;

    // Face id
    private long id;
    // Face rect
    private Rect bounds;
    // Score
    private int score;
    // Face quality.
    private int quality;
    // The position of left eye.
    private Point leftEyePosition;
    // The position of right eye.
    private Point rightEyePosition;
    // The position of mouth.
    private Point mouthPosition;

    //  The position for face.
    private transient Point position;
    // The width of face.
    private transient int width;
    // The height of face.
    private transient int height;

    public LiveFace() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Rect getBounds() {
        return bounds;
    }

    public void setBounds(Rect bounds) {
        this.bounds = bounds;
        if (this.bounds != null) {
            this.width = this.bounds.width();
            this.height = this.bounds.height();

            this.position = new Point(this.bounds.left, this.bounds.top);
        } else {
            this.width = 0;
            this.height = 0;

            this.position = new Point();
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public Point getLeftEyePosition() {
        return leftEyePosition;
    }

    public void setLeftEyePosition(Point leftEyePosition) {
        this.leftEyePosition = leftEyePosition;
    }

    public Point getRightEyePosition() {
        return rightEyePosition;
    }

    public void setRightEyePosition(Point rightEyePosition) {
        this.rightEyePosition = rightEyePosition;
    }

    public Point getMouthPosition() {
        return mouthPosition;
    }

    public void setMouthPosition(Point mouthPosition) {
        this.mouthPosition = mouthPosition;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isEmpty() {
        if (this.width <= 0 || this.height <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "LiveFace{" +
                "id=" + id +
                ", bounds=" + bounds +
                ", score=" + score +
                ", quality=" + quality +
                ", leftEyePosition=" + leftEyePosition +
                ", rightEyePosition=" + rightEyePosition +
                ", mouthPosition=" + mouthPosition +
                ", position=" + position +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeParcelable(this.bounds, flags);
        dest.writeInt(this.score);
        dest.writeInt(this.quality);
        dest.writeParcelable(this.leftEyePosition, flags);
        dest.writeParcelable(this.rightEyePosition, flags);
        dest.writeParcelable(this.mouthPosition, flags);
    }

    protected void readFromParcel(Parcel in) {
        this.id = in.readLong();
        this.bounds = in.readParcelable(Rect.class.getClassLoader());
        this.score = in.readInt();
        this.quality = in.readInt();
        this.leftEyePosition = in.readParcelable(Point.class.getClassLoader());
        this.rightEyePosition = in.readParcelable(Point.class.getClassLoader());
        this.mouthPosition = in.readParcelable(Point.class.getClassLoader());
    }

    protected LiveFace(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<LiveFace> CREATOR = new Creator<LiveFace>() {
        @Override
        public LiveFace createFromParcel(Parcel source) {
            return new LiveFace(source);
        }

        @Override
        public LiveFace[] newArray(int size) {
            return new LiveFace[size];
        }
    };
}
