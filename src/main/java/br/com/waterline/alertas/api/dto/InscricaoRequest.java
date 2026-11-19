package br.com.waterline.alertas.api.dto;

import java.time.Instant;

import br.com.waterline.alertas.dominio.Canal;
import br.com.waterline.alertas.dominio.Coordenada;
import br.com.waterline.alertas.dominio.Inscricao;
import br.com.waterline.alertas.dominio.TipoInscrito;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para um morador ou comerciante receber alertas da sua rua.")
public record InscricaoRequest(
		@Schema(description = "Quem se inscreve", example = "MORADOR")
		@NotNull(message = "Informe o tipo: MORADOR ou COMERCIANTE.") TipoInscrito tipo,

		@Schema(description = "Nome ou nome do comércio", example = "Morador Fictício Quatro")
		@NotBlank(message = "Informe o nome.") @Size(max = Inscricao.TAMANHO_MAXIMO_NOME) String nome,

		@Schema(description = "Canal do aviso", example = "SMS")
		@NotNull(message = "Informe o canal: SMS, WHATSAPP ou EMAIL.") Canal canal,

		@Schema(description = "Telefone com DDD (SMS e WhatsApp) ou e-mail", example = "(11) 00000-0004")
		@NotBlank(message = "Informe o contato.") @Size(max = Inscricao.TAMANHO_MAXIMO_CONTATO) String contato,

		@Schema(description = "Rua e número", example = "Rua Fictícia das Águas, 140 - Bairro Exemplo")
		@NotBlank(message = "Informe o endereço.") @Size(max = Inscricao.TAMANHO_MAXIMO_ENDERECO) String endereco,

		@Schema(description = "Latitude do endereço", example = "-23.5502")
		@NotNull(message = "Informe a latitude.") @DecimalMin("-90") @DecimalMax("90") Double latitude,

		@Schema(description = "Longitude do endereço", example = "-46.6403")
		@NotNull(message = "Informe a longitude.") @DecimalMin("-180") @DecimalMax("180") Double longitude,

		@Schema(description = "Raio, em metros, ao redor do endereço", example = "300")
		@NotNull(message = "Informe o raio em metros.")
		@Min(Inscricao.RAIO_MINIMO_METROS) @Max(Inscricao.RAIO_MAXIMO_METROS) Integer raioMetros) {

	public Inscricao paraDominio(Instant agora) {
		return new Inscricao(tipo, nome, contato, canal, endereco, new Coordenada(latitude, longitude), raioMetros,
				agora);
	}
}
