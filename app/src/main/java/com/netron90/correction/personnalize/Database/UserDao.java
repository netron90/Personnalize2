package com.netron90.correction.personnalize.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by CHRISTIAN on 17/02/2019.
 */

@Dao
public interface UserDao {

    //Insert one document
    @Insert()
    public void insertDocument(DocumentUser childrenList);

    @Query("SELECT * FROM document_user")
    public List<DocumentUser> selectAllDocument();

    @Query("SELECT * FROM document_user WHERE id = :position")
    public DocumentUser selectDocument(int position);

    //DELETE SPECIFIC ELEMENT INTO DATABASE
    @Query("DELETE FROM document_user WHERE document_info = :name")
    public void deleteOneDocument(String name);

    //Update power point
    @Query("UPDATE document_user SET power_point = :power_point_value WHERE id= :id")
    public void updatePowerPoint(Boolean power_point_value, int id);

    //Update mise en forme
    @Query("UPDATE document_user SET mise_en_forme = :mise_en_forme WHERE id= :id")
    public void updateMiseEnForme(Boolean mise_en_forme, int id);

    //Insert diapo format
    @Insert
    public void insertDiapo(DiapositiveFormat diapositiveFormat);

    //Select one diapo format with idDocument == id
    @Query("SELECT * FROM diapositive_format WHERE id_document = :id_document")
    public List<DiapositiveFormat> selectDiapos(int id_document);

    //Select all diapo format with idDocument == id
    @Query("SELECT * FROM diapositive_format WHERE diapo_title = :diapo_name")
    public DiapositiveFormat selectDiapo(String diapo_name);

    //Select all diapo format with idDocument == id
    @Query("DELETE FROM diapositive_format")
    public void deleteAllDiapos();

    //Update diapositive title field
    @Query("UPDATE diapositive_format SET diapo_title = :diapo_title WHERE id= :id")
    public void updateDiapoTitle(String diapo_title, int id);

    //Update diapositive content field
    @Query("UPDATE diapositive_format SET diapo_description = :diapo_description WHERE id= :id")
    public void updateDiapoDesc(String diapo_description, int id);

    //Update diapositive content field
    @Query("UPDATE diapositive_format SET nombre_image = :nbr_image WHERE id= :id")
    public void updateDiapoImage(int nbr_image, int id);
}