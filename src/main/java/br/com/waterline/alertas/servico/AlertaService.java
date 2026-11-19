package br.com.waterline.alertas.servico;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.waterline.alertas.configuracao.AlertasProperties;
import br.com.waterline.alertas.dominio.Alerta;
import br.com.waterline.alertas.dominio.Alerta.ResumoEnvios;
import br.com.waterline.alertas.dominio.Coordenada;
import br.com.waterline.alertas.dominio.Envio;
import br.com.waterline.alertas.dominio.Inscricao;
import br.com.waterline.alertas.dominio.NivelAlerta;
import br.com.waterline.alertas.dominio.StatusEnvio;
import br.com.waterline.alertas.dominio.excecao.RecursoNaoEncontradoException;
import br.com.waterline.alertas.dominio.excecao.RegraDeNegocioException;
import br.com.waterline.alertas.repositorio.AlertaRepository;
import br.com.waterline.alertas.repositorio.EnvioRepository;
import br.com.waterline.alertas.repositorio.InscricaoRepository;

@Service
public class AlertaService {

	private static final Logger log = LoggerFactory.getLogger(AlertaService.class);

	private final AlertaRepository alertas;
	private final InscricaoRepository inscricoes;
	private final EnvioRepository envios;
	private final Notificador notificador;
	private final AlertasProperties propriedades;
	private final Clock relogio;

	public AlertaService(AlertaRepository alertas, InscricaoRepository inscricoes, EnvioRepository envios,
			Notificador notificador, AlertasProperties propriedades, Clock relogio) {
		this.alertas = alertas;
		this.inscricoes = inscricoes;
		this.envios = envios;
		this.notificador = notificador;
		this.propriedades = propriedades;
		this.relogio = relogio;
	}

	public record Registro(Alerta alerta, ResumoEnvios resumo, boolean novo) {
	}

	public record Detalhe(Alerta alerta, ResumoEnvios resumo) {
	}

	public record AlertaProximo(Alerta alerta, double distanciaMetros) {
	}

	// Registrar alerta
	@Transactional
	public Registro registrar(NovoAlerta dados, NivelAlerta nivelDaRota) {
		if (dados.nivelInformado() != null && dados.nivelInformado() != nivelDaRota) {
			throw new RegraDeNegocioException(nivelDaRota == NivelAlerta.EMERGENCIA
					? "Esta rota recebe só alertas de emergência."
					: "Alertas de emergência vão para POST /alertas/emergencia.");
		}

		var existente = alertas.findByAlertaOrigemId(dados.alertaOrigemId());
		if (existente.isPresent()) {
			log.info("Alerta de origem {} já registrado como {}; nada foi reenviado.", dados.alertaOrigemId(),
					existente.get().getId());
			return new Registro(existente.get(), existente.get().resumoDosEnvios(), false);
		}

		Instant agora = relogio.instant();
		Instant criadoEm = dados.criadoEm() != null ? dados.criadoEm() : agora;
		Alerta alerta = alertas.save(new Alerta(dados.alertaOrigemId(), nivelDaRota, dados.mensagem(),
				dados.origem(), dados.localizacao(), criadoEm, propriedades.validade()));

		List<Inscricao> alcancadas = inscricoesNoRaio(alerta.getLocalizacao());
		for (Inscricao inscricao : alcancadas) {
			boolean entregue = notificador.notificarInscrito(alerta, inscricao);
			alerta.registrarEnvio(inscricao, entregue ? StatusEnvio.ENVIADO : StatusEnvio.FALHOU, agora);
		}

		if (nivelDaRota == NivelAlerta.EMERGENCIA) {
			boolean entregue = notificador.notificarCentral(alerta);
			alerta.registrarEnvioACentral(entregue ? StatusEnvio.ENVIADO : StatusEnvio.FALHOU, agora);
		}

		log.info("Alerta {} ({}) registrado com {} envio(s).", alerta.getId(), alerta.getNivel(),
				alerta.getEnvios().size());
		return new Registro(alerta, alerta.resumoDosEnvios(), true);
	}

	// Inscrições no raio
	public List<Inscricao> inscricoesNoRaio(Coordenada pontoDoAlerta) {
		return inscricoes.buscarNaArea(pontoDoAlerta.areaAoRedor(Inscricao.RAIO_MAXIMO_METROS)).stream()
				.filter(inscricao -> inscricao.cobre(pontoDoAlerta))
				.sorted(Comparator.comparing(Inscricao::getId))
				.toList();
	}

	// Alertas ativos próximos
	@Transactional(readOnly = true)
	public List<AlertaProximo> ativosPerto(Coordenada ponto) {
		int raio = propriedades.raioConsultaMetros();
		return alertas.buscarAtivosNaArea(relogio.instant(), ponto.areaAoRedor(raio)).stream()
				.map(alerta -> new AlertaProximo(alerta, ponto.distanciaEmMetros(alerta.getLocalizacao())))
				.filter(proximo -> proximo.distanciaMetros() <= raio)
				.sorted(Comparator
						.comparing((AlertaProximo proximo) -> proximo.alerta().getNivel() != NivelAlerta.EMERGENCIA)
						.thenComparingDouble(AlertaProximo::distanciaMetros))
				.toList();
	}

	@Transactional(readOnly = true)
	public Detalhe detalhar(Long id) {
		Alerta alerta = buscar(id);
		return new Detalhe(alerta, alerta.resumoDosEnvios());
	}

	private Alerta buscar(Long id) {
		return alertas.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Alerta " + id + " não encontrado."));
	}

	@Transactional(readOnly = true)
	public List<Envio> envios(Long alertaId) {
		buscar(alertaId);
		return envios.listarDoAlerta(alertaId);
	}

	public boolean ativo(Alerta alerta) {
		return alerta.ativoEm(relogio.instant());
	}
}
