**MEMORANDO N° XXX-2026/DRC/SDRERC/RENIEC**

**Para:** [Nombre del Jefe de la Oficina de Tecnologías de la Información]
Jefe de la Oficina de Tecnologías de la Información (OTI)

**De:** [Nombre del remitente]
Sub Dirección de Recursos Registrales Civiles (SDRERC)
Dirección de Registros Civiles (DRC)

**Asunto:** Solicitud de documentos normativos internos de OTI para verificación de cumplimiento del aplicativo web SDRERC V3

**Referencia:**

a) NAI-025-OTI/UIST/002 "Atención de Requerimientos en la Base de Datos de Producción y Desarrollo"

b) DI-042-OTI/005 "Requerimiento de Desarrollo y Mantenimiento de Software"

c) DI 061-OTI "Seguridad Informática del RENIEC"

d) DI-049-OTI "Arquitectura y Gestión de la Red del RENIEC"

e) DI 064-OTI "Servicios de Tecnologías de la Información para Usuarios Finales"

f) NAI-027-OTI/UIS/002 "Metodología de Desarrollo y Mantenimiento de Software RENIEC", segunda versión

**Fecha:** [dd de mes de 2026]

---

Tengo el agrado de dirigirme a usted para saludarlo cordialmente y, en el marco de la migración del sistema de gestión de expedientes de la Sub Dirección de Recursos Registrales Civiles hacia una nueva versión web (en adelante "SDRERC V3"), informar que se ha realizado una revisión interna de los documentos normativos de la Oficina de Tecnologías de la Información (OTI) vigentes y aplicables al desarrollo de software, con el objetivo de verificar el cumplimiento de dicho aplicativo antes de su pase a producción.

Como resultado de esa revisión se ha identificado que los documentos de referencia citan, de forma explícita, otros 7 documentos normativos internos que no obran en poder de esta Sub Dirección y que resultan necesarios para completar la verificación de cumplimiento. Por lo expuesto, solicito a su despacho la remisión de los siguientes documentos:

## 1. Documento de "Estándares de Base de Datos"

**Justificación:** define la convención de nomenclatura de tablas, columnas y demás objetos de base de datos exigida para cualquier pase a producción de BD. Sin este documento no es posible verificar si los objetos del esquema SDRERC_APP (tablas y columnas en español, snake_case) cumplen el estándar institucional, ni anticipar observaciones del DBA.

**Referenciado en:** NAI-025-OTI/UIST/002, numerales 3.4.4 y 3.4.5.a; NAI-027-OTI/UIS/002 (segunda versión), Anexo N° 02.

## 2. Directiva DI-446-OPP "Metodología para la Gestión por Proyectos Institucionales"

**Justificación:** define la documentación adicional exigida cuando un requerimiento de desarrollo de software se califica como "proyecto institucional" (cronograma, hitos, gobernanza de proyecto), entregables que hoy no es posible verificar sin el documento.

**Referenciado en:** DI-042-OTI/005, numerales 6.1.4 y 7.1.1; NAI-027-OTI/UIS/002 (segunda versión), Anexo N° 02, fila 1.2.

## 3. "Estándar para la Construcción Segura de Aplicaciones Web"

**Justificación:** estándar específico de construcción segura para aplicaciones web, de mantenimiento anual obligatorio por la propia UIS, distinto del Estándar de Codificación Segura ya remitido previamente. Es el documento más directamente aplicable a SDRERC V3, dado que se trata de una aplicación web (backend + frontend).

**Referenciado en:** DI 061-OTI, numerales 5.3.2 y 7.11.2.1; NAI-027-OTI/UIS/002 (segunda versión), numeral 3.1.6.

## 4. Directiva DI-038-OTI/003 "Controles Tecnológicos de Seguridad de la Información del SGSI - RENIEC"

**Justificación:** por su denominación, detalla los controles técnicos concretos del Sistema de Gestión de Seguridad de la Información (SGSI) del RENIEC, con un nivel de detalle mayor al de los estándares generales ya remitidos (criptografía, autenticación, logs de auditoría).

**Referenciado en:** DI 061-OTI, numeral 3.43 (Base Legal); DI-049-OTI, numeral 3.33 (Base Legal); DI 064-OTI, numeral 3.44 (Base Legal).

## 5. Política y Objetivos de Seguridad de la Información del RENIEC

(aprobada por Resolución Jefatural N° 000153-2023/JNAC/RENIEC)

**Justificación:** documento marco del que se desprenden los estándares específicos ya conocidos (criptografía, autenticación, logs, datos confidenciales); se cuenta únicamente con la resolución que la aprueba, no con su contenido.

**Referenciado en:** DI 061-OTI (Base Legal y sección VI "Disposiciones Generales"); DI-042-OTI/005; NAI-029-OTI/UIS/003; entre otros.

## 6. "Guía de diseño web"

**Justificación:** define los patrones de diseño de interfaz de usuario exigidos para aplicaciones web del RENIEC, necesarios para verificar consistencia visual del frontend de SDRERC V3 con el estándar institucional.

**Referenciado en:** NAI-027-OTI/UIS/002 (segunda versión), numeral 3.1.6.

## 7. "Lineamientos para uso de logotipo y elementos de la identidad gráfica del RENIEC"

**Justificación:** define el uso correcto del logotipo institucional y demás elementos de identidad gráfica dentro de aplicativos del RENIEC.

**Referenciado en:** NAI-027-OTI/UIS/002 (segunda versión), numeral 3.1.6.

---

Sin otro particular, y agradeciendo de antemano la atención brindada, quedamos atentos a la remisión de los documentos solicitados a fin de continuar con la verificación de cumplimiento normativo del aplicativo SDRERC V3.

Atentamente,


_______________________________

[Nombre y cargo del remitente]

Sub Dirección de Recursos Registrales Civiles

Dirección de Registros Civiles
