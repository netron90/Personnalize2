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

    //DELETE SPECIFIC ELEMENT INTO DATABASE
    @Query("DELETE FROM document_user WHERE document_info = :name")
    public void deleteOneDocument(String name);

    //Update power point
    @Query("UPDATE document_user SET power_point = :power_point_value WHERE id= :id")
    public void updatePowerPoint(Boolean power_point_value, int id);

    //Update mise en forme
    @Query("UPDATE document_user SET mise_en_forme = :mise_en_forme WHERE id= :id")
    public void updateMiseEnForme(Boolean mise_en_forme, int id);
}
