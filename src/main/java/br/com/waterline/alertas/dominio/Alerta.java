package br.com.waterline.alertas.dominio;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import br.com.waterline.alertas.dominio.excecao.RegraDeNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "alertas")
public class Alerta {

	public static final int TAMANHO_MAXIMO_MENSAGEM = 500;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "alerta_origem_id", nullable = false, unique = true)
	private int alertaOrigemId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NivelAlerta nivel;

	@Column(nullable = false, length = TAMANHO_MAXIMO_MENSAGEM)
	private String mensagem;

	@Embedded
	private OrigemAlerta origem;

	@Embedded
	private Coordenada localizacao;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	@Column(name = "expira_em", nullable = false)
	private Instant expiraEm;

	@OneToMany(mappedBy = "alerta", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private List<Envio> envios = new ArrayList<>();

	protected Alerta() {
	}

	public Alerta(int alertaOrigemId, NivelAlerta nivel, String mensagem, OrigemAlerta origem,
			Coordenada localizacao, Instant criadoEm, Duration validade) {
		if (alertaOrigemId <= 0) {
			throw new RegraDeNegocioException("O id do alerta na API principal precisa ser positivo.");
		}
		if (mensagem == null || mensagem.isBlank()) {
			throw new RegraDeNegocioException("A mensagem do alerta é obrigatória.");
		}
		if (mensagem.strip().length() > TAMANHO_MAXIMO_MENSAGEM) {
			throw new RegraDeNegocioException(
					"A mensagem do alerta aceita no máximo " + TAMANHO_MAXIMO_MENSAGEM + " caracteres.");
		}
		if (validade == null || validade.isNegative() || validade.isZero()) {
			throw new RegraDeNegocioException("A validade do alerta precisa ser positiva.");
		}
		this.alertaOrigemId = alertaOrigemId;
		this.nivel = Objects.requireNonNull(nivel, "nivel");
		this.mensagem = mensagem.strip();
		this.origem = Objects.requireNonNull(origem, "origem");
		this.localizacao = Objects.requireNonNull(localizacao, "localizacao");
		this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm");
		this.expiraEm = criadoEm.plus(validade);
	}

	// Alerta ativo
	public boolean ativoEm(Instant instante) {
		return instante.isBefore(expiraEm);
	}

	// Envio ao inscrito
	public Envio registrarEnvio(Inscricao inscricao, StatusEnvio status, Instant registradoEm) {
		Envio envio = Envio.paraInscrito(this, inscricao, status, registradoEm);
		envios.add(envio);
		return envio;
	}

	// Envio à central
	public Envio registrarEnvioACentral(StatusEnvio status, Instant registradoEm) {
		if (nivel != NivelAlerta.EMERGENCIA) {
			throw new RegraDeNegocioException("Só alertas de emergência são enviados à central.");
		}
		Envio envio = Envio.paraCentral(this, status, registradoEm);
		envios.add(envio);
		return envio;
	}

	// Resumo dos envios
	public ResumoEnvios resumoDosEnvios() {
		int inscritos = 0;
		int falhas = 0;
		boolean central = false;
		for (Envio envio : envios) {
			if (envio.getStatus() == StatusEnvio.FALHOU) {
				falhas++;
			} else if (envio.getDestino() == DestinoEnvio.CENTRAL) {
				central = true;
			} else {
				inscritos++;
			}
		}
		return new ResumoEnvios(inscritos, central, falhas);
	}

	public record ResumoEnvios(int inscritosNotificados, boolean centralNotificada, int falhas) {
	}

	public Long getId() {
		return id;
	}

	public int getAlertaOrigemId() {
		return alertaOrigemId;
	}

	public NivelAlerta getNivel() {
		return nivel;
	}

	public String getMensagem() {
		return mensagem;
	}

	public OrigemAlerta getOrigem() {
		return origem;
	}

	public Coordenada getLocalizacao() {
		return localizacao;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

	public Instant getExpiraEm() {
		return expiraEm;
	}

	public List<Envio> getEnvios() {
		return Collections.unmodifiableList(envios);
	}

	@Override
	public String toString() {
		return "Alerta[id=" + id + ", nivel=" + nivel + "]";
	}
}
