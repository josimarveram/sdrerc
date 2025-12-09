/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sdrerc.domain.model.Catalogo;
import java.util.ArrayList;
import java.util.List;


public class CatalogoRepository {
    public List<Catalogo> listarCabeceras() throws SQLException {
        
        List<Catalogo> lista = new ArrayList<>();
        String sql = "SELECT ID_CATALOGO, DESCRIPCION, ACTIVO FROM CATALOGO";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs= stmt.executeQuery()) {

            while(rs.next())         {   
                lista.add(new Catalogo(
                        rs.getInt("ID_CATALOGO"),
                        rs.getString("DESCRIPCION"),
                        rs.getInt("ACTIVO")
                ));
            }
            
        } catch(SQLException ex){
            throw new RuntimeException("Error listando catalogos" + ex.getMessage());
        }

        return lista; // usuario no encontrado
    }
    
    
}
