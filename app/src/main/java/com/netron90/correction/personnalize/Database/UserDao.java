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
    @Query("DELETE FROM document_user WHERE document_info = :doc_name")
    public void deleteOneDocument(String doc_name);

//    //DELETE SPECIFIC ELEMENT INTO DATABASE
//    @Query("DELETE FROM document_user WHERE id = :id_document")
//    public void deleteOneDocumentWithId(int id_document);

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
    @Query("SELECT * FROM diapositive_format WHERE id = :id_diapo")
    public DiapositiveFormat selectDiapo(int id_diapo);

    //Select all diapo format with idDocument == id
    @Query("DELETE FROM diapositive_format WHERE id_document = :id_document")
    public void deleteAllDiapos(int id_document);

    //Update diapositive title field
    @Query("UPDATE diapositive_format SET diapo_title = :diapo_title WHERE id= :id")
    public void updateDiapoTitle(String diapo_title, int id);

    //Update diapositive content field
    @Query("UPDATE diapositive_format SET diapo_description = :diapo_description WHERE id= :id")
    public void updateDiapoDesc(String diapo_description, int id);

    //Update diapositive content field
    @Query("UPDATE diapositive_format SET nombre_image = :nbr_image WHERE id= :id")
    public void updateDiapoImage(int nbr_image, int id);

    //Reset document ID
    @Query("UPDATE document_user SET delivery_date = :delivery_date WHERE id = :id")
    public void updateDocumentDate(String delivery_date, int id);

    //Delete diapo where id == @Id
    @Query("DELETE FROM diapositive_format WHERE id = :id")
    public void deleteOneDiapo(int id);

    //Insert DiapoImagePath Object
    @Insert
    public void insertDiapoImagePath(DiapoImagePath diapoImagePath);

    //Select all DiapoImagePath Object
    @Query("SELECT * FROM diapo_image_path WHERE id_path = :id_path")
    public List<DiapoImagePath> selectDiapoImagePath(int id_path);

    //Delete all DiapoImagePath Object
    @Query("DELETE FROM diapo_image_path WHERE id_path = :id_path")
    public void deleteDiapoImagePath(int id_path);

    @Insert
    public void insertNewDocAvailable(DocumentAvailable documentAvailable);

    @Query("SELECT * FROM document_available")
    public List<DocumentAvailable> selectListDocAvailable ();
}
