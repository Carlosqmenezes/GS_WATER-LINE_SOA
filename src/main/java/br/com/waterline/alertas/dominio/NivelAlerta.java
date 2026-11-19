package br.com.waterline.alertas.dominio;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NivelAlerta {
	ALERTA_PRECOCE,
	EMERGENCIA;

	// Formato do C#
	@JsonCreator
	public static NivelAlerta deTexto(String texto) {
		if (texto == null) {
			return null;
		}
		String normalizado = texto.replace("_", "").replace(" ", "");
		for (NivelAlerta nivel : values()) {
			if (nivel.name().replace("_", "").equalsIgnoreCase(normalizado)) {
				return nivel;
			}
		}
		throw new IllegalArgumentException("Nível de alerta desconhecido: " + texto);
	}
}
