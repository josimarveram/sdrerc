/* ============================================================
   SCRIPT 08 - Constraints e indices
   Ejecutar conectado como SDRERC_APP despues de scripts 02 a 07.
   ============================================================ */

ALTER TABLE area ADD CONSTRAINT pk_area PRIMARY KEY (id_area);
ALTER TABLE rol ADD CONSTRAINT pk_rol PRIMARY KEY (id_rol);
ALTER TABLE usuario ADD CONSTRAINT pk_usuario PRIMARY KEY (id_usuario);
ALTER TABLE usuario_rol ADD CONSTRAINT pk_usuario_rol PRIMARY KEY (id_usuario_rol);
ALTER TABLE equipo ADD CONSTRAINT pk_equipo PRIMARY KEY (id_equipo);
ALTER TABLE equipo_usuario ADD CONSTRAINT pk_equipo_usuario PRIMARY KEY (id_equipo_usuario);
ALTER TABLE usuario_supervision ADD CONSTRAINT pk_usuario_supervision PRIMARY KEY (id_usuario_supervision);
ALTER TABLE entidad_externa ADD CONSTRAINT pk_entidad_externa PRIMARY KEY (id_entidad_externa);
ALTER TABLE canal_recepcion ADD CONSTRAINT pk_canal_recepcion PRIMARY KEY (id_canal_recepcion);

ALTER TABLE etapa_expediente ADD CONSTRAINT pk_etapa_expediente PRIMARY KEY (id_etapa);
ALTER TABLE estado_expediente ADD CONSTRAINT pk_estado_expediente PRIMARY KEY (id_estado);
ALTER TABLE tipo_movimiento ADD CONSTRAINT pk_tipo_movimiento PRIMARY KEY (id_tipo_movimiento);
ALTER TABLE tipo_documento ADD CONSTRAINT pk_tipo_documento PRIMARY KEY (id_tipo_documento);
ALTER TABLE tipo_acta ADD CONSTRAINT pk_tipo_acta PRIMARY KEY (id_tipo_acta);
ALTER TABLE procedimiento_registral ADD CONSTRAINT pk_procedimiento_registral PRIMARY KEY (id_procedimiento_registral);
ALTER TABLE tipo_documento_adjunto ADD CONSTRAINT pk_tipo_documento_adjunto PRIMARY KEY (id_tipo_documento_adjunto);
ALTER TABLE estado_documento ADD CONSTRAINT pk_estado_documento PRIMARY KEY (id_estado_documento);
ALTER TABLE tipo_observacion ADD CONSTRAINT pk_tipo_observacion PRIMARY KEY (id_tipo_observacion);
ALTER TABLE tipo_resultado_evaluacion ADD CONSTRAINT pk_tipo_resultado_eval PRIMARY KEY (id_tipo_resultado_evaluacion);
ALTER TABLE tipo_resultado_ejecucion ADD CONSTRAINT pk_tipo_resultado_ejec PRIMARY KEY (id_tipo_resultado_ejecucion);
ALTER TABLE tipo_notificacion ADD CONSTRAINT pk_tipo_notificacion PRIMARY KEY (id_tipo_notificacion);
ALTER TABLE estado_notificacion ADD CONSTRAINT pk_estado_notificacion PRIMARY KEY (id_estado_notificacion);
ALTER TABLE estado_cargo_acuse ADD CONSTRAINT pk_estado_cargo_acuse PRIMARY KEY (id_estado_cargo_acuse);
ALTER TABLE tipo_resolucion ADD CONSTRAINT pk_tipo_resolucion PRIMARY KEY (id_tipo_resolucion);
ALTER TABLE motivo_no_corresponde ADD CONSTRAINT pk_motivo_no_corresponde PRIMARY KEY (id_motivo_no_corresponde);
ALTER TABLE motivo_archivo ADD CONSTRAINT pk_motivo_archivo PRIMARY KEY (id_motivo_archivo);
ALTER TABLE motivo_correccion ADD CONSTRAINT pk_motivo_correccion PRIMARY KEY (id_motivo_correccion);

ALTER TABLE flujo ADD CONSTRAINT pk_flujo PRIMARY KEY (id_flujo);
ALTER TABLE flujo_transicion ADD CONSTRAINT pk_flujo_transicion PRIMARY KEY (id_flujo_transicion);

