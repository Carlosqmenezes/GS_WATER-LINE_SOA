package br.com.waterline.alertas.dominio.excecao;

public class ConflitoException extends RuntimeException {

	public ConflitoException(String mensagem) {
		super(mensagem);
	}
}
