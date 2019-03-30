package com.netron90.correction.personnalize.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by CHRISTIAN on 23/03/2019.
 */

@Entity(tableName = "document_available")
public class DocumentAvailable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "id_server")
    public long idServer;

    @ColumnInfo(name = "document_name")
    public String documentName;

    @ColumnInfo(name = "document_number_page")
    public long pageNumber;

    @ColumnInfo(name = "document_username")
    public String nameUser;

    @ColumnInfo(name = "document_user_email")
    public String emailUser;

    @ColumnInfo(name = "document_user_phone")
    public String phoneUser;

    @ColumnInfo(name = "document_path")
    public String documentPath;

    @ColumnInfo(name = "document_power_point")
    public boolean powerPoint;

    @ColumnInfo(name = "document_mise_en_forme")
    public boolean miseEnForme;

    @ColumnInfo(name = "document_delivery_date")
    public String deliveryDate;

    @ColumnInfo(name = "document_finish")
    public boolean docEnd;

    @ColumnInfo(name = "document_paid")
    public boolean documentPaid;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "team_id")
    public String teamId;

    public DocumentAvailable() {
    }
}
