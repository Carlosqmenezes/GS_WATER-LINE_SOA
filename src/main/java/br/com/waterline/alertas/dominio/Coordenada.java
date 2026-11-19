package br.com.waterline.alertas.dominio;

import br.com.waterline.alertas.dominio.excecao.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Coordenada(
		@Column(name = "latitude", nullable = false) double latitude,
		@Column(name = "longitude", nullable = false) double longitude) {

	private static final double RAIO_DA_TERRA_METROS = 6_371_000;

	// Validação
	public Coordenada {
		if (Double.isNaN(latitude) || latitude < -90 || latitude > 90) {
			throw new RegraDeNegocioException("A latitude precisa estar entre -90 e 90.");
		}
		if (Double.isNaN(longitude) || longitude < -180 || longitude > 180) {
			throw new RegraDeNegocioException("A longitude precisa estar entre -180 e 180.");
		}
	}

	// Fórmula de Haversine
	public double distanciaEmMetros(Coordenada outra) {
		double dLat = Math.toRadians(outra.latitude - latitude);
		double dLng = Math.toRadians(outra.longitude - longitude);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(outra.latitude))
						* Math.sin(dLng / 2) * Math.sin(dLng / 2);
		return 2 * RAIO_DA_TERRA_METROS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}

	// Área de busca
	public AreaDeBusca areaAoRedor(double raioMetros) {
		double deltaLat = Math.toDegrees(raioMetros / RAIO_DA_TERRA_METROS);
		double cosLat = Math.max(Math.cos(Math.toRadians(latitude)), 0.01);
		double deltaLng = Math.toDegrees(raioMetros / (RAIO_DA_TERRA_METROS * cosLat));
		return new AreaDeBusca(latitude - deltaLat, latitude + deltaLat, longitude - deltaLng, longitude + deltaLng);
	}

	public record AreaDeBusca(double latMin, double latMax, double lngMin, double lngMax) {
	}
}
