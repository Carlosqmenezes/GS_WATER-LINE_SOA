package br.com.waterline.alertas.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.waterline.alertas.api.dto.AlertaAtivoResponse;
import br.com.waterline.alertas.api.dto.AlertaRequest;
import br.com.waterline.alertas.api.dto.AlertaResponse;
import br.com.waterline.alertas.api.dto.EnvioResponse;
import br.com.waterline.alertas.dominio.Coordenada;
import br.com.waterline.alertas.dominio.NivelAlerta;
import br.com.waterline.alertas.servico.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@RestController
@RequestMapping("/alertas")
@Tag(name = "Alertas", description = "Alertas que chegam da API principal e os avisos enviados")
public class AlertaController {

	private final AlertaService servico;

	public AlertaController(AlertaService servico) {
		this.servico = servico;
	}

	@PostMapping
	@Operation(summary = "Receber alerta precoce",
			description = "Chamado pela API principal quando a água sobe rápido. Avisa as inscrições cujo raio "
					+ "alcança a boca de lobo. Reenviar o mesmo alertaId não avisa de novo.")
	@ApiResponse(responseCode = "201", description = "Alerta registrado e avisos enviados")
	@ApiResponse(responseCode = "200", description = "Alerta já recebido antes; nada foi reenviado")
	@ApiResponse(responseCode = "400", description = "Campos ausentes ou fora do formato", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "422", description = "Nível de emergência enviado na rota errada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public ResponseEntity<AlertaResponse> receberAlerta(@Valid @RequestBody AlertaRequest requisicao) {
		return registrar(requisicao, NivelAlerta.ALERTA_PRECOCE);
	}

	@PostMapping("/emergencia")
	@Operation(summary = "Receber emergência",
			description = "Chamado pela API principal no nível crítico. Simula o envio à central "
					+ "(CGE / Defesa Civil) e também avisa as inscrições no raio.")
	@ApiResponse(responseCode = "201", description = "Emergência registrada e enviada à central")
	@ApiResponse(responseCode = "200", description = "Emergência já recebida antes; nada foi reenviado")
	@ApiResponse(responseCode = "400", description = "Campos ausentes ou fora do formato", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "422", description = "Alerta precoce enviado na rota de emergência", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public ResponseEntity<AlertaResponse> receberEmergencia(@Valid @RequestBody AlertaRequest requisicao) {
		return registrar(requisicao, NivelAlerta.EMERGENCIA);
	}

	@GetMapping("/ativos")
	@Operation(summary = "Alertas ativos perto de um ponto",
			description = "Lista os alertas ainda válidos no raio de consulta (1000 m por padrão), com as "
					+ "emergências primeiro e depois os mais próximos.")
	@ApiResponse(responseCode = "200", description = "Lista, possivelmente vazia")
	@ApiResponse(responseCode = "400", description = "Latitude ou longitude ausente ou inválida", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public List<AlertaAtivoResponse> ativos(
			@Parameter(description = "Latitude", example = "-23.5501")
			@RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
			@Parameter(description = "Longitude", example = "-46.6401")
			@RequestParam @DecimalMin("-180") @DecimalMax("180") double lng) {
		return servico.ativosPerto(new Coordenada(lat, lng)).stream().map(AlertaAtivoResponse::de).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar alerta", description = "Mostra o alerta e o resumo dos avisos.")
	@ApiResponse(responseCode = "200", description = "Alerta encontrado")
	@ApiResponse(responseCode = "404", description = "Alerta não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public AlertaResponse buscar(@PathVariable Long id) {
		var detalhe = servico.detalhar(id);
		return AlertaResponse.de(detalhe.alerta(), detalhe.resumo(), servico.ativo(detalhe.alerta()));
	}

	@GetMapping("/{id}/envios")
	@Operation(summary = "Envios do alerta", description = "Para quem o aviso foi, por qual canal e o resultado.")
	@ApiResponse(responseCode = "200", description = "Envios do alerta")
	@ApiResponse(responseCode = "404", description = "Alerta não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public List<EnvioResponse> envios(@PathVariable Long id) {
		return servico.envios(id).stream().map(EnvioResponse::de).toList();
	}

	private ResponseEntity<AlertaResponse> registrar(AlertaRequest requisicao, NivelAlerta nivel) {
		var registro = servico.registrar(requisicao.paraComando(), nivel);
		var corpo = AlertaResponse.de(registro.alerta(), registro.resumo(), servico.ativo(registro.alerta()));
		if (!registro.novo()) {
			return ResponseEntity.ok(corpo);
		}
		return ResponseEntity.created(URI.create("/alertas/" + registro.alerta().getId())).body(corpo);
	}
}
