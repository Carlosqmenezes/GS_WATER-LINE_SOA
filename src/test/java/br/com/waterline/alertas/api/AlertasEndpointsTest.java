package br.com.waterline.alertas.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AlertasEndpointsTest {

	@Autowired
	private MockMvc mvc;

	// Auxiliares
	private ResultActions postar(String rota, String json) throws Exception {
		return mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private long inscrever(String contato, double lat, double lng, int raio) throws Exception {
		String json = """
				{"tipo":"MORADOR","nome":"Morador Fictício","canal":"SMS","contato":"%s",
				 "endereco":"Rua Fictícia, 1 - Bairro Exemplo","latitude":%s,"longitude":%s,"raioMetros":%d}
				""".formatted(contato, lat, lng, raio);
		String resposta = postar("/inscricoes", json).andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(resposta, "$.id")).longValue();
	}

	private static String alertaDaApiPrincipal(int alertaId, String nivel) {
		return """
				{"alertaId":%d,"bocaDeLoboId":1,"codigoBocaDeLobo":"BL-0001",
				 "endereco":"Rua Fictícia das Águas, 100 - Bairro Exemplo",
				 "latitude":-23.55,"longitude":-46.64,"nivel":"%s",
				 "mensagem":"Água subindo rápido na boca de lobo BL-0001.","criadoEm":"%s"}
				""".formatted(alertaId, nivel, Instant.now());
	}

	// Inscrições
	@Test
	void inscricaoValidaECriadaComContatoMascarado() throws Exception {
		postar("/inscricoes", """
				{"tipo":"COMERCIANTE","nome":"Comércio Fictício","canal":"WHATSAPP","contato":"(11) 00000-0009",
				 "endereco":"Rua Fictícia, 9 - Bairro Exemplo","latitude":-23.55,"longitude":-46.64,"raioMetros":300}
				""")
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.contatoMascarado").value("*******0009"))
				.andExpect(jsonPath("$.contato").doesNotExist());
	}

	@Test
	void inscricaoSemCamposObrigatoriosDevolve400ComOsCampos() throws Exception {
		postar("/inscricoes", "{\"tipo\":\"MORADOR\",\"raioMetros\":10}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.nome").exists())
				.andExpect(jsonPath("$.erros.raioMetros").exists());
	}

	@Test
	void mesmoContatoNoMesmoCanalDevolve409() throws Exception {
		inscrever("11000000010", -23.55, -46.64, 300);
		postar("/inscricoes", """
				{"tipo":"MORADOR","nome":"Outro","canal":"SMS","contato":"11000000010",
				 "endereco":"Rua Fictícia, 2","latitude":-23.55,"longitude":-46.64,"raioMetros":300}
				""").andExpect(status().isConflict());
	}

	@Test
	void cancelarInscricaoApagaEDesvinculaOsEnvios() throws Exception {
		long id = inscrever("11000000011", -23.55010, -46.64020, 300);
		String resposta = postar("/alertas", alertaDaApiPrincipal(7001, "AlertaPrecoce"))
				.andReturn().getResponse().getContentAsString();
		long alertaId = ((Number) JsonPath.read(resposta, "$.id")).longValue();

		mvc.perform(delete("/inscricoes/" + id)).andExpect(status().isNoContent());
		mvc.perform(get("/inscricoes/" + id)).andExpect(status().isNotFound());
		mvc.perform(get("/alertas/" + alertaId + "/envios"))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].inscricaoId").value(nullValue()));
	}

	// Regra de raio
	@Test
	void alertaAvisaSoAsInscricoesCujoRaioAlcancaABocaDeLobo() throws Exception {
		long perto = inscrever("11000000020", -23.55010, -46.64020, 300);
		inscrever("11000000021", -23.56010, -46.64790, 300);

		String resposta = postar("/alertas", alertaDaApiPrincipal(7002, "AlertaPrecoce"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nivel").value("ALERTA_PRECOCE"))
				.andExpect(jsonPath("$.ativo").value(true))
				.andExpect(jsonPath("$.envios.inscritosNotificados").value(1))
				.andExpect(jsonPath("$.envios.centralNotificada").value(false))
				.andReturn().getResponse().getContentAsString();
		long alertaId = ((Number) JsonPath.read(resposta, "$.id")).longValue();

		mvc.perform(get("/alertas/" + alertaId + "/envios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].inscricaoId").value(perto))
				.andExpect(jsonPath("$[0].canal").value("SMS"))
				.andExpect(jsonPath("$[0].status").value("ENVIADO"));
	}

	@Test
	void mesmoAlertaReenviadoNaoAvisaDeNovo() throws Exception {
		inscrever("11000000030", -23.55010, -46.64020, 300);
		postar("/alertas", alertaDaApiPrincipal(7003, "AlertaPrecoce")).andExpect(status().isCreated());
		postar("/alertas", alertaDaApiPrincipal(7003, "AlertaPrecoce"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.envios.inscritosNotificados").value(1));
	}

	@Test
	void emergenciaVaiParaACentralEParaOsInscritos() throws Exception {
		inscrever("11000000040", -23.55010, -46.64020, 300);
		postar("/alertas/emergencia", alertaDaApiPrincipal(7004, "Emergencia"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nivel").value("EMERGENCIA"))
				.andExpect(jsonPath("$.envios.centralNotificada").value(true))
				.andExpect(jsonPath("$.envios.inscritosNotificados").value(1));
	}

	@Test
	void emergenciaNaRotaDeAlertaPrecoceDevolve422() throws Exception {
		postar("/alertas", alertaDaApiPrincipal(7005, "Emergencia")).andExpect(status().isUnprocessableContent());
	}

	@Test
	void alertaComNivelDesconhecidoDevolve400() throws Exception {
		postar("/alertas", alertaDaApiPrincipal(7006, "Chuva")).andExpect(status().isBadRequest());
	}

	// Alertas ativos
	@Test
	void alertasAtivosListaSoOsProximosComEmergenciaPrimeiro() throws Exception {
		postar("/alertas", alertaDaApiPrincipal(7007, "AlertaPrecoce")).andExpect(status().isCreated());
		postar("/alertas/emergencia", alertaDaApiPrincipal(7008, "Emergencia")).andExpect(status().isCreated());

		mvc.perform(get("/alertas/ativos").param("lat", "-23.5501").param("lng", "-46.6401"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].nivel").value("EMERGENCIA"))
				.andExpect(jsonPath("$[1].nivel").value("ALERTA_PRECOCE"));

		mvc.perform(get("/alertas/ativos").param("lat", "-23.60").param("lng", "-46.70"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void alertasAtivosComCoordenadaInvalidaDevolve400() throws Exception {
		mvc.perform(get("/alertas/ativos").param("lat", "-23.55")).andExpect(status().isBadRequest());
		mvc.perform(get("/alertas/ativos").param("lat", "100").param("lng", "-46.64"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.lat").exists());
		mvc.perform(get("/alertas/ativos").param("lat", "abc").param("lng", "-46.64"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void alertaInexistenteDevolve404() throws Exception {
		mvc.perform(get("/alertas/999999")).andExpect(status().isNotFound());
	}

	// Documentação
	@Test
	void documentacaoOpenApiListaOsEndpoints() throws Exception {
		mvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['/inscricoes']").exists())
				.andExpect(jsonPath("$.paths['/alertas']").exists())
				.andExpect(jsonPath("$.paths['/alertas/emergencia']").exists())
				.andExpect(jsonPath("$.paths['/alertas/ativos']").exists());
	}
}
