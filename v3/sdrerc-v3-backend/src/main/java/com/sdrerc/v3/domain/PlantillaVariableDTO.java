package com.sdrerc.v3.domain;

/**
 * Catálogo de referencia de variables {@code #variable#} disponibles al redactar una plantilla
 * Word — mejora explícita sobre V2 (Administración &gt; Plantillas de documento V3, pedido
 * explícito del usuario "mejora funcional, no solo de diseño"): en V2 esto era un bloque de texto
 * fijo dentro de un {@code JOptionPane} ("Ver variables disponibles"); acá se expone como catálogo
 * estructurado y filtrable en la UI, generado a partir de las MISMAS claves que
 * {@code AnalisisPlantillaDocumentoService#valores(...)} realmente resuelve — si se agrega una
 * variable nueva ahí, agregarla también acá (una sola fuente de verdad documentada, no autogenerada
 * por reflexión para mantener el orden/categoría curados a mano).
 */
public record PlantillaVariableDTO(String nombre, String descripcion, String categoria) {
}