ALTER TABLE persona ADD CONSTRAINT pk_persona PRIMARY KEY (id_persona);
ALTER TABLE expediente ADD CONSTRAINT pk_expediente PRIMARY KEY (id_expediente);
ALTER TABLE expediente_solicitud ADD CONSTRAINT pk_expediente_solicitud PRIMARY KEY (id_expediente_solicitud);
ALTER TABLE expediente_persona ADD CONSTRAINT pk_expediente_persona PRIMARY KEY (id_expediente_persona);
ALTER TABLE expediente_acta ADD CONSTRAINT pk_expediente_acta PRIMARY KEY (id_expediente_acta);
ALTER TABLE expediente_relacion ADD CONSTRAINT pk_expediente_relacion PRIMARY KEY (id_expediente_relacion);
ALTER TABLE expediente_asignacion ADD CONSTRAINT pk_expediente_asignacion PRIMARY KEY (id_expediente_asignacion);
ALTER TABLE expediente_documento ADD CONSTRAINT pk_expediente_documento PRIMARY KEY (id_expediente_documento);
ALTER TABLE expediente_documento_analizado ADD CONSTRAINT pk_expediente_doc_analizado PRIMARY KEY (id_documento_analizado);
ALTER TABLE expediente_evaluacion ADD CONSTRAINT pk_expediente_evaluacion PRIMARY KEY (id_expediente_evaluacion);
ALTER TABLE expediente_observacion ADD CONSTRAINT pk_expediente_observacion PRIMARY KEY (id_expediente_observacion);
ALTER TABLE expediente_resolucion ADD CONSTRAINT pk_expediente_resolucion PRIMARY KEY (id_expediente_resolucion);
ALTER TABLE expediente_notificacion ADD CONSTRAINT pk_expediente_notificacion PRIMARY KEY (id_expediente_notificacion);
ALTER TABLE expediente_cargo_acuse ADD CONSTRAINT pk_expediente_cargo_acuse PRIMARY KEY (id_expediente_cargo_acuse);
ALTER TABLE expediente_digital ADD CONSTRAINT pk_expediente_digital PRIMARY KEY (id_expediente_digital);
ALTER TABLE expediente_historial ADD CONSTRAINT pk_expediente_historial PRIMARY KEY (id_expediente_historial);
ALTER TABLE auditoria_evento ADD CONSTRAINT pk_auditoria_evento PRIMARY KEY (id_auditoria_evento);
ALTER TABLE legacy_estado_map ADD CONSTRAINT pk_legacy_estado_map PRIMARY KEY (id_legacy_estado_map);
ALTER TABLE legacy_catalogo_map ADD CONSTRAINT pk_legacy_catalogo_map PRIMARY KEY (id_legacy_catalogo_map);
ALTER TABLE expediente_derivacion_externa ADD CONSTRAINT pk_expediente_derivacion_ext PRIMARY KEY (id_derivacion_externa);
ALTER TABLE expediente_publicacion ADD CONSTRAINT pk_expediente_publicacion PRIMARY KEY (id_expediente_publicacion);
ALTER TABLE plazo_configuracion ADD CONSTRAINT pk_plazo_configuracion PRIMARY KEY (id_plazo_configuracion);
ALTER TABLE expediente_plazo ADD CONSTRAINT pk_expediente_plazo PRIMARY KEY (id_expediente_plazo);
ALTER TABLE expediente_alerta ADD CONSTRAINT pk_expediente_alerta PRIMARY KEY (id_expediente_alerta);
ALTER TABLE permiso ADD CONSTRAINT pk_permiso PRIMARY KEY (id_permiso);
ALTER TABLE rol_permiso ADD CONSTRAINT pk_rol_permiso PRIMARY KEY (id_rol_permiso);
ALTER TABLE flujo_transicion_rol ADD CONSTRAINT pk_flujo_transicion_rol PRIMARY KEY (id_flujo_transicion_rol);
ALTER TABLE flujo_transicion_equipo ADD CONSTRAINT pk_flujo_transicion_equipo PRIMARY KEY (id_flujo_transicion_equipo);
ALTER TABLE expediente_metrica_etapa ADD CONSTRAINT pk_expediente_metrica_etapa PRIMARY KEY (id_expediente_metrica_etapa);

