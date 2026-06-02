# AGENTS.md - Reglas persistentes del proyecto SDRERC

Este archivo es la fuente principal de reglas operativas para sesiones futuras de Codex o Codex CLI en el proyecto SDRERC.

Ruta del proyecto:

```text
D:\2026\FuentesRENIEC\sdrerc_CODIGOS
```

## 1. Rol del agente

- Actuar como agente principal de desarrollo para SDRERC.
- Trabajar de forma autonoma, ordenada y verificable.
- Priorizar cambios pequenos, incrementales y compilables.
- Leer el codigo y la documentacion existente antes de asumir detalles importantes.
- Mantener foco en la tarea solicitada y evitar refactors no pedidos.

## 2. Contexto persistente

- Usar `AGENTS.md` como fuente principal de reglas del proyecto.
- Usar `docs/arquitectura_app/*.md`, prompts e informes como contexto tecnico cuando aplique.
- Usar `/resume`, `/compact` y `/review` cuando ayuden a conservar continuidad y ahorrar contexto/tokens.
- No repetir contexto largo salvo que sea necesario para decidir, validar o explicar un bloqueo.
- Antes de pedir contexto al usuario, revisar archivos locales relevantes del proyecto.

## 3. Reglas SDRERC obligatorias

- Mantener SDRERC V2 separada de la app legacy.
- No tocar `src/main/java/com/sdrerc/infrastructure/database/OracleConnection.java` salvo autorizacion explicita.
- No tocar `FrmLogin.java` legacy salvo autorizacion explicita.
- No tocar `MenuPrincipal.java` legacy salvo autorizacion explicita.
- No tocar `com.sdrerc.Main` legacy salvo autorizacion explicita.
- No eliminar codigo legacy.
- No ejecutar SQL salvo autorizacion explicita.
- No modificar datos de BD salvo autorizacion explicita.
- No imprimir ni documentar passwords reales.
- No crear etapa visual `VALIDACION`.
- Mantener Java 8 y Swing.
- Mantener FlatLaf/AppUiConfig si se reutiliza sin afectar legacy.

## 4. Reglas de SDRERC V2

- Avanzar modulos V2 dentro de la nueva arquitectura.
- Mantener la App SDRERC V2 separada de la app legacy.
- V2 puede evolucionar visualmente en paquetes `appv2` y `expedienteconsola` cuando la tarea lo pida.
- Usar `SDRERC_APP` mediante la conexion paralela `SdrercAppConnection`.
- No modificar la conexion global legacy.
- Consultar SDRERC_APP mediante DAOs y Services; no poner SQL en formularios Swing.
- Usar vistas y DAOs de lectura cuando el incremento sea solo consulta.
- Por defecto, V2 es lectura/consulta.
- No implementar `INSERT`, `UPDATE`, `DELETE`, `MERGE`, `executeUpdate` ni movimientos reales de flujo salvo autorizacion explicita.
- No implementar escrituras o movimientos reales de flujo salvo que la tarea lo pida explicitamente.
- No usar IDs hardcodeados.
- Mantener UI moderna tipo Service Console / Case Management.
- Mantener nombres visuales amigables para etapas y estados; no mostrar codigos tecnicos al usuario final cuando exista nombre amigable.
- No crear etapa visual `VALIDACION`.

## 5. Reglas de SQL y BD

- No ejecutar SQL sin autorizacion explicita.
- No modificar scripts SQL sin autorizacion explicita.
- No modificar datos de BD sin autorizacion explicita.
- Scripts de prueba o patch solo deben crearse/modificarse si la tarea lo pide.
- No reejecutar scripts base sobre una BD existente salvo autorizacion explicita.
- No usar `DROP`, `DELETE`, `TRUNCATE`, `INSERT`, `UPDATE` o `MERGE` en scripts o codigo salvo autorizacion explicita y alcance claro.
- Si la tarea autoriza crear un script SQL, hacerlo idempotente cuando sea posible y no ejecutarlo salvo instruccion separada.

## 6. Seguridad y credenciales

- No imprimir credenciales en respuestas finales.
- No documentar passwords reales.
- Si aparece un password real en documentacion editable dentro del alcance, reemplazarlo por `[REDACTADO]`.
- Si aparece un password real en codigo legacy que no fue autorizado tocar, no modificarlo; reportarlo brevemente.
- No pedir, exponer ni inferir credenciales si no son necesarias para la tarea.

## 7. Reglas de git

