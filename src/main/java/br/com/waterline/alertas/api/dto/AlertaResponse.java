package br.com.waterline.alertas.api.dto;

import java.time.Instant;

import br.com.waterline.alertas.dominio.Alerta;
import br.com.waterline.alertas.dominio.Alerta.ResumoEnvios;
import br.com.waterline.alertas.dominio.NivelAlerta;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Alerta registrado e o resumo de quem foi avisado.")
public record AlertaResponse(
		Long id,
		int alertaOrigemId,
		NivelAlerta nivel,
		String mensagem,
		int bocaDeLoboId,
		String codigoBocaDeLobo,
		String endereco,
		double latitude,
		double longitude,
		Instant criadoEm,
		Instant expiraEm,
		boolean ativo,
		ResumoEnvios envios) {

	public static AlertaResponse de(Alerta alerta, ResumoEnvios resumo, boolean ativo) {
		return new AlertaResponse(alerta.getId(), alerta.getAlertaOrigemId(), alerta.getNivel(),
				alerta.getMensagem(), alerta.getOrigem().bocaDeLoboId(), alerta.getOrigem().codigoBocaDeLobo(),
				alerta.getOrigem().endereco(), alerta.getLocalizacao().latitude(),
				alerta.getLocalizacao().longitude(), alerta.getCriadoEm(), alerta.getExpiraEm(), ativo, resumo);
	}
}