ALTER TABLE area ADD CONSTRAINT uk_area_codigo UNIQUE (codigo);
ALTER TABLE rol ADD CONSTRAINT uk_rol_codigo UNIQUE (codigo);
ALTER TABLE usuario ADD CONSTRAINT uk_usuario_username UNIQUE (username);
ALTER TABLE equipo ADD CONSTRAINT uk_equipo_codigo UNIQUE (codigo);
ALTER TABLE entidad_externa ADD CONSTRAINT uk_entidad_externa_codigo UNIQUE (codigo);
ALTER TABLE canal_recepcion ADD CONSTRAINT uk_canal_recepcion_codigo UNIQUE (codigo);
ALTER TABLE etapa_expediente ADD CONSTRAINT uk_etapa_codigo UNIQUE (codigo);
ALTER TABLE estado_expediente ADD CONSTRAINT uk_estado_codigo UNIQUE (codigo);
ALTER TABLE tipo_movimiento ADD CONSTRAINT uk_tipo_movimiento_codigo UNIQUE (codigo);
ALTER TABLE tipo_documento ADD CONSTRAINT uk_tipo_documento_codigo UNIQUE (codigo);
ALTER TABLE tipo_acta ADD CONSTRAINT uk_tipo_acta_codigo UNIQUE (codigo);
ALTER TABLE procedimiento_registral ADD CONSTRAINT uk_proc_reg_codigo UNIQUE (codigo);
ALTER TABLE tipo_documento_adjunto ADD CONSTRAINT uk_tipo_doc_adj_codigo UNIQUE (codigo);
ALTER TABLE estado_documento ADD CONSTRAINT uk_estado_documento_codigo UNIQUE (codigo);
ALTER TABLE tipo_resultado_evaluacion ADD CONSTRAINT uk_tipo_result_eval_codigo UNIQUE (codigo);
ALTER TABLE tipo_resultado_ejecucion ADD CONSTRAINT uk_tipo_result_ejec_codigo UNIQUE (codigo);
ALTER TABLE tipo_notificacion ADD CONSTRAINT uk_tipo_notificacion_codigo UNIQUE (codigo);
ALTER TABLE estado_notificacion ADD CONSTRAINT uk_estado_notificacion_codigo UNIQUE (codigo);
ALTER TABLE estado_cargo_acuse ADD CONSTRAINT uk_estado_cargo_codigo UNIQUE (codigo);
ALTER TABLE tipo_resolucion ADD CONSTRAINT uk_tipo_resolucion_codigo UNIQUE (codigo);
ALTER TABLE flujo ADD CONSTRAINT uk_flujo_codigo UNIQUE (codigo);
ALTER TABLE flujo_transicion ADD CONSTRAINT uk_flujo_transicion_ruta UNIQUE (
  id_flujo,
  id_etapa_origen,
  id_estado_origen,
  codigo_accion,
  id_etapa_destino,
  id_estado_destino
);
ALTER TABLE legacy_estado_map ADD CONSTRAINT uk_legacy_estado_id UNIQUE (id_legacy);
ALTER TABLE permiso ADD CONSTRAINT uk_permiso_codigo UNIQUE (codigo);

ALTER TABLE usuario_rol ADD CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario);
ALTER TABLE usuario_rol ADD CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol);
ALTER TABLE equipo ADD CONSTRAINT fk_equipo_area FOREIGN KEY (id_area) REFERENCES area(id_area);
ALTER TABLE equipo_usuario ADD CONSTRAINT fk_equipo_usuario_equipo FOREIGN KEY (id_equipo) REFERENCES equipo(id_equipo);
ALTER TABLE equipo_usuario ADD CONSTRAINT fk_equipo_usuario_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario);
ALTER TABLE usuario_supervision ADD CONSTRAINT fk_usuario_sup_supervisor FOREIGN KEY (id_supervisor) REFERENCES usuario(id_usuario);
ALTER TABLE usuario_supervision ADD CONSTRAINT fk_usuario_sup_abogado FOREIGN KEY (id_abogado) REFERENCES usuario(id_usuario);

ALTER TABLE estado_expediente ADD CONSTRAINT fk_estado_etapa FOREIGN KEY (id_etapa) REFERENCES etapa_expediente(id_etapa);
ALTER TABLE flujo_transicion ADD CONSTRAINT fk_transicion_flujo FOREIGN KEY (id_flujo) REFERENCES flujo(id_flujo);
ALTER TABLE flujo_transicion ADD CONSTRAINT fk_transicion_etapa_ori FOREIGN KEY (id_etapa_origen) REFERENCES etapa_expediente(id_etapa);
ALTER TABLE flujo_transicion ADD CONSTRAINT fk_transicion_estado_ori FOREIGN KEY (id_estado_origen) REFERENCES estado_expediente(id_estado);
ALTER TABLE flujo_transicion ADD CONSTRAINT fk_transicion_etapa_des FOREIGN KEY (id_etapa_destino) REFERENCES etapa_expediente(id_etapa);
ALTER TABLE flujo_transicion ADD CONSTRAINT fk_transicion_estado_des FOREIGN KEY (id_estado_destino) REFERENCES estado_expediente(id_estado);

