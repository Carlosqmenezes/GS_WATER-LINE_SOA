package br.com.waterline.alertas.dominio;

import br.com.waterline.alertas.dominio.excecao.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record OrigemAlerta(
		@Column(name = "boca_de_lobo_id", nullable = false) int bocaDeLoboId,
		@Column(name = "codigo_boca_de_lobo", nullable = false, length = 20) String codigoBocaDeLobo,
		@Column(name = "endereco", nullable = false, length = 200) String endereco) {

	// Validação
	public OrigemAlerta {
		if (bocaDeLoboId <= 0) {
			throw new RegraDeNegocioException("O id da boca de lobo precisa ser positivo.");
		}
		if (codigoBocaDeLobo == null || codigoBocaDeLobo.isBlank() || codigoBocaDeLobo.strip().length() > 20) {
			throw new RegraDeNegocioException("O código da boca de lobo é obrigatório e tem no máximo 20 caracteres.");
		}
		if (endereco == null || endereco.isBlank() || endereco.strip().length() > 200) {
			throw new RegraDeNegocioException("O endereço da boca de lobo é obrigatório e tem no máximo 200 caracteres.");
		}
		codigoBocaDeLobo = codigoBocaDeLobo.strip();
		endereco = endereco.strip();
	}
}
