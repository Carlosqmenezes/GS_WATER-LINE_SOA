package br.com.waterline.alertas.api.dto;

import java.time.Instant;

import br.com.waterline.alertas.dominio.Alerta;
import br.com.waterline.alertas.dominio.Coordenada;
import br.com.waterline.alertas.dominio.NivelAlerta;
import br.com.waterline.alertas.dominio.OrigemAlerta;
import br.com.waterline.alertas.servico.NovoAlerta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Alerta enviado pela API principal do WaterLine. Só traz dados da boca de lobo.")
public record AlertaRequest(
		@Schema(description = "Id do alerta na API principal", example = "42")
		@NotNull(message = "Informe o alertaId.") @Positive Integer alertaId,

		@Schema(description = "Id da boca de lobo na API principal", example = "1")
		@NotNull(message = "Informe o bocaDeLoboId.") @Positive Integer bocaDeLoboId,

		@Schema(description = "Código da boca de lobo", example = "BL-0001")
		@NotBlank(message = "Informe o código da boca de lobo.") @Size(max = 20) String codigoBocaDeLobo,

		@Schema(description = "Endereço da boca de lobo", example = "Rua Fictícia das Águas, 100 - Bairro Exemplo")
		@NotBlank(message = "Informe o endereço.") @Size(max = 200) String endereco,

		@Schema(example = "-23.55")
		@NotNull(message = "Informe a latitude.") @DecimalMin("-90") @DecimalMax("90") Double latitude,

		@Schema(example = "-46.64")
		@NotNull(message = "Informe a longitude.") @DecimalMin("-180") @DecimalMax("180") Double longitude,

		@Schema(description = "Opcional. Se vier, precisa combinar com a rota. Aceita também AlertaPrecoce e Emergencia, como a API C# envia.")
		NivelAlerta nivel,

		@Schema(example = "Água subindo rápido na boca de lobo BL-0001. Tire o carro da rua.")
		@NotBlank(message = "Informe a mensagem.") @Size(max = Alerta.TAMANHO_MAXIMO_MENSAGEM) String mensagem,

		@Schema(description = "Quando a API principal criou o alerta (UTC). Se faltar, vale a hora do recebimento.")
		Instant criadoEm) {

	public NovoAlerta paraComando() {
		return new NovoAlerta(alertaId, nivel, mensagem,
				new OrigemAlerta(bocaDeLoboId, codigoBocaDeLobo, endereco),
				new Coordenada(latitude, longitude), criadoEm);
	}
}
