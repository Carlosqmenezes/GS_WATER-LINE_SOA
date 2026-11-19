package br.com.waterline.alertas.servico;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.waterline.alertas.dominio.Inscricao;
import br.com.waterline.alertas.dominio.excecao.ConflitoException;
import br.com.waterline.alertas.dominio.excecao.RecursoNaoEncontradoException;
import br.com.waterline.alertas.repositorio.EnvioRepository;
import br.com.waterline.alertas.repositorio.InscricaoRepository;

@Service
public class InscricaoService {

	private static final Logger log = LoggerFactory.getLogger(InscricaoService.class);

	private final InscricaoRepository inscricoes;
	private final EnvioRepository envios;

	public InscricaoService(InscricaoRepository inscricoes, EnvioRepository envios) {
		this.inscricoes = inscricoes;
		this.envios = envios;
	}

	// Nova inscrição
	@Transactional
	public Inscricao inscrever(Inscricao inscricao) {
		if (inscricoes.existsByCanalAndContato(inscricao.getCanal(), inscricao.getContato())) {
			throw new ConflitoException("Já existe uma inscrição com este contato neste canal.");
		}
		Inscricao salva = inscricoes.save(inscricao);
		log.info("Inscrição {} criada.", salva.getId());
		return salva;
	}

	@Transactional(readOnly = true)
	public Inscricao buscar(Long id) {
		return inscricoes.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Inscrição " + id + " não encontrada."));
	}

	// Cancelar inscrição
	@Transactional
	public void cancelar(Long id) {
		if (!inscricoes.existsById(id)) {
			throw new RecursoNaoEncontradoException("Inscrição " + id + " não encontrada.");
		}
		envios.desvincularInscricao(id);
		inscricoes.deleteById(id);
		log.info("Inscrição {} cancelada e dados pessoais apagados.", id);
	}
}
