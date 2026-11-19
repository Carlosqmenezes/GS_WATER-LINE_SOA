package br.com.waterline.alertas.api;

import java.net.URI;
import java.time.Clock;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.waterline.alertas.api.dto.InscricaoRequest;
import br.com.waterline.alertas.api.dto.InscricaoResponse;
import br.com.waterline.alertas.servico.InscricaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/inscricoes")
@Tag(name = "Inscrições", description = "Moradores e comerciantes que querem ser avisados")
public class InscricaoController {

	private final InscricaoService servico;
	private final Clock relogio;

	public InscricaoController(InscricaoService servico, Clock relogio) {
		this.servico = servico;
		this.relogio = relogio;
	}

	@PostMapping
	@Operation(summary = "Inscrever morador ou comerciante",
			description = "Registra quem quer receber alertas num raio de 50 a 2000 metros ao redor do endereço.")
	@ApiResponse(responseCode = "201", description = "Inscrição criada")
	@ApiResponse(responseCode = "400", description = "Campos ausentes ou fora do formato", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "409", description = "Contato já inscrito neste canal", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "422", description = "Contato incompatível com o canal", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public ResponseEntity<InscricaoResponse> inscrever(@Valid @RequestBody InscricaoRequest requisicao) {
		var inscricao = servico.inscrever(requisicao.paraDominio(relogio.instant()));
		return ResponseEntity.created(URI.create("/inscricoes/" + inscricao.getId()))
				.body(InscricaoResponse.de(inscricao));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar inscrição", description = "O contato volta mascarado.")
	@ApiResponse(responseCode = "200", description = "Inscrição encontrada")
	@ApiResponse(responseCode = "404", description = "Inscrição não encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public InscricaoResponse buscar(@PathVariable Long id) {
		return InscricaoResponse.de(servico.buscar(id));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Cancelar inscrição",
			description = "Apaga a inscrição e os dados pessoais. O histórico de envios fica sem o vínculo.")
	@ApiResponse(responseCode = "204", description = "Inscrição cancelada")
	@ApiResponse(responseCode = "404", description = "Inscrição não encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public void cancelar(@PathVariable Long id) {
		servico.cancelar(id);
	}
}
