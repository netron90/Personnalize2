package com.netron90.correction.personnalize;

/**
 * Created by CHRISTIAN on 19/03/2019.
 */

public class FirestoreUserDoc {

    public int id;

    public String documentName;

    public int pageNumber;

    public String nameUser;

    public String emailUser;

    public String phoneUser;

    public String documentPath;

    public boolean powerPoint;

    public boolean miseEnForme;

    public String deliveryDate;

    public boolean docEnd;

    public boolean documentPaid;

    public String userId;

    public String teamId;

    public FirestoreUserDoc() {
    }

    public FirestoreUserDoc(int id, String documentName, int pageNumber, String nameUser, String emailUser, String phoneUser, String documentPath, boolean powerPoint, boolean miseEnForme, String deliveryDate, boolean docEnd, boolean docPaid, String userId, String teamId) {

        this.id = id;
        this.documentName = documentName;
        this.pageNumber = pageNumber;
        this.nameUser = nameUser;
        this.emailUser = emailUser;
        this.phoneUser = phoneUser;
        this.documentPath = documentPath;
        this.powerPoint = powerPoint;
        this.miseEnForme = miseEnForme;
        this.deliveryDate = deliveryDate;
        this.docEnd = docEnd;
        this.documentPaid = docPaid;
        this.userId = userId;
        this.teamId = teamId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public String getNameUser() {
        return nameUser;
    }
    public String getEmailUser() {
        return emailUser;
    }

    public String getPhoneUser() {
        return phoneUser;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public boolean isPowerPoint() {
        return powerPoint;
    }

    public boolean isMiseEnForme() {
        return miseEnForme;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public int getId() {
        return id;
    }

    public boolean isDocEnd() {
        return docEnd;
    }

    public boolean isDocumentPaid() {
        return documentPaid;
    }

    public String getUserId() {
        return userId;
    }

    public String getTeamId() {
        return teamId;
    }
}
