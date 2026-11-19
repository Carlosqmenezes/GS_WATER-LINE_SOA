package br.com.waterline.alertas.api.dto;

import java.time.Instant;

import br.com.waterline.alertas.dominio.Canal;
import br.com.waterline.alertas.dominio.Inscricao;
import br.com.waterline.alertas.dominio.TipoInscrito;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Inscrição registrada. O contato volta mascarado.")
public record InscricaoResponse(
		Long id,
		TipoInscrito tipo,
		String nome,
		Canal canal,
		@Schema(example = "*******0004") String contatoMascarado,
		String endereco,
		double latitude,
		double longitude,
		int raioMetros,
		Instant criadaEm) {

	public static InscricaoResponse de(Inscricao inscricao) {
		return new InscricaoResponse(inscricao.getId(), inscricao.getTipo(), inscricao.getNome(),
				inscricao.getCanal(), inscricao.contatoMascarado(), inscricao.getEndereco(),
				inscricao.getLocalizacao().latitude(), inscricao.getLocalizacao().longitude(),
				inscricao.getRaioMetros(), inscricao.getCriadaEm());
	}
}
