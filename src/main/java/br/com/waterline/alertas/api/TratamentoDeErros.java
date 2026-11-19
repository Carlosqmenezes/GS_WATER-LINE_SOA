package br.com.waterline.alertas.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.waterline.alertas.dominio.excecao.ConflitoException;
import br.com.waterline.alertas.dominio.excecao.RecursoNaoEncontradoException;
import br.com.waterline.alertas.dominio.excecao.RegraDeNegocioException;

@RestControllerAdvice
public class TratamentoDeErros extends ResponseEntityExceptionHandler {

	// Regras do domínio
	@ExceptionHandler(RegraDeNegocioException.class)
	public ProblemDetail regraDeNegocio(RegraDeNegocioException ex) {
		return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Regra de negócio violada", ex.getMessage());
	}

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
		return problema(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage());
	}

	@ExceptionHandler(ConflitoException.class)
	public ProblemDetail conflito(ConflitoException ex) {
		return problema(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail integridade(DataIntegrityViolationException ex) {
		return problema(HttpStatus.CONFLICT, "Conflito", "O registro conflita com outro já existente.");
	}

	// Erros de entrada
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> erros = new LinkedHashMap<>();
		for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
			erros.putIfAbsent(erro.getField(), erro.getDefaultMessage());
		}
		ProblemDetail corpo = problema(HttpStatus.BAD_REQUEST, "Dados inválidos", "Corrija os campos indicados.");
		corpo.setProperty("erros", erros);
		return ResponseEntity.badRequest().body(corpo);
	}

	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> erros = new LinkedHashMap<>();
		ex.getParameterValidationResults().forEach(resultado -> erros.putIfAbsent(
				resultado.getMethodParameter().getParameterName(),
				resultado.getResolvableErrors().getFirst().getDefaultMessage()));
		ProblemDetail corpo = problema(HttpStatus.BAD_REQUEST, "Dados inválidos", "Corrija os parâmetros indicados.");
		corpo.setProperty("erros", erros);
		return ResponseEntity.badRequest().body(corpo);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, "Corpo inválido",
				"O JSON enviado está malformado ou tem um valor fora do formato esperado."));
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, "Parâmetro ausente",
				"Informe o parâmetro " + ex.getParameterName() + "."));
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
				"O valor " + ex.getValue() + " não está no formato esperado."));
	}

	private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
		ProblemDetail corpo = ProblemDetail.forStatusAndDetail(status, detalhe);
		corpo.setTitle(titulo);
		return corpo;
	}
}
