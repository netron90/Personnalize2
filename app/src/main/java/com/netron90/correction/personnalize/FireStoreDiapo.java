package com.netron90.correction.personnalize;

import android.net.Uri;

import com.google.android.gms.tasks.Task;

import java.util.List;

/**
 * Created by CHRISTIAN on 19/03/2019.
 */

public class FireStoreDiapo {

    public String diapoTitle;
    public String diapoContent;
    public int nombreImage;

    public FireStoreDiapo() {
    }

    public FireStoreDiapo(String diapoTitle, String diapoContent, int nombreImage) {
        this.diapoTitle = diapoTitle;
        this.diapoContent = diapoContent;
        this.nombreImage = nombreImage;
    }

    public String getDiapoTitle() {
        return diapoTitle;
    }
    public String getDiapoContent() {
        return diapoContent;
    }

    public int getNombreImage() {
        return nombreImage;
    }
}
