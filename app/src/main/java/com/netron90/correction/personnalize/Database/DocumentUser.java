package com.netron90.correction.personnalize.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by CHRISTIAN on 17/02/2019.
 */

@Entity(tableName = "document_user")
public class DocumentUser implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "document_info")
    public String documentName;

    @ColumnInfo(name = "page_number")
    public int pageNumber;

    @ColumnInfo(name = "lasr_name_user")
    public String lastNameUser;

    @ColumnInfo(name = "first_name_user")
    public String firstNameUSer;

    @ColumnInfo(name = "email_user")
    public String emailUser;

    @ColumnInfo(name = "phone_user")
    public String phoneUser;

    @ColumnInfo(name = "document_path")
    public String documentPath;

    @ColumnInfo(name = "power_point")
    public boolean powerPoint;

    @ColumnInfo(name = "mise_en_forme")
    public boolean miseEnForme;

    @ColumnInfo(name = "delivery_date")
    public String deliveryDate;

    public DocumentUser() {
    }


    protected DocumentUser(Parcel in) {
        id = in.readInt();
        documentName = in.readString();
        pageNumber = in.readInt();
        lastNameUser = in.readString();
        firstNameUSer = in.readString();
        emailUser = in.readString();
        phoneUser = in.readString();
        documentPath = in.readString();
        powerPoint = in.readByte() != 0;
        miseEnForme = in.readByte() != 0;
        deliveryDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(documentName);
        dest.writeInt(pageNumber);
        dest.writeString(lastNameUser);
        dest.writeString(firstNameUSer);
        dest.writeString(emailUser);
        dest.writeString(phoneUser);
        dest.writeString(documentPath);
        dest.writeByte((byte) (powerPoint ? 1 : 0));
        dest.writeByte((byte) (miseEnForme ? 1 : 0));
        dest.writeString(deliveryDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DocumentUser> CREATOR = new Creator<DocumentUser>() {
        @Override
        public DocumentUser createFromParcel(Parcel in) {
            return new DocumentUser(in);
        }

        @Override
        public DocumentUser[] newArray(int size) {
            return new DocumentUser[size];
        }
    };
}
