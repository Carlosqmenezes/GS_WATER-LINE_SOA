package br.com.waterline.alertas.servico;

import br.com.waterline.alertas.dominio.Alerta;
import br.com.waterline.alertas.dominio.Inscricao;

public interface Notificador {

	boolean notificarInscrito(Alerta alerta, Inscricao inscricao);

	boolean notificarCentral(Alerta alerta);
}
