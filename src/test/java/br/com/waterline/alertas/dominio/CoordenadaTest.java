package br.com.waterline.alertas.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

import br.com.waterline.alertas.dominio.excecao.RegraDeNegocioException;

class CoordenadaTest {

	private static final Coordenada BL_0001 = new Coordenada(-23.55000, -46.64000);

	@Test
	void distanciaDoMesmoPontoEZero() {
		assertThat(BL_0001.distanciaEmMetros(BL_0001)).isZero();
	}

	@Test
	void umMilesimoDeGrauDeLatitudeTemCercaDe111Metros() {
		var aoNorte = new Coordenada(-23.54900, -46.64000);
		assertThat(BL_0001.distanciaEmMetros(aoNorte)).isCloseTo(111.2, within(0.5));
	}

	@Test
	void distanciaESimetrica() {
		var outra = new Coordenada(-23.56000, -46.64800);
		assertThat(BL_0001.distanciaEmMetros(outra)).isCloseTo(outra.distanciaEmMetros(BL_0001), within(1e-6));
	}

	@Test
	void areaDeBuscaContemPontosDentroDoRaio() {
		var area = BL_0001.areaAoRedor(500);
		var aLeste = new Coordenada(-23.55000, -46.63600);
		assertThat(BL_0001.distanciaEmMetros(aLeste)).isLessThan(500);
		assertThat(aLeste.longitude()).isBetween(area.lngMin(), area.lngMax());
		assertThat(aLeste.latitude()).isBetween(area.latMin(), area.latMax());
	}

	@Test
	void latitudeForaDoIntervaloERecusada() {
		assertThatThrownBy(() -> new Coordenada(91, 0)).isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> new Coordenada(0, -181)).isInstanceOf(RegraDeNegocioException.class);
	}
}
