package br.com.waterline.alertas.servico;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import br.com.waterline.alertas.dominio.Alerta;
import br.com.waterline.alertas.dominio.Inscricao;

@Component
public class NotificadorSimulado implements Notificador {

	private static final Logger log = LoggerFactory.getLogger(NotificadorSimulado.class);

	@Override
	public boolean notificarInscrito(Alerta alerta, Inscricao inscricao) {
		log.info("Envio simulado: alerta {} para a inscrição {} por {}.", alerta.getId(), inscricao.getId(),
				inscricao.getCanal());
		return true;
	}

	@Override
	public boolean notificarCentral(Alerta alerta) {
		log.info("Envio simulado: emergência do alerta {} para a central (CGE / Defesa Civil).", alerta.getId());
		return true;
	}
}
