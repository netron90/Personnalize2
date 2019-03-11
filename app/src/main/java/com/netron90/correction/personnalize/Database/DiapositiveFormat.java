package com.netron90.correction.personnalize.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by CHRISTIAN on 10/03/2019.
 */

@Entity(tableName = "diapositive_format", foreignKeys = @ForeignKey(entity = DocumentUser.class, parentColumns = "id", childColumns = "id_document"))
public class DiapositiveFormat {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "id_document")
    public int idDocument;

    @ColumnInfo(name = "diapo_title")
    public String diapoTitle;

    @ColumnInfo(name = "diapo_description")
    public String diapoDesc;

    @ColumnInfo(name = "nombre_image")
    public int nbrImage;

}
