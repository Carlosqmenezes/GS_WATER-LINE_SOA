package br.com.waterline.alertas.api.dto;

import java.time.Instant;

import br.com.waterline.alertas.dominio.Canal;
import br.com.waterline.alertas.dominio.DestinoEnvio;
import br.com.waterline.alertas.dominio.Envio;
import br.com.waterline.alertas.dominio.StatusEnvio;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Registro de um aviso: para quem, quando e com qual resultado.")
public record EnvioResponse(
		Long id,
		DestinoEnvio destino,
		@Schema(description = "Nulo para a central ou se a inscrição foi cancelada") Long inscricaoId,
		@Schema(description = "Nulo para a central") Canal canal,
		StatusEnvio status,
		Instant registradoEm) {

	public static EnvioResponse de(Envio envio) {
		Long inscricaoId = envio.getInscricao() == null ? null : envio.getInscricao().getId();
		return new EnvioResponse(envio.getId(), envio.getDestino(), inscricaoId, envio.getCanal(),
				envio.getStatus(), envio.getRegistradoEm());
	}
}
