package br.com.waterline.alertas.api.dto;

import java.time.Instant;

import br.com.waterline.alertas.dominio.NivelAlerta;
import br.com.waterline.alertas.servico.AlertaService.AlertaProximo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Alerta ativo perto do ponto consultado.")
public record AlertaAtivoResponse(
		Long id,
		NivelAlerta nivel,
		String mensagem,
		String codigoBocaDeLobo,
		String endereco,
		double latitude,
		double longitude,
		@Schema(description = "Distância do ponto consultado, em metros", example = "24") long distanciaMetros,
		Instant criadoEm,
		Instant expiraEm) {

	public static AlertaAtivoResponse de(AlertaProximo proximo) {
		var alerta = proximo.alerta();
		return new AlertaAtivoResponse(alerta.getId(), alerta.getNivel(), alerta.getMensagem(),
				alerta.getOrigem().codigoBocaDeLobo(), alerta.getOrigem().endereco(),
				alerta.getLocalizacao().latitude(), alerta.getLocalizacao().longitude(),
				Math.round(proximo.distanciaMetros()), alerta.getCriadoEm(), alerta.getExpiraEm());
	}
}
