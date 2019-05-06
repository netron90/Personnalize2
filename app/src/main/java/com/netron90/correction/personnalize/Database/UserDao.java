package com.netron90.correction.personnalize.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.netron90.correction.personnalize.UserMessage;

import java.util.List;

/**
 * Created by CHRISTIAN on 17/02/2019.
 */

@Dao
public interface UserDao {

    //Insert one document
    @Insert()
    void insertDocument(DocumentUser childrenList);

    @Query("SELECT * FROM document_user")
    List<DocumentUser> selectAllDocument();

    @Query("SELECT * FROM document_user WHERE id = :position")
    DocumentUser selectDocument(int position);

    //DELETE SPECIFIC ELEMENT INTO DATABASE
    @Query("DELETE FROM document_user WHERE document_info = :doc_name")
    void deleteOneDocument(String doc_name);

//    //DELETE SPECIFIC ELEMENT INTO DATABASE
//    @Query("DELETE FROM document_user WHERE id = :id_document")
//    public void deleteOneDocumentWithId(int id_document);

    //Update power point
    @Query("UPDATE document_user SET power_point = :power_point_value WHERE id= :id")
    void updatePowerPoint(Boolean power_point_value, int id);

    //Update mise en forme
    @Query("UPDATE document_user SET mise_en_forme = :mise_en_forme WHERE id= :id")
    void updateMiseEnForme(Boolean mise_en_forme, int id);

    //Insert diapo format
    @Insert
    void insertDiapo(DiapositiveFormat diapositiveFormat);

    //Select one diapo format with idDocument == id
    @Query("SELECT * FROM diapositive_format WHERE id_document = :id_document")
    List<DiapositiveFormat> selectDiapos(int id_document);

    //Select all diapo format with idDocument == id
    @Query("SELECT * FROM diapositive_format WHERE id = :id_diapo")
    DiapositiveFormat selectDiapo(int id_diapo);

    //Select all diapo format with idDocument == id
    @Query("DELETE FROM diapositive_format WHERE id_document = :id_document")
    void deleteAllDiapos(int id_document);

    //Update diapositive title field
    @Query("UPDATE diapositive_format SET diapo_title = :diapo_title WHERE id= :id")
    void updateDiapoTitle(String diapo_title, int id);

    //Update diapositive content field
    @Query("UPDATE diapositive_format SET diapo_description = :diapo_description WHERE id= :id")
    void updateDiapoDesc(String diapo_description, int id);

    //Update diapositive content field
    @Query("UPDATE diapositive_format SET nombre_image = :nbr_image WHERE id= :id")
    void updateDiapoImage(int nbr_image, int id);

    //Reset document ID
    @Query("UPDATE document_user SET delivery_date = :delivery_date WHERE id = :id")
    void updateDocumentDate(String delivery_date, int id);

    //Delete diapo where id == @Id
    @Query("DELETE FROM diapositive_format WHERE id = :id")
    void deleteOneDiapo(int id);

    //Insert DiapoImagePath Object
    @Insert
    void insertDiapoImagePath(DiapoImagePath diapoImagePath);

    //Select all DiapoImagePath Object
    @Query("SELECT * FROM diapo_image_path WHERE id_path = :id_path")
    List<DiapoImagePath> selectDiapoImagePath(int id_path);

    //Delete all DiapoImagePath Object
    @Query("DELETE FROM diapo_image_path WHERE id_path = :id_path")
    void deleteDiapoImagePath(int id_path);

    @Insert
    void insertNewDocAvailable(DocumentAvailable documentAvailable);

    @Query("SELECT * FROM document_available")
    List<DocumentAvailable> selectListDocAvailable();

    //Update docEndField
    @Query("UPDATE document_available SET document_finish = :doc_end WHERE id= :id")
    void updateDocEndField(Boolean doc_end, int id);

    //Update docEndField
    @Query("UPDATE document_available SET document_paid = :doc_paid WHERE id= :id")
    void updateDocPaidField(Boolean doc_paid, int id);

    //Update docEndField
    @Query("UPDATE document_available SET team_id = :team_id WHERE id= :id")
    void updateTeamIdField(String team_id, int id);

    @Insert
    void insertMessageUser(UserMessageDb userMessage);

    @Query("SELECT * FROM user_message_db")
    List<UserMessageDb> selectAllMessage();
}
