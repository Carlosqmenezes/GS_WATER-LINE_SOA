package br.com.waterline.alertas.dominio;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "envios")
public class Envio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "alerta_id", nullable = false)
	private Alerta alerta;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "inscricao_id")
	private Inscricao inscricao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DestinoEnvio destino;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Canal canal;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusEnvio status;

	@Column(name = "registrado_em", nullable = false)
	private Instant registradoEm;

	protected Envio() {
	}

	private Envio(Alerta alerta, Inscricao inscricao, DestinoEnvio destino, Canal canal, StatusEnvio status,
			Instant registradoEm) {
		this.alerta = Objects.requireNonNull(alerta, "alerta");
		this.inscricao = inscricao;
		this.destino = destino;
		this.canal = canal;
		this.status = Objects.requireNonNull(status, "status");
		this.registradoEm = Objects.requireNonNull(registradoEm, "registradoEm");
	}

	static Envio paraInscrito(Alerta alerta, Inscricao inscricao, StatusEnvio status, Instant registradoEm) {
		Objects.requireNonNull(inscricao, "inscricao");
		return new Envio(alerta, inscricao, DestinoEnvio.INSCRITO, inscricao.getCanal(), status, registradoEm);
	}

	static Envio paraCentral(Alerta alerta, StatusEnvio status, Instant registradoEm) {
		return new Envio(alerta, null, DestinoEnvio.CENTRAL, null, status, registradoEm);
	}

	public Long getId() {
		return id;
	}

	public Alerta getAlerta() {
		return alerta;
	}

	public Inscricao getInscricao() {
		return inscricao;
	}

	public DestinoEnvio getDestino() {
		return destino;
	}

	public Canal getCanal() {
		return canal;
	}

	public StatusEnvio getStatus() {
		return status;
	}

	public Instant getRegistradoEm() {
		return registradoEm;
	}
}
