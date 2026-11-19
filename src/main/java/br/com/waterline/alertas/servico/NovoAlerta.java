package br.com.waterline.alertas.servico;

import java.time.Instant;

import br.com.waterline.alertas.dominio.Coordenada;
import br.com.waterline.alertas.dominio.NivelAlerta;
import br.com.waterline.alertas.dominio.OrigemAlerta;

public record NovoAlerta(
		int alertaOrigemId,
		NivelAlerta nivelInformado,
		String mensagem,
		OrigemAlerta origem,
		Coordenada localizacao,
		Instant criadoEm) {
}
