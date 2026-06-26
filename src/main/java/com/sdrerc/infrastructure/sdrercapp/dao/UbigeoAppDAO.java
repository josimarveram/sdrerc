package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.UbigeoItemDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UbigeoAppDAO {

    public List<UbigeoItemDTO> listarDepartamentos() throws SQLException {
        String sql = "SELECT id_ubigeo_departamento, codigo, nombre "
                + "FROM ubigeo_departamento WHERE activo = 1 ORDER BY nombre";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<UbigeoItemDTO> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new UbigeoItemDTO(
                        rs.getLong("id_ubigeo_departamento"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        null));
            }
            return items;
        }
    }

    public List<UbigeoItemDTO> listarProvincias(Long idDepartamento) throws SQLException {
        if (idDepartamento == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT id_ubigeo_provincia, codigo, nombre, id_ubigeo_departamento "
                + "FROM ubigeo_provincia WHERE activo = 1 AND id_ubigeo_departamento = ? ORDER BY nombre";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idDepartamento);
            try (ResultSet rs = ps.executeQuery()) {
                List<UbigeoItemDTO> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(new UbigeoItemDTO(
                            rs.getLong("id_ubigeo_provincia"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getLong("id_ubigeo_departamento")));
                }
                return items;
            }
        }
    }

    public List<UbigeoItemDTO> listarDistritos(Long idProvincia) throws SQLException {
        if (idProvincia == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT id_ubigeo_distrito, codigo, nombre, id_ubigeo_provincia "
                + "FROM ubigeo_distrito WHERE activo = 1 AND id_ubigeo_provincia = ? ORDER BY nombre";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idProvincia);
            try (ResultSet rs = ps.executeQuery()) {
                List<UbigeoItemDTO> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(new UbigeoItemDTO(
                            rs.getLong("id_ubigeo_distrito"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getLong("id_ubigeo_provincia")));
                }
                return items;
            }
        }
    }
}