ALTER TABLE expediente ADD CONSTRAINT fk_exp_etapa_actual FOREIGN KEY (id_etapa_actual) REFERENCES etapa_expediente(id_etapa);
ALTER TABLE expediente ADD CONSTRAINT fk_exp_estado_actual FOREIGN KEY (id_estado_actual) REFERENCES estado_expediente(id_estado);
ALTER TABLE expediente ADD CONSTRAINT fk_exp_resp_actual FOREIGN KEY (id_usuario_responsable_actual) REFERENCES usuario(id_usuario);
ALTER TABLE expediente ADD CONSTRAINT fk_exp_abogado_inicial FOREIGN KEY (id_usuario_abogado_inicial) REFERENCES usuario(id_usuario);
ALTER TABLE expediente ADD CONSTRAINT fk_exp_equipo_actual FOREIGN KEY (id_equipo_responsable_actual) REFERENCES equipo(id_equipo);

ALTER TABLE expediente_solicitud ADD CONSTRAINT fk_exp_sol_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_solicitud ADD CONSTRAINT fk_exp_sol_entidad FOREIGN KEY (id_entidad_origen) REFERENCES entidad_externa(id_entidad_externa);
ALTER TABLE expediente_solicitud ADD CONSTRAINT fk_exp_sol_canal FOREIGN KEY (id_canal_recepcion) REFERENCES canal_recepcion(id_canal_recepcion);
ALTER TABLE expediente_solicitud ADD CONSTRAINT fk_exp_sol_persona FOREIGN KEY (id_persona_solicitante) REFERENCES persona(id_persona);
ALTER TABLE expediente_persona ADD CONSTRAINT fk_exp_per_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_persona ADD CONSTRAINT fk_exp_per_persona FOREIGN KEY (id_persona) REFERENCES persona(id_persona);
ALTER TABLE expediente_acta ADD CONSTRAINT fk_exp_acta_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_acta ADD CONSTRAINT fk_exp_acta_tipo FOREIGN KEY (id_tipo_acta) REFERENCES tipo_acta(id_tipo_acta);
ALTER TABLE expediente_relacion ADD CONSTRAINT fk_exp_rel_principal FOREIGN KEY (id_expediente_principal) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_relacion ADD CONSTRAINT fk_exp_rel_relacionado FOREIGN KEY (id_expediente_relacionado) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_asignacion ADD CONSTRAINT fk_exp_asig_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_asignacion ADD CONSTRAINT fk_exp_asig_usuario FOREIGN KEY (id_usuario_asignado) REFERENCES usuario(id_usuario);
ALTER TABLE expediente_asignacion ADD CONSTRAINT fk_exp_asig_equipo FOREIGN KEY (id_equipo_asignado) REFERENCES equipo(id_equipo);
ALTER TABLE expediente_asignacion ADD CONSTRAINT fk_exp_asig_etapa FOREIGN KEY (id_etapa) REFERENCES etapa_expediente(id_etapa);
ALTER TABLE expediente_documento ADD CONSTRAINT fk_exp_doc_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_documento ADD CONSTRAINT fk_exp_doc_tipo FOREIGN KEY (id_tipo_documento_adjunto) REFERENCES tipo_documento_adjunto(id_tipo_documento_adjunto);
ALTER TABLE expediente_documento ADD CONSTRAINT fk_exp_doc_estado FOREIGN KEY (id_estado_documento) REFERENCES estado_documento(id_estado_documento);
ALTER TABLE expediente_documento_analizado ADD CONSTRAINT fk_exp_doc_an_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_documento_analizado ADD CONSTRAINT fk_exp_doc_an_tipo FOREIGN KEY (id_tipo_documento_adjunto) REFERENCES tipo_documento_adjunto(id_tipo_documento_adjunto);
ALTER TABLE expediente_documento_analizado ADD CONSTRAINT fk_exp_doc_an_estado FOREIGN KEY (id_estado_documento) REFERENCES estado_documento(id_estado_documento);
ALTER TABLE expediente_evaluacion ADD CONSTRAINT fk_exp_eval_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_evaluacion ADD CONSTRAINT fk_exp_eval_result FOREIGN KEY (id_tipo_resultado_evaluacion) REFERENCES tipo_resultado_evaluacion(id_tipo_resultado_evaluacion);
ALTER TABLE expediente_evaluacion ADD CONSTRAINT fk_exp_eval_motivo_no_corr FOREIGN KEY (id_motivo_no_corresponde) REFERENCES motivo_no_corresponde(id_motivo_no_corresponde);
ALTER TABLE expediente_observacion ADD CONSTRAINT fk_exp_obs_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_observacion ADD CONSTRAINT fk_exp_obs_tipo FOREIGN KEY (id_tipo_observacion) REFERENCES tipo_observacion(id_tipo_observacion);
ALTER TABLE expediente_observacion ADD CONSTRAINT fk_exp_obs_motivo_corr FOREIGN KEY (id_motivo_correccion) REFERENCES motivo_correccion(id_motivo_correccion);
ALTER TABLE expediente_resolucion ADD CONSTRAINT fk_exp_res_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_resolucion ADD CONSTRAINT fk_exp_res_tipo FOREIGN KEY (id_tipo_resolucion) REFERENCES tipo_resolucion(id_tipo_resolucion);
ALTER TABLE expediente_resolucion ADD CONSTRAINT fk_exp_res_doc FOREIGN KEY (id_documento_resolucion) REFERENCES expediente_documento(id_expediente_documento);
ALTER TABLE expediente_notificacion ADD CONSTRAINT fk_exp_not_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_notificacion ADD CONSTRAINT fk_exp_not_res FOREIGN KEY (id_expediente_resolucion) REFERENCES expediente_resolucion(id_expediente_resolucion);
ALTER TABLE expediente_notificacion ADD CONSTRAINT fk_exp_not_tipo FOREIGN KEY (id_tipo_notificacion) REFERENCES tipo_notificacion(id_tipo_notificacion);
ALTER TABLE expediente_notificacion ADD CONSTRAINT fk_exp_not_estado FOREIGN KEY (id_estado_notificacion) REFERENCES estado_notificacion(id_estado_notificacion);
ALTER TABLE expediente_cargo_acuse ADD CONSTRAINT fk_exp_cargo_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_cargo_acuse ADD CONSTRAINT fk_exp_cargo_not FOREIGN KEY (id_expediente_notificacion) REFERENCES expediente_notificacion(id_expediente_notificacion);
ALTER TABLE expediente_cargo_acuse ADD CONSTRAINT fk_exp_cargo_estado FOREIGN KEY (id_estado_cargo_acuse) REFERENCES estado_cargo_acuse(id_estado_cargo_acuse);
ALTER TABLE expediente_cargo_acuse ADD CONSTRAINT fk_exp_cargo_doc FOREIGN KEY (id_documento_cargo) REFERENCES expediente_documento(id_expediente_documento);
ALTER TABLE expediente_digital ADD CONSTRAINT fk_exp_dig_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_digital ADD CONSTRAINT fk_exp_dig_resp FOREIGN KEY (id_usuario_responsable) REFERENCES usuario(id_usuario);
ALTER TABLE expediente_digital ADD CONSTRAINT fk_exp_dig_custodio FOREIGN KEY (id_usuario_custodio) REFERENCES usuario(id_usuario);
ALTER TABLE expediente_historial ADD CONSTRAINT fk_exp_hist_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_historial ADD CONSTRAINT fk_exp_hist_mov FOREIGN KEY (id_tipo_movimiento) REFERENCES tipo_movimiento(id_tipo_movimiento);
ALTER TABLE expediente_derivacion_externa ADD CONSTRAINT fk_der_ext_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_derivacion_externa ADD CONSTRAINT fk_der_ext_entidad FOREIGN KEY (id_entidad_destino) REFERENCES entidad_externa(id_entidad_externa);
ALTER TABLE expediente_derivacion_externa ADD CONSTRAINT fk_der_ext_doc_env FOREIGN KEY (id_documento_enviado) REFERENCES expediente_documento(id_expediente_documento);
ALTER TABLE expediente_derivacion_externa ADD CONSTRAINT fk_der_ext_doc_resp FOREIGN KEY (id_documento_respuesta) REFERENCES expediente_documento(id_expediente_documento);
ALTER TABLE expediente_publicacion ADD CONSTRAINT fk_publicacion_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_publicacion ADD CONSTRAINT fk_publicacion_not FOREIGN KEY (id_expediente_notificacion) REFERENCES expediente_notificacion(id_expediente_notificacion);
ALTER TABLE plazo_configuracion ADD CONSTRAINT fk_plazo_conf_etapa FOREIGN KEY (id_etapa) REFERENCES etapa_expediente(id_etapa);
ALTER TABLE plazo_configuracion ADD CONSTRAINT fk_plazo_conf_tipo_doc FOREIGN KEY (id_tipo_documento) REFERENCES tipo_documento(id_tipo_documento);
ALTER TABLE expediente_plazo ADD CONSTRAINT fk_exp_plazo_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_plazo ADD CONSTRAINT fk_exp_plazo_conf FOREIGN KEY (id_plazo_configuracion) REFERENCES plazo_configuracion(id_plazo_configuracion);
ALTER TABLE expediente_alerta ADD CONSTRAINT fk_exp_alerta_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE rol_permiso ADD CONSTRAINT fk_rol_permiso_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol);
ALTER TABLE rol_permiso ADD CONSTRAINT fk_rol_permiso_permiso FOREIGN KEY (id_permiso) REFERENCES permiso(id_permiso);
ALTER TABLE flujo_transicion_rol ADD CONSTRAINT fk_ftr_transicion FOREIGN KEY (id_flujo_transicion) REFERENCES flujo_transicion(id_flujo_transicion);
ALTER TABLE flujo_transicion_rol ADD CONSTRAINT fk_ftr_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol);
ALTER TABLE flujo_transicion_equipo ADD CONSTRAINT fk_fte_transicion FOREIGN KEY (id_flujo_transicion) REFERENCES flujo_transicion(id_flujo_transicion);
ALTER TABLE flujo_transicion_equipo ADD CONSTRAINT fk_fte_equipo FOREIGN KEY (id_equipo) REFERENCES equipo(id_equipo);
ALTER TABLE expediente_metrica_etapa ADD CONSTRAINT fk_exp_met_exp FOREIGN KEY (id_expediente) REFERENCES expediente(id_expediente);
ALTER TABLE expediente_metrica_etapa ADD CONSTRAINT fk_exp_met_etapa FOREIGN KEY (id_etapa) REFERENCES etapa_expediente(id_etapa);

