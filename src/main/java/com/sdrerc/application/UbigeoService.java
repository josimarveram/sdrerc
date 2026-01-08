/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Departamento;
import com.sdrerc.domain.model.Distrito;
import com.sdrerc.domain.model.Provincia;
import com.sdrerc.infrastructure.repository.DepartamentoRepository;
import com.sdrerc.infrastructure.repository.DistritoRepository;
import com.sdrerc.infrastructure.repository.ProvinciaRepository;
import java.util.List;

/**
 *
 * @author David
 */
public class UbigeoService {
    private final DepartamentoRepository departamentoDAO = new DepartamentoRepository();
    private final ProvinciaRepository provinciaDAO = new ProvinciaRepository();
    private final DistritoRepository distritoDAO = new DistritoRepository();

    public List<Departamento> listarDepartamentos() {
        return departamentoDAO.listarActivos();
    }

    public List<Provincia> listarProvincias(int idDepartamento) {
        return provinciaDAO.listarPorDepartamento(idDepartamento);
    }

    public List<Distrito> listarDistritos(int idProvincia) {
        return distritoDAO.listarPorProvincia(idProvincia);
    }
}
