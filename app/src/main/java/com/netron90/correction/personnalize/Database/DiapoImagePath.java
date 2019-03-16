package com.netron90.correction.personnalize.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by CHRISTIAN on 16/03/2019.
 */

@Entity(tableName = "diapo_image_path", foreignKeys = @ForeignKey(entity = DiapositiveFormat.class, parentColumns = "id", childColumns = "id_path"))
public class DiapoImagePath {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "id_path")
    public int idPath;

    @ColumnInfo(name = "image_path")
    public String imagePath;

}
