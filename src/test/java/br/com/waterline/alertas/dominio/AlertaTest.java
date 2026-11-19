package br.com.waterline.alertas.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import br.com.waterline.alertas.dominio.excecao.RegraDeNegocioException;

class AlertaTest {

	private static final Instant CRIADO_EM = Instant.parse("2026-11-17T12:00:00Z");

	private static Alerta alerta(NivelAlerta nivel) {
		return new Alerta(42, nivel, "Água subindo rápido.",
				new OrigemAlerta(1, "BL-0001", "Rua Fictícia das Águas, 100 - Bairro Exemplo"),
				new Coordenada(-23.55, -46.64), CRIADO_EM, Duration.ofHours(2));
	}

	// Validade
	@Test
	void alertaFicaAtivoAteExpirar() {
		var alerta = alerta(NivelAlerta.ALERTA_PRECOCE);
		assertThat(alerta.getExpiraEm()).isEqualTo(CRIADO_EM.plus(Duration.ofHours(2)));
		assertThat(alerta.ativoEm(CRIADO_EM.plus(Duration.ofMinutes(119)))).isTrue();
		assertThat(alerta.ativoEm(CRIADO_EM.plus(Duration.ofHours(2)))).isFalse();
	}

	// Envio à central
	@Test
	void soEmergenciaVaiParaACentral() {
		var precoce = alerta(NivelAlerta.ALERTA_PRECOCE);
		assertThatThrownBy(() -> precoce.registrarEnvioACentral(StatusEnvio.ENVIADO, CRIADO_EM))
				.isInstanceOf(RegraDeNegocioException.class);

		var emergencia = alerta(NivelAlerta.EMERGENCIA);
		emergencia.registrarEnvioACentral(StatusEnvio.ENVIADO, CRIADO_EM);
		assertThat(emergencia.resumoDosEnvios().centralNotificada()).isTrue();
	}

	// Nível do C#
	@Test
	void nivelAceitaOFormatoDaApiPrincipal() {
		assertThat(NivelAlerta.deTexto("AlertaPrecoce")).isEqualTo(NivelAlerta.ALERTA_PRECOCE);
		assertThat(NivelAlerta.deTexto("Emergencia")).isEqualTo(NivelAlerta.EMERGENCIA);
		assertThat(NivelAlerta.deTexto("ALERTA_PRECOCE")).isEqualTo(NivelAlerta.ALERTA_PRECOCE);
		assertThatThrownBy(() -> NivelAlerta.deTexto("Chuva")).isInstanceOf(IllegalArgumentException.class);
	}
}
