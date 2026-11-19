package br.com.waterline.alertas.configuracao;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class ConfiguracaoGeral {

	// Relógio
	@Bean
	public Clock relogio() {
		return Clock.systemUTC();
	}

	// Documentação OpenAPI
	@Bean
	public OpenAPI documentacao() {
		return new OpenAPI().info(new Info()
				.title("WaterLine — Serviço de alertas")
				.version("1.0.0")
				.description("""
						Serviço separado (SOA) que avisa moradores e comerciantes quando a água sobe numa \
						boca de lobo e encaminha as emergências à central da cidade. A API principal do \
						WaterLine chama POST /alertas e POST /alertas/emergencia; os moradores se inscrevem \
						em POST /inscricoes. Todos os dados de exemplo são fictícios."""));
	}
}
