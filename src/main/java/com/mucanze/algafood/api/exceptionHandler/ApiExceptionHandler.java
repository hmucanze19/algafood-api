package com.mucanze.algafood.api.exceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import com.mucanze.algafood.core.validation.ValidacaoException;
import com.mucanze.algafood.domain.exception.EntidadeEmUsoException;
import com.mucanze.algafood.domain.exception.EntidadeInexistenteException;
import com.mucanze.algafood.domain.exception.NegocioException;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static final String MSG_GENERIC_FINAL_USER_ERROR = "Ocorreu um erro inesperado no sistema. "
			+ "Tente novamente e se o problema persistir, entre em contacto "
			+ "com o administrador do sistema.";
	
	@Autowired
	private MessageSource messageSource;

	@ExceptionHandler(EntidadeInexistenteException.class)
	public ResponseEntity<?> handleEntidadeInexistenteException(EntidadeInexistenteException e, WebRequest request) {
//		Problema problema = Problema.builder()
//				.dataHora(LocalDateTime.now())
//				.mensagem(e.getMessage())
//				.build();
//		
//		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problema);
		
		ProblemType problemType = ProblemType.RECURSO_INEXISTENTE_EXCEPTION;
		HttpStatus status = HttpStatus.NOT_FOUND;
		String detail = e.getMessage();
		
		Problem problem = createProblemBuilder(status, problemType, detail)
				.userMessage(detail)
				.build();
		
		return handleExceptionInternal(e, problem, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(EntidadeEmUsoException.class)
	public ResponseEntity<?> handleEntidadeEmUsoException(EntidadeEmUsoException e, WebRequest request) {
		
		ProblemType problemType = ProblemType.ENTIDADE_EM_USO_EXCEPTION;
		HttpStatus status = HttpStatus.CONFLICT;
		String detail = e.getMessage();
		
		Problem problem = createProblemBuilder(status, problemType, detail)
				.userMessage(detail)
				.build();
		
		return handleExceptionInternal(e, problem, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(NegocioException.class)
	public ResponseEntity<?> handleNegocioException(NegocioException e, WebRequest request) {
		
		ProblemType problemType = ProblemType.NEGOCIO_EXCEPTION;
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String detail = e.getMessage();
		
		Problem problem = createProblemBuilder(status, problemType, detail)
				.userMessage(MSG_GENERIC_FINAL_USER_ERROR)
				.build();
		
		return handleExceptionInternal(e, problem, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleUncaught(Exception e, WebRequest request) {
		
		ProblemType problemType = ProblemType.ERRO_DE_SISTEMA;
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		String detail = MSG_GENERIC_FINAL_USER_ERROR;
		
		e.printStackTrace();
		
		Problem problem = createProblemBuilder(status, problemType, detail).build();
		
		return handleExceptionInternal(e, problem, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(ValidacaoException.class)
	public ResponseEntity<Object> handleValidacaoException(ValidacaoException ex, WebRequest request) {
		return handleValidationInternal(ex, ex.getBindingResult(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		return ResponseEntity.status(status).headers(headers).build();
	}
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		Throwable rootCause = ExceptionUtils.getRootCause(ex);
		
		if(rootCause instanceof InvalidFormatException) {
			return handleInvalidFormat((InvalidFormatException) rootCause, headers, status, request);
		} else if(rootCause instanceof PropertyBindingException) {
			return handlePropertyBinding((PropertyBindingException) rootCause, headers, status, request);
		}
		
		ProblemType probleType = ProblemType.MENSAGEM_INCOMPREENSIVEL;
		String detail = "O corpo da requisição está inválido, verifique erro de sintaxe";
		
		Problem problem = createProblemBuilder(status, probleType, detail).build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		if(ex instanceof MethodArgumentTypeMismatchException) {
			return handleMethodArgumentTypeMismatch((MethodArgumentTypeMismatchException) ex, headers, status, request);
		}
		
		return super.handleTypeMismatch(ex, headers, status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status,
			WebRequest request) {
		
		return handleValidationInternal(ex, ex.getBindingResult(), headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		if(body == null) {
			body = Problem.builder()
					.timestamp(OffsetDateTime.now())
					.title(status.getReasonPhrase())
					.status(status.value())
					.userMessage(MSG_GENERIC_FINAL_USER_ERROR)
					.build();
		}
		else if(body instanceof String) {
			body = Problem.builder()
					.timestamp(OffsetDateTime.now())
					.title(status.getReasonPhrase())
					.status(status.value())
					.userMessage((String) body)
					.build();
		}
		return super.handleExceptionInternal(ex, body, headers, status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		ProblemType problemType = ProblemType.RECURSO_INEXISTENTE_EXCEPTION;
		String detail = String.format("O recurso '%s', que você tentou acessar não existe", ex.getRequestURL());
		
		Problem problem = createProblemBuilder(status, problemType, detail)
				.userMessage(MSG_GENERIC_FINAL_USER_ERROR)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		return handleValidationInternal(ex, ex.getBindingResult(), headers, status, request);
	}
	
	private ResponseEntity<Object> handleValidationInternal(Exception ex, BindingResult bindingResult, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		ProblemType problemType = ProblemType.DADOS_INVALIDOS;
		String detail = "Um ou mais campos estão incorrectos. Faça o preenchimento correcto e tente novamente.";
		
		List<Problem.Object> problemObjects = bindingResult.getAllErrors().stream()
				.map(objectError -> {
					
					String message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());
					
					String name = objectError.getObjectName();
					
					if(objectError instanceof FieldError) {
						name = ((FieldError) objectError).getField();
					}
					
					return Problem.Object.builder()
							.name(name)
							.userMessage(message)
							.build();
				})
				.collect(Collectors.toList());
		
		Problem problem = createProblemBuilder(status, problemType, detail)
				.userMessage(detail)
				.objects(problemObjects)
				.build();
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	private ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		ProblemType problemType = ProblemType.PARAMETRO_INVALIDO;
		String detail = String.format("O parâmetro de URL '%s' recebeu o valor '%s',"
				+ "que é de um tipo inválido. Corrija e informe um valor compatível com o tipo '%s'",
				ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
		
		Problem problem = createProblemBuilder(status, problemType, detail)
				.userMessage(MSG_GENERIC_FINAL_USER_ERROR)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	private ResponseEntity<Object> handlePropertyBinding(PropertyBindingException e, HttpHeaders headers, HttpStatus status,
			WebRequest request) {
		
		String path = joinPath(e.getPath());
		
		ProblemType problemType = ProblemType.MENSAGEM_INCOMPREENSIVEL;
		String detail = String.format("A propriedade '%s' não existe."
				+ "Corrija ou remova essa propriedade e tente novamente", path);
		
		Problem problem = createProblemBuilder(status, problemType, detail).build();
		
		return handleExceptionInternal(e, problem, headers, status, request);
	}

	private ResponseEntity<Object> handleInvalidFormat(InvalidFormatException e, HttpHeaders headers, HttpStatus status,
			WebRequest request) {
		
		String path = joinPath(e.getPath());
		
		ProblemType problemType = ProblemType.MENSAGEM_INCOMPREENSIVEL;
		String detail = String.format("A propriedade '%s' recebeu o valor '%s',"
				+ "que é de um tipo inválido. Corrija e informe um valor compatível com o tipo %s",
				path , e.getValue(), e.getTargetType().getSimpleName());
		
		Problem problem = createProblemBuilder(status, problemType, detail)
				.userMessage(MSG_GENERIC_FINAL_USER_ERROR)
				.build();
		
		return handleExceptionInternal(e, problem, headers, status, request);
	}
	
	private String joinPath(List<Reference> references) {
		return references.stream()
				.map(ref -> ref.getFieldName())
				.collect(Collectors.joining("."));
	}
	
	private Problem.ProblemBuilder createProblemBuilder(HttpStatus status,
			ProblemType problemType, String detail) {
		return Problem.builder()
				.timestamp(OffsetDateTime.now())
				.status(status.value())
				.type(problemType.getUri())
				.title(problemType.getTitle())
				.detail(detail);
	}

}