- Al terminar cada tarea, revisar `git status`.
- No usar `git add .`.
- Agregar solo archivos creados/modificados por la tarea.
- No revertir cambios ajenos.
- Si hay cambios previos no relacionados, reportarlos brevemente y dejarlos fuera del commit.
- No hacer commit/push automatico si existen cambios no relacionados o preexistentes que puedan mezclarse con la tarea.
- No pedir el mensaje de commit al inicio.
- Sugerir un mensaje de commit al final segun los cambios realizados.
- No ejecutar `git commit` ni `git push` salvo autorizacion clara del usuario o tarea explicita que lo pida.
- Solo pedir confirmacion si hay conflictos, cambios ajenos riesgosos, comandos destructivos, restricciones del entorno o riesgo de incluir archivos no relacionados.
- No mostrar `git diff` completo salvo que se pida explicitamente.

## 8. Autonomia / full access

- Asumir aprobacion para comandos normales de lectura, edicion, compilacion, pruebas y git cuando el entorno lo permita.
- Pedir confirmacion solo ante operaciones destructivas, SQL, datos sensibles, credenciales, conflictos de git o cambios fuera de alcance.
- En Codex CLI, se puede usar modo autonomo con:

```powershell
codex --approval never --sandbox workspace-write
```

- Para entornos controlados y de confianza, se puede usar:

```powershell
codex --approval never --sandbox danger-full-access
```

- `danger-full-access` reduce barreras de seguridad. Usarlo solo cuando el repo, la tarea y el entorno sean confiables.
- Aunque el entorno tenga full access, respetar las restricciones del proyecto, especialmente legacy, SQL, credenciales y cambios de BD.

## 9. Ahorro de tokens

- No pegar salidas largas de consola.
- No devolver `git diff` completo salvo pedido explicito.
- Resumir cambios por archivo.
- Resumir errores solo con lo necesario para corregirlos.
- Entregar respuestas finales cortas y accionables.
- Evitar repetir codigo completo si solo se modificaron fragmentos.
- Preferir referencias a archivos y resumen de impacto antes que bloques largos de texto.

## 10. Alcance y forma de trabajo

- Mantener cambios pequenos, incrementales y compilables.
- No hacer refactors amplios no solicitados.
- Antes de modificar, leer archivos relevantes.
- Si la tarea es ambigua, proponer o aplicar el alcance minimo seguro.
- Corregir solo errores atribuibles a la tarea actual.
- No mezclar mejoras oportunistas con la tarea solicitada.

## 11. Compilacion y verificacion

- Ejecutar `mvn clean compile` cuando se modifique Java.
- Ejecutar `mvn clean package` si se modifica App V2, launcher o estructura de ejecucion.
- Si Maven/JDK no esta disponible, reportar el bloqueo y hacer revision estatica.
- Corregir solo errores atribuibles a la tarea actual.
- Para cambios solo Markdown o documentacion, no compilar salvo que la tarea lo pida.

## 12. Flujo estandar de trabajo

1. Leer el contexto minimo necesario.
2. Hacer un plan breve solo si la tarea es amplia.
3. Implementar el cambio solicitado.
4. Compilar/verificar segun aplique.
5. Revisar restricciones de la tarea.
6. Revisar `git status`.
7. Sugerir commit si procede.
8. Hacer commit/push solo con autorizacion clara o tarea explicita.
9. Entregar resumen final.

## 13. Respuesta final esperada

- Responder corto y accionable.
- Listar archivos creados/modificados.
- Resumir la implementacion.
- Indicar resultado de build o verificacion.
- Indicar restricciones cumplidas.
- Indicar cambios ajenos detectados si los hubo.
- Sugerir mensaje de commit, sin hacer commit/push salvo autorizacion.
- Indicar bloqueos concretos si existieron.

## 14. Restricciones por defecto

- No ejecutar SQL sin autorizacion explicita.
- No modificar scripts SQL sin autorizacion explicita.
- No modificar datos de BD sin autorizacion explicita.
- No introducir escrituras en SDRERC V2 salvo que la tarea lo pida.
- No documentar passwords reales.
- No tocar legacy sin autorizacion explicita.

## 15. Vision del sistema SDRERC V2

SDRERC V2 busca ser:

- Una consola moderna de gestion de expedientes registrales.
- Una aplicacion de lectura primero y escritura controlada despues.
- Una interfaz basada en etapas, estados, responsables y acciones permitidas.
- Una herramienta para reducir confusion del usuario final.
- Una app con trazabilidad, historial, documentos, observaciones, notificaciones, publicacion, expediente digital y cierre.

## 16. Modelo mental de usuario

La interfaz debe ayudar al usuario a responder rapidamente:

- Donde esta el expediente.
- Quien lo tiene.
- Que etapa tiene.
- Que estado tiene.
- Que accion sigue.
- Si esta observado.
- Si esta vencido.
- Si tiene documentos pendientes.
- Si requiere notificacion.
- Si requiere publicacion.
- Si tiene expediente digital.
- Cual fue su historial.

