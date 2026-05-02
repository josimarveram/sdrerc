package com.sdrerc.ui.views.equipojuridico;

import com.sdrerc.domain.model.EquipoJuridicoImportPreview;
import com.sdrerc.domain.model.EquipoJuridicoImportResult;

public interface EquipoJuridicoImportOwner {

    EquipoJuridicoImportResult confirmarImportacionEquipoJuridico(
            EquipoJuridicoImportPreview preview,
            boolean incluirAdvertencias) throws Exception;
}
