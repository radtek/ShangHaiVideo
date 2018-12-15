/*
 *  Copyright 2016 Jeroen Mols
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package nss.mobile.video.video;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nss.mobile.video.MyApp;
import nss.mobile.video.utils.FileMeoryUtils;

public class VideoFile {

    private static final String DIRECTORY_SEPARATOR = "/";
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";
    public static String DEFAULT_PREFIX = "ID_";
    private static final String DEFAULT_EXTENSION = ".mp4";

    private final String mFilename;
    private Date mDate;

    public VideoFile(String filename) {
        this.mFilename = filename;
    }


    public VideoFile(String filename, Date date) {
        this(filename);
        this.mDate = date;
    }

    public String getFullPath() {
        return getFile().getAbsolutePath();
    }

    public File getFile() {

        File[] externalFilesDirs = MyApp.getInstance().getExternalFilesDirs("");
        File s = null;
        if (externalFilesDirs.length >= 2) {
            s = externalFilesDirs[1];
            return new File(s, generateFilename());
        }

        final String filename = generateFilename();
        if (filename.contains(DIRECTORY_SEPARATOR)) return new File(filename);
        File sdPath0 = FileMeoryUtils.getSDPath();

        File dir = new File(sdPath0, Environment.DIRECTORY_MOVIES);
        dir.mkdirs();

        return new File(dir, generateFilename());


    }

    private String generateFilename() {
        if (isValidFilename()) return mFilename;

        final String dateStamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(getDate());
        return DEFAULT_PREFIX + dateStamp + DEFAULT_EXTENSION;
    }

    private boolean isValidFilename() {
        if (mFilename == null) return false;
        if (mFilename.isEmpty()) return false;

        return true;
    }

    public static File baseFile() {
        File[] externalFilesDir = MyApp.getInstance().getExternalFilesDirs("");
        if (externalFilesDir.length > 1)
            return externalFilesDir[1];
        File sdPath = FileMeoryUtils.getSDPath();
        if (sdPath == null) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        }
        return new File(sdPath, Environment.DIRECTORY_MOVIES);
    }

    private Date getDate() {
        if (mDate == null) {
            mDate = new Date();
        }
        return mDate;
    }
}