## 17. Principios UX/UI obligatorios

- No mostrar codigos tecnicos al usuario si existe nombre amigable.
- No mostrar `ASIGNACION` si puede mostrarse `Asignacion`.
- No mostrar `EN_VERIFICACION` si puede mostrarse `En verificacion`.
- Evitar controles diminutos.
- Usar combos para catalogos cuando sea posible.
- Usar badges para etapa, estado y plazo.
- Usar panel lateral con cards.
- Usar barra visual de etapas.
- Evitar interfaces planas tipo tabla/formulario cuando el flujo requiera contexto.
- Mantener estilo institucional sobrio.
- No copiar logos, colores ni branding de referencias externas.

## 18. Flujo funcional esperado

El flujo visual y funcional base de SDRERC V2 debe respetar estas macroetapas:

- Registro.
- Asignacion.
- Analisis.
- Verificacion.
- Firma / Emision.
- Ejecucion.
- Notificacion.
- Publicacion condicional.
- Expediente digital.
- Cierre / Archivo.

Rutas especiales que deben considerarse en el diseno y en futuros incrementos autorizados:

- Devolucion desde Verificacion a Analisis.
- Correccion y reenvio.
- Reversion desde Ejecucion a Analisis.
- Notificacion virtual.
- Notificacion presencial.
- Cargos de acuse.
- Publicacion.
- Expediente digital.

## 19. Arquitectura visual de la consola

La Consola Expediente V2 debe tender a una arquitectura tipo Service Console / Case Management con:

- Header ejecutivo del expediente.
- Datos clave visibles.
- Barra visual de etapas.
- Pestanas de detalle.
- Seccion de documentos.
- Timeline / historial.
- Panel lateral de resumen.
- Acciones permitidas informativas hasta que exista autorizacion explicita de escritura.
- Diseno orientado a contexto, trazabilidad y decision rapida.

## 20. Criterios de aceptacion visual

- No debe haber campos superpuestos.
- No debe haber controles diminutos.
- Etapas y estados deben verse con nombres amigables.
- La barra de etapas debe ser legible.
- El panel lateral debe tener cards separadas y claras.
- La bandeja debe tener filtros entendibles.
- Los combos deben usarse para etapa y estado cuando corresponda.
- La consola debe mostrar contexto suficiente sin saturar.
- La UI debe ser sobria, institucional y moderna.

## 21. Criterios de aceptacion tecnica

- Build exitoso con `mvn clean compile` cuando se modifique Java.
- Ejecutar `mvn clean package` cuando se modifique App V2, launcher o estructura de ejecucion.
- App legacy intacta.
- Sin SQL ejecutado salvo autorizacion explicita.
- Sin scripts SQL modificados salvo autorizacion explicita.
- Sin escrituras no autorizadas.
- Sin cambios en `OracleConnection.java`.
- Sin cambios en `FrmLogin.java` legacy.
- Sin cambios en `MenuPrincipal.java` legacy.
- Sin cambios en `com.sdrerc.Main` legacy.
- Sin passwords reales documentados.
- Sin IDs hardcodeados nuevos.

## 22. Nombres amigables de etapas/estados

Mapeo visual obligatorio de macroetapas:

- `REGISTRO` -> `Registro`
- `ASIGNACION` -> `Asignacion`
- `ANALISIS` -> `Analisis`
- `VERIFICACION` -> `Verificacion`
- `FIRMA_EMISION` -> `Firma / Emision`
- `EJECUCION` -> `Ejecucion`
- `NOTIFICACION` -> `Notificacion`
- `PUBLICACION_CONDICIONAL` -> `Publicacion`
- `EXPEDIENTE_DIGITAL` -> `Expediente digital`
- `CIERRE_ARCHIVO` -> `Cierre / Archivo`

Reglas:

- No crear etapa visual `VALIDACION`.
- Las validaciones son acciones, observaciones, evaluaciones o reglas dentro de una etapa.
- Mantener codigos tecnicos internamente.
- Mostrar nombres amigables al usuario final.

## 23. Reglas para prompts incrementales

- Cada prompt debe indicar alcance exacto.
- Cada incremento debe ser pequeno, compilable y verificable.
- No mezclar UI con escritura salvo autorizacion explicita.
- No mezclar BD con Java salvo autorizacion explicita.
- Si el cambio es visual, no tocar SQL.
- Si el cambio es de BD, no tocar Java salvo autorizacion explicita.
- Si hay cambios ajenos en `git status`, reportarlos y no incluirlos.
- Si una mejora visual genera superposicion, corregir layout antes de agregar nuevas funcionalidades.
- Siempre indicar archivos creados/modificados, build y restricciones cumplidas.