ALTER TABLE expediente_solicitud ADD CONSTRAINT ck_sol_correo_virtual CHECK (es_tramite_virtual = 0 OR correo_electronico IS NOT NULL);
ALTER TABLE expediente_evaluacion ADD CONSTRAINT ck_eval_incorporado CHECK (incorporado IN (0,1));
ALTER TABLE expediente_notificacion ADD CONSTRAINT ck_notificacion_intento CHECK (numero_intento BETWEEN 1 AND 3);

CREATE INDEX ix_expediente_numero ON expediente(numero_expediente);
CREATE INDEX ix_expediente_tramite ON expediente(numero_tramite_documentario);
CREATE INDEX ix_expediente_estado ON expediente(id_estado_actual);
CREATE INDEX ix_expediente_etapa ON expediente(id_etapa_actual);
CREATE INDEX ix_expediente_resp ON expediente(id_usuario_responsable_actual);
CREATE INDEX ix_expediente_abogado_ini ON expediente(id_usuario_abogado_inicial);
CREATE INDEX ix_expediente_ult_mov ON expediente(fecha_ultimo_movimiento);
CREATE INDEX ix_exp_sol_tramite ON expediente_solicitud(numero_tramite_documentario);
CREATE INDEX ix_persona_documento ON persona(numero_documento);
CREATE INDEX ix_exp_acta_numero ON expediente_acta(numero_acta);
CREATE INDEX ix_exp_rel_principal ON expediente_relacion(id_expediente_principal);
CREATE INDEX ix_exp_asig_activa ON expediente_asignacion(id_expediente, activa);
CREATE INDEX ix_exp_hist_exp_fecha ON expediente_historial(id_expediente, fecha_movimiento);
CREATE INDEX ix_exp_not_exp_intento ON expediente_notificacion(id_expediente, numero_intento);
CREATE INDEX ix_exp_cargo_not ON expediente_cargo_acuse(id_expediente_notificacion);
CREATE INDEX ix_audit_tabla_reg ON auditoria_evento(tabla_afectada, id_registro);
CREATE INDEX ix_der_ext_exp ON expediente_derivacion_externa(id_expediente);
CREATE INDEX ix_publicacion_exp ON expediente_publicacion(id_expediente);
CREATE INDEX ix_exp_plazo_exp ON expediente_plazo(id_expediente);
CREATE INDEX ix_exp_alerta_exp ON expediente_alerta(id_expediente);
CREATE INDEX ix_exp_metrica_exp ON expediente_metrica_etapa(id_expediente);
