package com.sdrerc.application.sdrercapp;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GrupoFamiliarHeuristicaService {

    private static final Set<String> PARTICULAS = new HashSet<>(Arrays.asList(
            "DE", "DEL", "DELA", "LA", "LAS", "LOS", "Y", "DA", "DAS", "DO", "DOS"
    ));

    public String claveApellidosTitular(String titular) {
        String normalizado = normalizar(titular);
        if (normalizado.isEmpty()) {
            return null;
        }
        String[] tokens = normalizado.split(" ");
        List<String> significativos = new ArrayList<>();
        for (String token : tokens) {
            String limpio = token == null ? "" : token.trim();
            if (limpio.length() <= 1 || PARTICULAS.contains(limpio)) {
                continue;
            }
            significativos.add(limpio);
        }
        if (significativos.size() < 3) {
            return null;
        }
        return significativos.get(0) + "|" + significativos.get(1);
    }

    public String normalizar(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized;
    }
}
