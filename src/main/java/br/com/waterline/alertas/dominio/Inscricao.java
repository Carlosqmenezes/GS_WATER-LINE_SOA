package br.com.waterline.alertas.dominio;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import br.com.waterline.alertas.dominio.excecao.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inscricoes")
public class Inscricao {

	public static final int RAIO_MINIMO_METROS = 50;
	public static final int RAIO_MAXIMO_METROS = 2000;
	public static final int TAMANHO_MAXIMO_NOME = 100;
	public static final int TAMANHO_MAXIMO_CONTATO = 120;
	public static final int TAMANHO_MAXIMO_ENDERECO = 200;

	private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoInscrito tipo;

	@Column(nullable = false, length = TAMANHO_MAXIMO_NOME)
	private String nome;

	@Column(nullable = false, length = TAMANHO_MAXIMO_CONTATO)
	private String contato;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Canal canal;

	@Column(nullable = false, length = TAMANHO_MAXIMO_ENDERECO)
	private String endereco;

	@Embedded
	private Coordenada localizacao;

	@Column(name = "raio_metros", nullable = false)
	private int raioMetros;

	@Column(name = "criada_em", nullable = false)
	private Instant criadaEm;

	protected Inscricao() {
	}

	public Inscricao(TipoInscrito tipo, String nome, String contato, Canal canal, String endereco,
			Coordenada localizacao, int raioMetros, Instant criadaEm) {
		this.tipo = Objects.requireNonNull(tipo, "tipo");
		this.canal = Objects.requireNonNull(canal, "canal");
		this.nome = textoObrigatorio(nome, "nome", TAMANHO_MAXIMO_NOME);
		this.endereco = textoObrigatorio(endereco, "endereço", TAMANHO_MAXIMO_ENDERECO);
		this.contato = normalizarContato(contato, canal);
		this.localizacao = Objects.requireNonNull(localizacao, "localizacao");
		this.criadaEm = Objects.requireNonNull(criadaEm, "criadaEm");
		if (raioMetros < RAIO_MINIMO_METROS || raioMetros > RAIO_MAXIMO_METROS) {
			throw new RegraDeNegocioException(
					"O raio precisa estar entre " + RAIO_MINIMO_METROS + " e " + RAIO_MAXIMO_METROS + " metros.");
		}
		this.raioMetros = raioMetros;
	}

	// Regra de raio
	public boolean cobre(Coordenada ponto) {
		return localizacao.distanciaEmMetros(ponto) <= raioMetros;
	}

	// Contato mascarado
	public String contatoMascarado() {
		if (canal == Canal.EMAIL) {
			int arroba = contato.indexOf('@');
			return contato.charAt(0) + "***" + contato.substring(arroba);
		}
		return "*".repeat(contato.length() - 4) + contato.substring(contato.length() - 4);
	}

	private static String normalizarContato(String contato, Canal canal) {
		String texto = textoObrigatorio(contato, "contato", TAMANHO_MAXIMO_CONTATO);
		if (canal == Canal.EMAIL) {
			String email = texto.toLowerCase(Locale.ROOT);
			if (!EMAIL.matcher(email).matches()) {
				throw new RegraDeNegocioException("Para o canal EMAIL, o contato precisa ser um e-mail válido.");
			}
			return email;
		}
		String digitos = texto.replaceAll("\\D", "");
		if (digitos.length() < 10 || digitos.length() > 13) {
			throw new RegraDeNegocioException(
					"Para o canal " + canal + ", o contato precisa ser um telefone com DDD (10 a 13 dígitos).");
		}
		return digitos;
	}

	private static String textoObrigatorio(String valor, String campo, int tamanhoMaximo) {
		if (valor == null || valor.isBlank()) {
			throw new RegraDeNegocioException("O campo " + campo + " é obrigatório.");
		}
		String limpo = valor.strip();
		if (limpo.length() > tamanhoMaximo) {
			throw new RegraDeNegocioException("O campo " + campo + " aceita no máximo " + tamanhoMaximo + " caracteres.");
		}
		return limpo;
	}

	public Long getId() {
		return id;
	}

	public TipoInscrito getTipo() {
		return tipo;
	}

	public String getNome() {
		return nome;
	}

	public String getContato() {
		return contato;
	}

	public Canal getCanal() {
		return canal;
	}

	public String getEndereco() {
		return endereco;
	}

	public Coordenada getLocalizacao() {
		return localizacao;
	}

	public int getRaioMetros() {
		return raioMetros;
	}

	public Instant getCriadaEm() {
		return criadaEm;
	}

	@Override
	public String toString() {
		return "Inscricao[id=" + id + "]";
	}
}
