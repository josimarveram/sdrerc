/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Catalogo;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.User;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author DESARROLLADOR84_USI
 */
public class CatalogoDetalleRepository {
    public List<CatalogoItem> listarCatalogoItem(int idCatalogo) throws SQLException {
        
        List<CatalogoItem> lista = new ArrayList<>();
        String sql = "SELECT id_catalogo_item,id_catalogo, descripcion, active FROM catalogo_item";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            //ps.setInt(1, idCatalogo);
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                lista.add(new CatalogoItem(
                            rs.getInt("ID_CATALOGO_ITEM"),
                            rs.getInt("ID_CATALOGO"),
                            rs.getString("DESCRIPCION"),
                            rs.getInt("ACTIVE")                            
                ));
            }
        } catch(SQLException ex){
            throw new RuntimeException("Error listando catalogos" + ex.getMessage());
        }

        return lista; // usuario no encontrado
    }
}
