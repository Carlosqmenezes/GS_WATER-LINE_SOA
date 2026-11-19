package br.com.waterline.alertas.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import br.com.waterline.alertas.dominio.excecao.RegraDeNegocioException;

class InscricaoTest {

	private static final Coordenada CASA = new Coordenada(-23.55000, -46.64000);

	private static Inscricao inscricao(String contato, Canal canal, int raio) {
		return new Inscricao(TipoInscrito.MORADOR, "Morador Fictício", contato, canal,
				"Rua Fictícia, 1 - Bairro Exemplo", CASA, raio, Instant.parse("2026-11-17T12:00:00Z"));
	}

	// Regra de raio
	@Test
	void cobreBocaDeLoboDentroDoRaio() {
		var inscricao = inscricao("11000000001", Canal.SMS, 300);
		var bocaA200Metros = new Coordenada(-23.55180, -46.64000);
		assertThat(CASA.distanciaEmMetros(bocaA200Metros)).isBetween(190.0, 210.0);
		assertThat(inscricao.cobre(bocaA200Metros)).isTrue();
	}

	@Test
	void naoCobreBocaDeLoboForaDoRaio() {
		var inscricao = inscricao("11000000001", Canal.SMS, 100);
		var bocaA200Metros = new Coordenada(-23.55180, -46.64000);
		assertThat(inscricao.cobre(bocaA200Metros)).isFalse();
	}

	@Test
	void cobreExatamenteNoLimiteDoRaio() {
		var inscricao = inscricao("11000000001", Canal.SMS, 50);
		assertThat(inscricao.cobre(CASA)).isTrue();
	}

	@Test
	void raioForaDosLimitesERecusado() {
		assertThatThrownBy(() -> inscricao("11000000001", Canal.SMS, 49)).isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> inscricao("11000000001", Canal.SMS, 2001)).isInstanceOf(RegraDeNegocioException.class);
	}

	// Contato
	@Test
	void telefoneGuardaSoOsDigitosEVoltaMascarado() {
		var inscricao = inscricao("(11) 00000-0001", Canal.WHATSAPP, 300);
		assertThat(inscricao.getContato()).isEqualTo("11000000001");
		assertThat(inscricao.contatoMascarado()).isEqualTo("*******0001");
	}

	@Test
	void emailVoltaMascarado() {
		var inscricao = inscricao("Moradora@Exemplo.com", Canal.EMAIL, 300);
		assertThat(inscricao.getContato()).isEqualTo("moradora@exemplo.com");
		assertThat(inscricao.contatoMascarado()).isEqualTo("m***@exemplo.com");
	}

	@Test
	void contatoIncompativelComOCanalERecusado() {
		assertThatThrownBy(() -> inscricao("11000000001", Canal.EMAIL, 300))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> inscricao("moradora@exemplo.com", Canal.SMS, 300))
				.isInstanceOf(RegraDeNegocioException.class);
	}
}
