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
        
        
        System.out.println("➡ Ejecutando consulta con id_catalogo = " + idCatalogo);
    
        List<CatalogoItem> lista = new ArrayList<>();
        //idCatalogo = 2;
        String sql = "SELECT id_catalogo_item,id_catalogo, descripcion, active FROM catalogo_item WHERE id_catalogo = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idCatalogo);
            
            ResultSet rs = ps.executeQuery();
            
            if (!rs.isBeforeFirst()) {
                System.out.println("⚠ NO se encontraron registros en catalogo_item.");
            }
            
            while (rs.next()) {
                
                System.out.println("✔ Registro encontrado: " + rs.getString("DESCRIPCION"));
                lista.add(new CatalogoItem(
                            rs.getInt("ID_CATALOGO_ITEM"),
                            rs.getInt("ID_CATALOGO"),
                            rs.getString("DESCRIPCION"),
                            rs.getInt("ACTIVE")                            
                ));
            }
        } 
        catch(SQLException ex){
            throw new RuntimeException("Error listando catalogos" + ex.getMessage());
        }

        return lista; // usuario no encontrado
    }
    
    public List<CatalogoItem> obtenerEstados() {
        List<CatalogoItem> estados = new ArrayList<>();

        String sql = "SELECT ID_CATALOGO_ITEM,ID_CATALOGO, DESCRIPCION, ACTIVE FROM CATALOGO_ITEM WHERE ID_CATALOGO = 5";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs= stmt.executeQuery()) {

            while (rs.next()) {
                estados.add(new CatalogoItem(
                            rs.getInt("ID_CATALOGO_ITEM"),
                            rs.getInt("ID_CATALOGO"),
                            rs.getString("DESCRIPCION"),
                            rs.getInt("ACTIVE")  
                ));
            }

        } catch(SQLException ex){
            throw new RuntimeException("Error listando catalogos" + ex.getMessage());
        }

        return estados;
    }
}
