package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.CargaDiariaPreviewDTO;
import com.sdrerc.v3.domain.CentroAtencionDTO;
import com.sdrerc.v3.domain.MunicipalidadDTO;
import com.sdrerc.v3.domain.UnidadOrganicaDTO;
import com.sdrerc.v3.domain.rules.ProcedimientoRegistralRules;
import com.sdrerc.v3.infrastructure.dao.CatalogoLookupDAO;
import com.sdrerc.v3.infrastructure.dao.ExpedienteRegistroDAO;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Port literal de com.sdrerc.application.sdrercapp.CargaDiariaReglasService (V2): mismas reglas de
 * validación (obligatorios, duplicidad acta+titular en archivo y en base, N° SGD único, posible
 * grupo familiar, generación de número preliminar).
 *
 * <p>18/08/2026, pedido explícito del usuario — deja de ser un port 100% literal en un punto: el
 * número de expediente ya no se asigna en el orden de fila del Excel, sino en el orden de la Fecha
 * de solicitud (fila con fecha más antigua recibe el correlativo más bajo). Ver el segundo bucle al
 * final de {@link #validar}, después de calcular elegibilidad/duplicados/grupo familiar igual que
 * antes.</p>
 *
 * <p>14/08/2026, Fase 3 de la iniciativa "Solicitante con 3 tipos" (ver v3/CLAUDE.md, sin
 * equivalente en V2) — REDISEÑADO el mismo día (pedido explícito del usuario): no hay columnas de
 * Excel para tipo/centro/unidad. El tipo de solicitante se IDENTIFICA AUTOMÁTICAMENTE comparando
 * "SOLICITADO POR" (texto libre, único campo de entrada) contra {@code CENTROS_ATENCION.denominacion}/
 * {@code UNIDADES_ORGANICAS.nombre} — ver {@link #clasificarTipoSolicitante}.</p>
 */
@Service
public class CargaDiariaReglasService {

    private final ExpedienteRegistroDAO expedienteRegistroDAO;
    private final CorrelativoExpedienteService correlativoExpedienteService;
    private final GrupoFamiliarHeuristicaService grupoFamiliarHeuristicaService;
    private final CatalogoLookupDAO catalogoLookupDAO;

    public CargaDiariaReglasService(
            ExpedienteRegistroDAO expedienteRegistroDAO,
            CorrelativoExpedienteService correlativoExpedienteService,
            GrupoFamiliarHeuristicaService grupoFamiliarHeuristicaService,
            CatalogoLookupDAO catalogoLookupDAO) {
        this.expedienteRegistroDAO = expedienteRegistroDAO;
        this.correlativoExpedienteService = correlativoExpedienteService;
        this.grupoFamiliarHeuristicaService = grupoFamiliarHeuristicaService;
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public List<CargaDiariaPreviewDTO> validar(List<CargaDiariaPreviewDTO> registros) throws SQLException {
        if (registros == null) {
            return new ArrayList<CargaDiariaPreviewDTO>();
        }

        Map<String, Integer> actaTitular = new HashMap<String, Integer>();
        Map<String, Integer> primeraFilaActaTitular = new HashMap<String, Integer>();
        Map<String, Integer> apellidosTitular = new HashMap<String, Integer>();
        Map<String, Integer> titularesExactos = new HashMap<String, Integer>();
        Map<String, Integer> numeroSgd = new HashMap<String, Integer>();
        Map<String, Integer> primeraFilaNumeroSgd = new HashMap<String, Integer>();
        for (CargaDiariaPreviewDTO item : registros) {
            item.reiniciarValidacion();
            item.limpiarDeteccionGrupoFamiliar();
            String claveActaTitular = claveActaTitular(item);
            sumarClave(actaTitular, claveActaTitular);
            if (hasText(claveActaTitular) && !primeraFilaActaTitular.containsKey(claveActaTitular)) {
                primeraFilaActaTitular.put(claveActaTitular, item.getFila());
            }
            sumarClave(apellidosTitular, claveApellidosTitular(item));
            sumarClave(titularesExactos, claveTitularExacto(item));
            String claveSgd = clave(item.getNumeroExpedienteSgd());
            sumarClave(numeroSgd, claveSgd);
            if (hasText(claveSgd) && !primeraFilaNumeroSgd.containsKey(claveSgd)) {
                primeraFilaNumeroSgd.put(claveSgd, item.getFila());
            }
        }

        Map<Integer, String> duplicadosBase = expedienteRegistroDAO.detectarDuplicadosContraBase(registros);
        Map<Integer, String> duplicadosTitularExactoBase = expedienteRegistroDAO.detectarDuplicadosPorTitularExactoContraBase(registros);
        Map<Integer, String> posiblesGruposFamiliaresBase =
                expedienteRegistroDAO.detectarPosiblesGruposFamiliaresContraBase(registros);
        Map<Integer, String> duplicadosSgdBase = expedienteRegistroDAO.detectarDuplicadosPorNumeroSgdContraBase(registros);

        // Solicitante con 3 tipos: catálogos cargados una sola vez para todo el archivo (no una
        // consulta por fila). Centro de Atención se identifica por NOMBRE de oficina (denominación),
        // no por Código SIO (pedido explícito del usuario, 14/08/2026: "solo necesitamos el nombre
        // de la oficina, los demás campos solo serían como mantenimiento para más adelante").
        List<CentroAtencionDTO> centrosAtencion = catalogoLookupDAO.listarCentrosAtencion();
        List<UnidadOrganicaDTO> unidadesOrganicas = catalogoLookupDAO.listarUnidadesOrganicas();
        List<MunicipalidadDTO> municipalidades = catalogoLookupDAO.listarMunicipalidades();

        // Los números de expediente ya no se asignan en el orden del Excel, sino en el orden de la
        // Fecha de solicitud (18/08/2026, pedido explícito del usuario) — se junta acá la lista de
        // filas elegibles (sin bloqueo por SGD, sin duplicado, sin requerir decisión de asignación)
        // dentro del mismo recorrido de siempre, y el correlativo real se asigna después en un
        // segundo paso, ya ordenado por fecha (ver el bloque tras este `for`).
        List<CargaDiariaPreviewDTO> elegiblesParaNumero = new ArrayList<CargaDiariaPreviewDTO>();
        for (CargaDiariaPreviewDTO item : registros) {
            boolean error = false;
            // N° de trámite (18/08/2026, pedido explícito del usuario, lista de campos obligatorios
            // de Carga diaria): deja de ser obligatorio — coherente con "Nro. tramite web no es
            // obligatorio" ya documentado para Registro manual/Edición manual en el CLAUDE.md raíz.
            if (!hasText(item.getRemitente())) {
                item.agregarMensaje("Solicitado por obligatorio.");
                error = true;
            }
            if (!hasText(item.getNumeroDocumento())) {
                item.agregarMensaje("Número de documento obligatorio.");
                error = true;
            }
            if (!hasText(item.getTipoProcedimiento())) {
                item.agregarMensaje("Tipo de procedimiento obligatorio.");
                error = true;
            }
            if (!hasText(item.getTipoSolicitud())) {
                item.agregarMensaje("Tipo de solicitud obligatorio.");
                error = true;
            }
            if (!hasText(item.getTipoDocumento())) {
                item.agregarMensaje("Tipo de documento obligatorio.");
                error = true;
            }
            if (!hasText(item.getTipoActa())) {
                item.agregarMensaje("Tipo de acta obligatorio.");
                error = true;
            }
            if (!hasText(item.getNumeroActa())) {
                item.agregarMensaje("Número de acta obligatorio.");
                error = true;
            }
            if (!hasText(item.getNumeroExpedienteSgd())) {
                item.agregarMensaje("N° expediente SGD obligatorio.");
                error = true;
            }
            if (!hasText(item.getTitular())) {
                item.agregarMensaje("Titular obligatorio.");
                error = true;
            }
            if (item.getFechaRecepcion() == null) {
                item.agregarMensaje("Fecha de solicitud inválida u obligatoria.");
                error = true;
            }
            if (!hasText(item.getCanalRecepcion())) {
                item.agregarMensaje("No se pudo determinar canal de recepción con las reglas de la plantilla.");
                error = true;
            }

            // Clasificación del tipo de solicitante se resuelve ANTES de la identidad (18/08/2026,
            // pedido explícito del usuario: reordenado para que validarIdentidad ya sepa si el
            // solicitante es una persona o una entidad — ver el guard "solicitanteInstitucional" ahí).
            clasificarTipoSolicitante(item, centrosAtencion, unidadesOrganicas, municipalidades);

            List<String> erroresIdentidad = validarIdentidad(item);
            if (!erroresIdentidad.isEmpty()) {
                for (String mensaje : erroresIdentidad) {
                    item.agregarMensaje(mensaje);
                }
                error = true;
            }

            item.setPosibleDuplicado(false);
            item.setMotivoDuplicado(null);
            item.setMotivoSinNumero(null);

            List<String> motivosDuplicado = new ArrayList<String>();
            String claveActaTitular = claveActaTitular(item);
            Integer cantidadActaTitular = hasText(claveActaTitular) ? actaTitular.get(claveActaTitular) : null;
            if (cantidadActaTitular != null && cantidadActaTitular.intValue() > 1) {
                Integer primeraFila = primeraFilaActaTitular.get(claveActaTitular);
                if (primeraFila == null || item.getFila() != primeraFila.intValue()) {
                    motivosDuplicado.add("Acta y titular repetidos en el archivo. Ya existe una primera ocurrencia en la fila " + primeraFila + ".");
                }
            }
            String duplicadoBd = duplicadosBase.get(item.getFila());
            if (duplicadoBd != null) {
                motivosDuplicado.add(duplicadoBd);
            }
            if (!motivosDuplicado.isEmpty()) {
                item.setPosibleDuplicado(true);
                item.setMotivoDuplicado(String.join(" | ", motivosDuplicado));
                item.agregarMensaje(item.getMotivoDuplicado());
            }

            String claveApellidos = claveApellidosTitular(item);
            Integer cantidadApellidos = hasText(claveApellidos) ? apellidosTitular.get(claveApellidos) : null;
            String claveTitularExacto = claveTitularExacto(item);
            Integer cantidadTitularExacto = hasText(claveTitularExacto) ? titularesExactos.get(claveTitularExacto) : null;
            boolean titularExactoRepetido = cantidadTitularExacto != null && cantidadTitularExacto.intValue() > 1;
            boolean titularExactoEnBase = hasText(duplicadosTitularExactoBase.get(item.getFila()));
            if (!item.isPosibleDuplicado()
                    && !titularExactoRepetido
                    && !titularExactoEnBase
                    && cantidadApellidos != null
                    && cantidadApellidos.intValue() > 1) {
                item.agregarObservacionGrupoFamiliar(
                        "COINCIDENCIA_APELLIDOS_EXCEL",
                        "Posible grupo familiar por coincidencia de apellidos.");
                item.agregarMensaje("Posible grupo familiar por coincidencia de apellidos.");
            }
            String grupoFamiliarBd = posiblesGruposFamiliaresBase.get(item.getFila());
            if (!item.isPosibleDuplicado()
                    && !titularExactoRepetido
                    && !titularExactoEnBase
                    && hasText(grupoFamiliarBd)) {
                item.agregarObservacionGrupoFamiliar("COINCIDENCIA_APELLIDOS_BD", grupoFamiliarBd);
                item.agregarMensaje(grupoFamiliarBd);
            }

            // N° Documento (18/08/2026, pedido explícito del usuario): deja de ser bloqueante. Antes,
            // si faltaba, cortaba el método con "continue" antes de llegar a la lógica de
            // duplicado/generación de número (líneas de abajo), dejando la fila en Resultado="Error"
            // sin número y sin posibilidad de generarlo. Ahora el número de expediente se calcula
            // igual que cualquier otra fila (automático si no es duplicado, o vía "Decisión de número"
            // si sí lo es), y el dato incompleto solo se refleja como observación — ver priorización
            // de estadoValidacion más abajo (Duplicado/Grupo familiar tienen prioridad sobre "dato
            // incompleto" genérico, pedido explícito del usuario 18/08/2026).
            boolean sgdBloqueante = !hasText(item.getNumeroExpedienteSgd());
            String claveSgd = clave(item.getNumeroExpedienteSgd());
            Integer cantidadSgd = hasText(claveSgd) ? numeroSgd.get(claveSgd) : null;
            if (cantidadSgd != null && cantidadSgd.intValue() > 1) {
                Integer primeraFilaSgd = primeraFilaNumeroSgd.get(claveSgd);
                if (primeraFilaSgd == null || item.getFila() != primeraFilaSgd.intValue()) {
                    item.agregarMensaje("N° expediente SGD repetido en el archivo. Ya existe una primera ocurrencia en la fila "
                            + primeraFilaSgd + ". Debe ser único.");
                    sgdBloqueante = true;
                }
            }
            String duplicadoSgdBd = duplicadosSgdBase.get(item.getFila());
            if (hasText(duplicadoSgdBd)) {
                item.agregarMensaje("N° expediente SGD ya está registrado en " + duplicadoSgdBd + ". Debe ser único.");
                sgdBloqueante = true;
            }

            boolean requiereDecisionAsignacionNumero =
                    ProcedimientoRegistralRules.requiereDecisionAsignacionParaNumero(item.getTipoProcedimiento());
            if (requiereDecisionAsignacionNumero) {
                item.agregarMensaje(ProcedimientoRegistralRules.mensajeSinNumeroRecepcion());
            }

            if (sgdBloqueante) {
                item.setNumeroExpedienteGenerado(null);
                item.setMotivoSinNumero(null);
                item.setListoParaRegistrar(false);
                item.setEstadoValidacion("Error");
                continue;
            }
            if (item.isPosibleDuplicado() || requiereDecisionAsignacionNumero) {
                item.setNumeroExpedienteGenerado(null);
                item.setMotivoSinNumero(item.isPosibleDuplicado()
                        ? "duplicado"
                        : ProcedimientoRegistralRules.etiquetaSinNumero());
            } else {
                // El número real se asigna después de este `for`, ya ordenado por Fecha de
                // solicitud — acá solo se marca la fila como elegible (ver declaración de
                // `elegiblesParaNumero` más arriba).
                item.setNumeroExpedienteGenerado(null);
                item.setMotivoSinNumero(null);
                elegiblesParaNumero.add(item);
            }
            item.setListoParaRegistrar(true);
            // Estados uniformizados y priorizados (18/08/2026, pedido explícito del usuario):
            // "Error" queda reservado EXCLUSIVAMENTE para lo realmente bloqueante (sgdBloqueante,
            // arriba, con "continue"). Entre lo que no bloquea, "Duplicado" (Potencial duplicado)
            // tiene la máxima prioridad — incluso por encima de un dato incompleto genérico (ej. N°
            // Documento faltante) — seguido de "Con observaciones", que agrupa todo lo demás que
            // merece atención sin ser bloqueante ni duplicado: dato incompleto en cualquier columna,
            // Posible grupo familiar (mutuamente excluyente con Duplicado por diseño, ver el guard
            // "!item.isPosibleDuplicado()" antes de detectarlo más arriba) y cualquier otro mensaje
            // acumulado. "Válido" solo cuando no queda ningún mensaje.
            if (item.isPosibleDuplicado()) {
                item.setEstadoValidacion("Duplicado");
            } else if (requiereDecisionAsignacionNumero || error || hasText(item.getMensajeValidacion())) {
                item.setEstadoValidacion("Con observaciones");
            } else {
                item.setEstadoValidacion("Válido");
                item.setMensajeValidacion("Listo para registrar.");
            }
        }

        // Segundo paso: asigna el número de expediente real en orden de Fecha de solicitud, NO en
        // el orden del Excel (18/08/2026, pedido explícito del usuario) — ordena las filas elegibles
        // ascendente por `fechaRecepcion` (la fila con la fecha más antigua recibe el correlativo más
        // bajo); filas sin fecha válida (no debería ocurrir, ya que es obligatoria, pero se cubre
        // igual de forma defensiva) quedan al final, y entre fechas iguales se conserva el orden
        // original del archivo (`getFila()`) para que el resultado sea determinístico.
        elegiblesParaNumero.sort(
                Comparator.comparing(CargaDiariaPreviewDTO::getFechaRecepcion, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingInt(CargaDiariaPreviewDTO::getFila));
        int siguienteCorrelativo = expedienteRegistroDAO.obtenerSiguienteCorrelativoExpediente(correlativoExpedienteService.anioActual());
        for (CargaDiariaPreviewDTO item : elegiblesParaNumero) {
            item.setNumeroExpedienteGenerado(correlativoExpedienteService.generarPreliminar(siguienteCorrelativo++));
        }

        return registros;
    }

    /**
     * Identificación automática del tipo de solicitante (14/08/2026, ver v3/CLAUDE.md, iniciativa
     * "Solicitante con 3 tipos" — rediseñada el mismo día, pedido explícito del usuario: "para la
     * carga diaria solo necesitamos el nombre de la oficina... la plantilla debe seguir usando
     * ÚNICAMENTE la columna SOLICITADO POR"). No hay columna de Excel para el tipo: se deriva de
     * "SOLICITADO POR" ({@code item.getRemitente()}) por CONTENCIÓN normalizada (no coincidencia
     * exacta, no fuzzy/similitud) — el texto de esa columna puede traer palabras alrededor del
     * nombre real de la oficina (ej. "OR Chiclayo - mesa de partes"), así que se evalúa si CONTIENE
     * la denominación de algún centro/unidad, no si es igual a ella.
     *
     * <p>Prioridad: primero Centro de Atención, luego Unidad Orgánica (pedido explícito del
     * usuario); si ninguna de las 2 tablas maestras matchea, el tipo queda CIUDADANO por defecto y
     * {@code remitente} se conserva exactamente como fue ingresado, sin ninguna alteración (nunca se
     * sobrescribe con la versión normalizada usada solo para comparar) — ese caso sigue representando
     * el nombre real de una persona. Si varias denominaciones son substring del texto a la vez
     * (nombres cortos genéricos), se queda con la más larga/específica, para reducir el riesgo de un
     * falso positivo por un fragmento corto.</p>
     *
     * <p><b>"Solicitado por" como equivalencia (14/08/2026, 3ra ronda, pedido explícito del
     * usuario)</b>: cuando SÍ se identifica Centro de Atención o Unidad Orgánica, {@code remitente}
     * se SOBREESCRIBE con el nombre real/actual de la tabla maestra ({@code denominacion}/
     * {@code nombre}) — antes de este cambio la grilla de previsualización seguía mostrando el texto
     * crudo del Excel ("OR San Martín de Porres - Nunura...") aunque el tipo ya se hubiera
     * identificado correctamente, lo que no dejaba ver la equivalencia real. Idempotente: si se
     * vuelve a ejecutar {@code validar()} sobre un ítem ya reescrito, el nombre canónico sigue
     * conteniéndose a sí mismo, así que la clasificación no cambia entre corridas sucesivas.</p>
     *
     * <p>Nunca bloquea el registro de la fila: a diferencia de otras validaciones de este método,
     * la clasificación siempre resuelve a alguno de los 3 tipos válidos, así que no hay un estado de
     * "tipo no reconocido" que corregir manualmente.</p>
     */
    /**
     * Alias/sinónimos específicos (14/08/2026, segunda ronda de depuración contra
     * docs/arquitectura_app/datos_casos_reales.xlsx, pedido explícito del usuario — decisión: "NO
     * corrijas el nombre en la tabla maestra... agrega una regla de matching", "lo que consideres
     * más mantenible sin tocar el dato maestro"). Cada par {alias, canónico} se aplica como
     * reemplazo de texto sobre el haystack ya normalizado con {@link #claveTolerante(String)} — no
     * toca `CENTROS_ATENCION`/`UNIDADES_ORGANICAS`, es puramente una equivalencia de comparación:
     * <ul>
     *   <li>"TECNICO NORMATIVA" -&gt; "TECNICA NORMATIVA": error de redacción real y consistente en
     *   producción (8 casos), el dato maestro correcto sigue siendo "Técnica" (femenino).</li>
     *   <li>"OFICINA REGISTRAL" -&gt; "OR": mismo nombre antiguo que "OR", solo escrito sin abreviar.</li>
     *   <li>"CERCADO DE LIMA" -&gt; "LIMA": alias coloquial, no existe como entidad propia en
     *   CENTROS_ATENCION (solo existe "OR LIMA"/"Centro de Atención Lima").</li>
     *   <li>"RECURSOS REGISTRALES" -&gt; "RECURSOS REGISTRALES CIVILES": la mayoría de los casos
     *   reales omiten "CIVILES" al escribir el nombre de esta unidad orgánica (agregada como id=8
     *   en `unidades_organicas`, ver v3/CLAUDE.md).</li>
     * </ul>
     * <p>14/08/2026, tercera ronda (Fase 3 de Municipalidad, depuración contra
     * docs/arquitectura_app/data_casos_reales_municipalidades.xlsx, 152 casos reales, pedido
     * explícito del usuario, decisiones confirmadas tras reportar 76.3% de acierto inicial):</p>
     * <ul>
     *   <li>"DISRITAL" -&gt; "DISTRITAL": typo real y consistente (falta la T), 7 casos reales, todos
     *   "Municipalidad Distrital de Puinahua" (sí existe en el catálogo). Con "DISTRITAL" ya
     *   tolerado como stopword (ver {@link #STOPWORDS_TIPO_SOLICITANTE}), el efecto neto es que
     *   "DISRITAL" desaparece del haystack igual que si estuviera bien escrito.</li>
     *   <li>"PROV" -&gt; "PROVINCIAL": abreviación real, 2 casos ("Municipalidad Prov de Vilcas
     *   Huaman"). Riesgo aceptado explícitamente por el usuario: al ser un reemplazo de substring
     *   sobre el haystack ya concatenado (no por token), un texto que contuviera "PROV" dentro de
     *   otra palabra (ej. "PROVEEDOR") también se vería afectado — mismo mecanismo que ya usan los
     *   otros 4 alias, sin blindaje adicional por límite de palabra.</li>
     *   <li>"TOTO" -&gt; "TOTOS": 1 caso real (falta la S final de "Municipalidad Distrital de
     *   Totos"). Alias específico y angosto (no una regla genérica de "agregar S"), mismo criterio
     *   que "CERCADO DE LIMA" -&gt; "LIMA".</li>
     * </ul>
     */
    private static final String[][] ALIASES_TIPO_SOLICITANTE = {
        {"TECNICO NORMATIVA", "TECNICA NORMATIVA"},
        {"OFICINA REGISTRAL", "OR"},
        {"CERCADO DE LIMA", "LIMA"},
        {"RECURSOS REGISTRALES", "RECURSOS REGISTRALES CIVILES"},
        {"DISRITAL", "DISTRITAL"},
        {"PROV", "PROVINCIAL"},
        {"TOTO", "TOTOS"},
    };

    /** "OFICINA REGIONAL {núm} - {CIUDAD} - {persona}" -> extrae {CIUDAD} (ver {@link #clasificarTipoSolicitante}). */
    private static final java.util.regex.Pattern PATRON_OFICINA_REGIONAL =
            java.util.regex.Pattern.compile("OFICINA\\s+REGIONAL\\s+\\d+\\s*-\\s*([^-]+?)\\s*-");
    private static final java.util.regex.Pattern PATRON_OFICINA_REGIONAL_SIN_SEGUNDO_GUION =
            java.util.regex.Pattern.compile("OFICINA\\s+REGIONAL\\s+\\d+\\s*-\\s*(.+)$");

    private void clasificarTipoSolicitante(
            CargaDiariaPreviewDTO item,
            List<CentroAtencionDTO> centrosAtencion,
            List<UnidadOrganicaDTO> unidadesOrganicas,
            List<MunicipalidadDTO> municipalidades) {
        // 18/08/2026, pedido explícito del usuario (combo editable de "Tipo solicitante" en la
        // previsualización): si el usuario ya corrigió manualmente esta fila, no se recalcula — de lo
        // contrario el siguiente "Validar" pisaría la elección manual con la clasificación automática
        // de nuevo, ya que este método corre incondicionalmente en cada validación.
        if (item.isTipoSolicitanteManual()) {
            return;
        }
        item.setTipoSolicitante("CIUDADANO");
        item.setIdCentroAtencion(null);
        item.setCentroAtencionDenominacion(null);
        item.setIdUnidadOrganica(null);
        item.setUnidadOrganicaNombre(null);
        item.setIdMunicipalidad(null);
        item.setMunicipalidadDenominacion(null);

        if (!hasText(item.getRemitente())) {
            return;
        }
        String haystack = claveTolerante(item.getRemitente());
        if (!hasText(haystack)) {
            return;
        }
        for (String[] alias : ALIASES_TIPO_SOLICITANTE) {
            String desde = claveTolerante(alias[0]);
            if (hasText(desde) && haystack.contains(desde)) {
                haystack = haystack.replace(desde, claveTolerante(alias[1]));
            }
        }

        // Centro de Atención: compara contra denominación (nombre actual) Y nombre_anterior_res_019
        // (nomenclatura antigua "OR .../Oficina Registral ...", la que de hecho sigue usando la
        // mayoría del texto real en "SOLICITADO POR" — hallazgo real al depurar contra
        // datos_casos_reales.xlsx, pedido explícito del usuario). Si matchea por cualquiera de las
        // 2, se persiste igual la `denominacion` (nombre actual/oficial), nunca el nombre antiguo.
        CentroAtencionDTO mejorCentro = null;
        String mejorCentroClave = null;
        for (CentroAtencionDTO centro : centrosAtencion) {
            for (String campo : new String[] {centro.denominacion(), centro.nombreAnteriorRes019()}) {
                String candidato = claveTolerante(campo);
                if (hasText(candidato) && haystack.contains(candidato)
                        && (mejorCentroClave == null || candidato.length() > mejorCentroClave.length())) {
                    mejorCentro = centro;
                    mejorCentroClave = candidato;
                }
            }
        }
        if (mejorCentro != null) {
            item.setTipoSolicitante("CENTRO_ATENCION");
            item.setIdCentroAtencion(mejorCentro.idCentroAtencion());
            item.setCentroAtencionDenominacion(mejorCentro.denominacion());
            // "Solicitado por" pasa a mostrar el nombre real/actual del centro (equivalencia),
            // pedido explícito del usuario (14/08/2026, 3ra ronda): "la idea era que ya muestre...
            // el nombre real de la tabla centros_atenciones". Solo se sobreescribe cuando el tipo SÍ
            // se identificó — el texto tal cual escrito solo se preserva para CIUDADANO (fallback),
            // que sigue siendo el nombre real de una persona, no de una oficina.
            item.setRemitente(mejorCentro.denominacion());
            return;
        }

        // "Oficina Regional {núm} - {Ciudad} - {persona}": se refiere a la oficina regional
        // agrupadora, sin fila propia en CENTROS_ATENCION — se extrae la ciudad y se busca en
        // sentido INVERSO (¿la ciudad está contenida en la denominación/nombre antiguo de algún
        // centro?), a diferencia de la comparación de arriba. Acotado a este patrón estructural
        // específico (no es una regla general) para no arriesgar falsos positivos con textos cortos.
        CentroAtencionDTO centroPorCiudad = buscarCentroPorCiudadOficinaRegional(item.getRemitente(), centrosAtencion);
        if (centroPorCiudad != null) {
            item.setTipoSolicitante("CENTRO_ATENCION");
            item.setIdCentroAtencion(centroPorCiudad.idCentroAtencion());
            item.setCentroAtencionDenominacion(centroPorCiudad.denominacion());
            item.setRemitente(centroPorCiudad.denominacion());
            return;
        }

        // Unidad Orgánica: compara contra nombre Y glosa (siglas de la dirección/sub dirección, ej.
        // "SDPRC", agregada por el usuario en la tabla y expuesta por el script 107). Mismo patrón
        // de "candidato más largo gana" que Centro de Atención con denominacion/nombre_anterior_res_019.
        // Si matchea por la glosa, se persiste igual el `nombre` (nunca las siglas).
        UnidadOrganicaDTO mejorUnidad = null;
        String mejorUnidadClave = null;
        for (UnidadOrganicaDTO unidad : unidadesOrganicas) {
            for (String campo : new String[] {unidad.nombre(), unidad.glosa()}) {
                String candidato = claveTolerante(campo);
                if (hasText(candidato) && haystack.contains(candidato)
                        && (mejorUnidadClave == null || candidato.length() > mejorUnidadClave.length())) {
                    mejorUnidad = unidad;
                    mejorUnidadClave = candidato;
                }
            }
        }
        if (mejorUnidad != null) {
            item.setTipoSolicitante("UNIDAD_ORGANICA");
            item.setIdUnidadOrganica(mejorUnidad.idUnidadOrganica());
            item.setUnidadOrganicaNombre(mejorUnidad.nombre());
            item.setRemitente(mejorUnidad.nombre());
            return;
        }

        // Municipalidad (14/08/2026, Fase 3, ver v3/CLAUDE.md): último intento antes de caer a
        // Ciudadano, orden pedido explícito del usuario (Centro de Atención -> Unidad Orgánica ->
        // Municipalidad -> Ciudadano). Misma mecánica de contención + "el candidato más largo gana"
        // que Centro/Unidad, comparando únicamente contra `denominacion` (la única columna calculada
        // en la Fase 1 de esta iniciativa; MUNICIPALIDADES no tiene un equivalente a
        // nombre_anterior_res_019). 22 de 152 casos reales (14.5%) quedan sin identificar pese a
        // esta regla — verificados uno por uno contra denominacion/departamento/provincia/distrito/
        // centro_poblado: son lugares genuinamente ausentes del catálogo (ej. "Sumbilca", "Aurahua",
        // "Pastaza"), no un problema de matching. Decisión explícita del usuario: se dejan así por
        // ahora (caen a CIUDADANO), sin agregar filas a MUNICIPALIDADES en este incremento.
        MunicipalidadDTO mejorMunicipalidad = null;
        String mejorMunicipalidadClave = null;
        for (MunicipalidadDTO municipalidad : municipalidades) {
            String candidato = claveTolerante(municipalidad.denominacion());
            if (hasText(candidato) && haystack.contains(candidato)
                    && (mejorMunicipalidadClave == null || candidato.length() > mejorMunicipalidadClave.length())) {
                mejorMunicipalidad = municipalidad;
                mejorMunicipalidadClave = candidato;
            }
        }
        if (mejorMunicipalidad != null) {
            item.setTipoSolicitante("MUNICIPALIDAD");
            item.setIdMunicipalidad(mejorMunicipalidad.idMunicipalidad());
            item.setMunicipalidadDenominacion(mejorMunicipalidad.denominacion());
            item.setRemitente(mejorMunicipalidad.denominacion());
        }
    }

    private CentroAtencionDTO buscarCentroPorCiudadOficinaRegional(String remitente, List<CentroAtencionDTO> centrosAtencion) {
        String mayusculas = Normalizer.normalize(remitente.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        java.util.regex.Matcher m = PATRON_OFICINA_REGIONAL.matcher(mayusculas);
        String ciudad = m.find() ? m.group(1).trim() : null;
        if (ciudad == null) {
            java.util.regex.Matcher m2 = PATRON_OFICINA_REGIONAL_SIN_SEGUNDO_GUION.matcher(mayusculas);
            ciudad = m2.find() ? m2.group(1).trim() : null;
        }
        String ciudadClave = hasText(ciudad) ? claveTolerante(ciudad) : null;
        if (!hasText(ciudadClave)) {
            return null;
        }
        CentroAtencionDTO mejor = null;
        Integer mejorLongitud = null;
        for (CentroAtencionDTO centro : centrosAtencion) {
            for (String campo : new String[] {centro.denominacion(), centro.nombreAnteriorRes019()}) {
                String candidato = claveTolerante(campo);
                if (hasText(candidato) && candidato.contains(ciudadClave)
                        && (mejorLongitud == null || candidato.length() < mejorLongitud)) {
                    mejor = centro;
                    mejorLongitud = candidato.length();
                }
            }
        }
        return mejor;
    }

    /**
     * Marcador de "dato no provisto en el Excel" para los 4 campos de identidad (tipo/número de
     * solicitante y titular) — 18/08/2026, pedido explícito del usuario: estos 4 campos dejan de ser
     * obligatorios en Carga diaria (no forman parte de la lista de campos obligatorios), así que en
     * vez de marcar la fila como incompleta se autocompletan con este valor. Nunca dispara una
     * validación de formato (ver {@link #validarDocumento}, que corta apenas ve este valor en
     * cualquiera de los 2 campos del par).
     */
    private static final String NO_DEFINIDO = "No Definido";

    /**
     * Marcador para "no corresponde documento de identidad" (18/08/2026, pedido explícito del
     * usuario) — cuando el Tipo Solicitante (ya resuelto por {@link #clasificarTipoSolicitante},
     * que corre ANTES que este método en el bucle principal de {@link #validar}) es Centro de
     * Atención, Unidad Orgánica o Municipalidad, el "solicitante" es una entidad, no una persona:
     * Tipo y N° de documento de identidad del solicitante se guardan como este valor fijo, sin pasar
     * por el autocompletado "No Definido" ni por ninguna validación de formato.
     */
    private static final String NO_APLICA = "NO APLICA";

    private List<String> validarIdentidad(CargaDiariaPreviewDTO item) {
        boolean solicitanteInstitucional =
                hasText(item.getTipoSolicitante()) && !"CIUDADANO".equals(item.getTipoSolicitante());

        if (solicitanteInstitucional) {
            item.setTipoDocumentoIdentidadSolicitante(NO_APLICA);
            item.setNumeroDocumentoIdentidadSolicitante(NO_APLICA);
        } else {
            // "SIN DNI" nunca debe quedar como valor final del solicitante (18/08/2026, pedido
            // explícito del usuario: "no debe... aparecer como SIN DNI... debería mostrar mejor NO
            // DEFINIDO") — a diferencia del titular (que sí conserva el atajo "SIN DNI" de siempre,
            // ver más abajo), cualquier ocurrencia de "SIN DNI" en Tipo o N° del solicitante (ya sea
            // escrita en el Excel o el resultado de una edición manual previa) se limpia ANTES del
            // autocompletado normal, para que caiga en el mismo camino que "sin dato" y termine en
            // "No Definido" en vez de "SIN DNI".
            if ("SIN DNI".equalsIgnoreCase(trim(item.getTipoDocumentoIdentidadSolicitante()))) {
                item.setTipoDocumentoIdentidadSolicitante(null);
            }
            if ("SIN DNI".equalsIgnoreCase(trim(item.getNumeroDocumentoIdentidadSolicitante()))) {
                item.setNumeroDocumentoIdentidadSolicitante(null);
            }
            // Tipo de documento de identidad del solicitante: ya no es obligatorio (18/08/2026,
            // pedido explícito del usuario) — se autocompleta con "No Definido" cuando falta.
            if (!hasText(item.getTipoDocumentoIdentidadSolicitante())) {
                item.setTipoDocumentoIdentidadSolicitante(NO_DEFINIDO);
            }
        }

        // Titular es siempre una persona (sin cambios de este pedido): mismo atajo "SIN DNI" +
        // autocompletado "No Definido" que ya existía.
        if (!hasText(item.getTipoDocumentoIdentidadTitular())
                && "SIN DNI".equalsIgnoreCase(trim(item.getNumeroDocumentoIdentidadTitular()))) {
            item.setTipoDocumentoIdentidadTitular("SIN DNI");
        }
        if (!hasText(item.getTipoDocumentoIdentidadTitular())) {
            item.setTipoDocumentoIdentidadTitular(NO_DEFINIDO);
        }

        // Si el solicitante es la misma persona que el titular (mismo nombre, comparación exacta
        // normalizada — igual criterio que claveActaTitular/claveTitularExacto de este mismo
        // archivo), se copia el N° de documento del solicitante al titular cuando el titular no
        // trae el suyo propio (18/08/2026, pedido explícito del usuario). No aplica si el
        // solicitante es institucional (ya quedó en "NO APLICA", sin número real que copiar) — el
        // guard `!solicitanteInstitucional` cubre ese caso; el resto de condiciones queda igual.
        if (!solicitanteInstitucional
                && !hasText(item.getNumeroDocumentoIdentidadTitular())
                && hasText(item.getNumeroDocumentoIdentidadSolicitante())
                && !NO_DEFINIDO.equalsIgnoreCase(trim(item.getNumeroDocumentoIdentidadSolicitante()))
                && hasText(item.getRemitente())
                && hasText(item.getTitular())
                && clave(item.getRemitente()).equals(clave(item.getTitular()))) {
            item.setNumeroDocumentoIdentidadTitular(item.getNumeroDocumentoIdentidadSolicitante());
        }

        // N° de documento de identidad: mismo criterio que el Tipo — solicitante solo si NO es
        // institucional (ya tiene "NO APLICA"), titular siempre.
        if (!solicitanteInstitucional && !hasText(item.getNumeroDocumentoIdentidadSolicitante())) {
            item.setNumeroDocumentoIdentidadSolicitante(NO_DEFINIDO);
        }
        if (!hasText(item.getNumeroDocumentoIdentidadTitular())) {
            item.setNumeroDocumentoIdentidadTitular(NO_DEFINIDO);
        }

        // DNI con menos de 8 dígitos numéricos: se rellena con ceros a la izquierda (18/08/2026,
        // pedido explícito del usuario) — antes de validar el formato final. Sin efecto sobre
        // "NO APLICA"/"No Definido"/"SIN DNI" (normalizarNumeroDni ya los deja intactos, no son
        // solo-dígitos).
        if (!solicitanteInstitucional) {
            item.setNumeroDocumentoIdentidadSolicitante(
                    normalizarNumeroDni(item.getTipoDocumentoIdentidadSolicitante(), item.getNumeroDocumentoIdentidadSolicitante()));
        }
        item.setNumeroDocumentoIdentidadTitular(
                normalizarNumeroDni(item.getTipoDocumentoIdentidadTitular(), item.getNumeroDocumentoIdentidadTitular()));

        List<String> errores = new ArrayList<String>();
        if (!solicitanteInstitucional) {
            validarDocumento(
                    errores,
                    "solicitante",
                    item.getTipoDocumentoIdentidadSolicitante(),
                    item.getNumeroDocumentoIdentidadSolicitante(),
                    true);
        }
        validarDocumento(
                errores,
                "titular",
                item.getTipoDocumentoIdentidadTitular(),
                item.getNumeroDocumentoIdentidadTitular(),
                false);
        return errores;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /** Si `tipo` es DNI y `numero` son solo dígitos con menos de 8, rellena con ceros a la izquierda. */
    private String normalizarNumeroDni(String tipo, String numero) {
        if (!hasText(tipo) || !hasText(numero) || !"DNI".equalsIgnoreCase(tipo.trim())) {
            return numero;
        }
        String num = numero.trim();
        if (num.matches("\\d{1,7}")) {
            return String.format("%08d", Long.parseLong(num));
        }
        return numero;
    }

    private void validarDocumento(List<String> errores, String etiqueta, String tipo, String numero, boolean permiteRuc) {
        // Resguardo defensivo: con el autocompletado de validarIdentidad, tipo/número de identidad
        // ya no deberían llegar vacíos — si de todas formas ocurriera, no se agrega ningún mensaje
        // de "obligatorio" (18/08/2026, pedido explícito del usuario: dejaron de ser campos
        // obligatorios de Carga diaria).
        if (!hasText(tipo) || !hasText(numero)) {
            return;
        }
        String tipoNormalizado = tipo.trim().toUpperCase(Locale.ROOT);
        String numeroNormalizado = numero.trim().toUpperCase(Locale.ROOT);
        // "No Definido" (autocompletado cuando el dato no venía en el Excel) y "NO APLICA"
        // (solicitante institucional, ver `validarIdentidad`) nunca deben generar un error de
        // formato — si CUALQUIERA de los 2 campos del par quedó así, se omite la validación de
        // formato completa para ese par. Resguardo defensivo para "NO APLICA": `validarIdentidad` ya
        // no llama a este método para el solicitante cuando es institucional, pero se deja el chequeo
        // igual por si se reutiliza este método desde otro punto en el futuro.
        if (NO_DEFINIDO.equalsIgnoreCase(tipoNormalizado) || NO_DEFINIDO.equalsIgnoreCase(numeroNormalizado)
                || NO_APLICA.equalsIgnoreCase(tipoNormalizado) || NO_APLICA.equalsIgnoreCase(numeroNormalizado)) {
            return;
        }
        if ("SIN DNI".equals(tipoNormalizado)) {
            if (!"SIN DNI".equalsIgnoreCase(numero.trim())) {
                errores.add("Si el tipo de documento del " + etiqueta + " es SIN DNI, el número debe quedar vacío.");
            }
            return;
        }
        if ("RUC".equals(tipoNormalizado) && !permiteRuc) {
            errores.add("El titular no permite RUC como tipo de documento de identidad.");
            return;
        }
        // Límites de formato por tipo de documento (18/08/2026, pedido explícito del usuario): DNI
        // exactamente 8 dígitos numéricos (el rellenado con ceros ya corrió antes, ver
        // `normalizarNumeroDni`); RUC exactamente 11 dígitos numéricos; CE alfanumérico de hasta 15
        // caracteres (antes compartía el límite de 12 con Pasaporte, ahora con su propio límite más
        // amplio); Pasaporte alfanumérico de hasta 12 caracteres.
        if ("DNI".equals(tipoNormalizado)) {
            if (!numeroNormalizado.matches("\\d{8}")) {
                errores.add("DNI del " + etiqueta + " debe tener 8 caracteres numéricos.");
            }
            return;
        }
        if ("RUC".equals(tipoNormalizado)) {
            if (!numeroNormalizado.matches("\\d{11}")) {
                errores.add("RUC del " + etiqueta + " debe tener 11 caracteres numéricos.");
            }
            return;
        }
        if ("CE".equals(tipoNormalizado)) {
            if (!numeroNormalizado.matches("[A-Z0-9]{1,15}")) {
                errores.add("CE del " + etiqueta + " debe ser alfanumérico de hasta 15 caracteres.");
            }
            return;
        }
        if ("PASAPORTE".equals(tipoNormalizado)) {
            if (!numeroNormalizado.matches("[A-Z0-9]{1,12}")) {
                errores.add("PASAPORTE del " + etiqueta + " debe ser alfanumérico de hasta 12 caracteres.");
            }
            return;
        }
        errores.add("Tipo de documento de identidad del " + etiqueta + " no reconocido: " + tipo + ".");
    }

    private void sumarClave(Map<String, Integer> contador, String key) {
        if (!hasText(key)) {
            return;
        }
        Integer actual = contador.get(key);
        contador.put(key, actual == null ? 1 : actual + 1);
    }

    private String claveActaTitular(CargaDiariaPreviewDTO item) {
        if (item == null || !hasText(item.getNumeroActa()) || !hasText(item.getTitular())) {
            return null;
        }
        return clave(item.getNumeroActa()) + "|" + clave(item.getTitular());
    }

    private String claveApellidosTitular(CargaDiariaPreviewDTO item) {
        if (item == null) {
            return null;
        }
        return grupoFamiliarHeuristicaService.claveApellidosTitular(item.getTitular());
    }

    private String claveTitularExacto(CargaDiariaPreviewDTO item) {
        if (item == null) {
            return null;
        }
        return grupoFamiliarHeuristicaService.normalizarTitular(item.getTitular());
    }

    private String clave(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        return normalized.replaceAll("[^A-Z0-9]", "");
    }

    /**
     * Variante de {@link #clave(String)} SOLO para {@link #clasificarTipoSolicitante} (no se toca
     * {@code clave()} en sí para no alterar la dedup de acta/titular/SGD, que debe seguir siendo
     * exacta). Además de tildes/mayúsculas/no-alfanumérico, ignora artículos/preposiciones cortas
     * ("DE", "DEL", "LA", "LAS", "EL", "LOS", "Y") — encontrado al depurar contra
     * docs/arquitectura_app/datos_casos_reales.xlsx (14/08/2026): la denominación real de
     * "Sub Dirección de Procesamiento de REGISTROS CIVILES" a veces se escribe con un "LOS" de más
     * ("...de LOS Registros Civiles"), y una comparación por contención estricta de caracteres
     * pierde ese caso aunque el nombre sea reconociblemente el mismo.
     *
     * <p>14/08/2026, Fase 3 de Municipalidad: "DISTRITAL"/"PROVINCIAL" se agregaron al mismo
     * conjunto (no es un alias puntual de una municipalidad, aplica a las 1652 filas del catálogo
     * por igual). Hallazgo al depurar contra data_casos_reales_municipalidades.xlsx: el texto real
     * de "SOLICITADO POR" casi siempre omite ese calificador administrativo ("MUNICIPALIDAD DE
     * ALTO SELVA ALEGRE" en vez de "Municipalidad DISTRITAL de Alto Selva Alegre"), mientras que
     * `MUNICIPALIDADES.denominacion` siempre lo incluye por el patrón de concatenación de la Fase
     * 1 — sin tolerarlo, esa palabra de más rompe la contención aunque el resto del nombre calce
     * exacto. Inocuo para Centro de Atención/Unidad Orgánica: ninguna de sus denominaciones
     * contiene esas 2 palabras.</p>
     */
    private static final java.util.Set<String> STOPWORDS_TIPO_SOLICITANTE =
            new java.util.HashSet<>(java.util.Arrays.asList(
                    "DE", "DEL", "LA", "LAS", "EL", "LOS", "Y", "DISTRITAL", "PROVINCIAL"));

    private String claveTolerante(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        StringBuilder resultado = new StringBuilder();
        for (String token : normalized.split("[^A-Z0-9]+")) {
            if (!token.isEmpty() && !STOPWORDS_TIPO_SOLICITANTE.contains(token)) {
                resultado.append(token);
            }
        }
        return resultado.toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
