package br.com.waterline.alertas.dominio.excecao;

public class RegraDeNegocioException extends RuntimeException {

	public RegraDeNegocioException(String mensagem) {
		super(mensagem);
	}
}
