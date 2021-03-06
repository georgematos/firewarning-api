package com.fortalezasec.firewarning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortalezasec.firewarning.domain.Incidente;
import com.fortalezasec.firewarning.domain.NivelPerigo;
import com.fortalezasec.firewarning.domain.Status;
import com.fortalezasec.firewarning.domain.DTOs.EmpresaDTO;
import com.fortalezasec.firewarning.domain.DTOs.EmpresaFavoritaDTO;
import com.fortalezasec.firewarning.security.jwt.JwtUtils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class EmpresaControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  private JwtUtils jwtUtils;

  String autheticate(String username, String password) {
    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(username, password));
    String token = jwtUtils.generateJwtToken(authentication);
    return "Bearer " + token;
  };

  @Test
  void shouldListAllEmpresasAndReturnStatus200Ok() throws Exception {
    String token = autheticate("zecantor@texaco.com", "senha");
    MvcResult result = mockMvc.perform(get("/empresas").header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(status().isOk()).andReturn();

    String empresasJsonResponse = result.getResponse().getContentAsString();

    EmpresaDTO[] empresasConverted = objectMapper.readValue(empresasJsonResponse, EmpresaDTO[].class);

    assertEquals("05014725000152", empresasConverted[0].getCnpj());
    assertEquals("03021302000134", empresasConverted[1].getCnpj());
    assertEquals("69855137000124", empresasConverted[2].getCnpj());

  }

  @Test
  void shouldListAllIncidentesAndReturnStatus200Ok() throws Exception {
    String token = autheticate("zecantor@texaco.com", "senha");
    MvcResult result = mockMvc.perform(get("/empresas/incidentes").header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(status().isOk()).andReturn();

    String empresasJsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

    Incidente[] incidenteConverted = objectMapper.readValue(empresasJsonResponse, Incidente[].class);

    assertEquals("05014725000152", incidenteConverted[0].getCnpjEmpresa());
    assertEquals("03021302000134", incidenteConverted[5].getCnpjEmpresa());

  }

  @Test
  void shouldListIncidentesFilterByCNPJAndReturnStatus200Ok() throws Exception {
    String token = autheticate("zecantor@texaco.com", "senha");
    MvcResult result = mockMvc
        .perform(get("/empresas/incidentes?tipo=cnpj&valor=03021302000134").header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(status().isOk()).andReturn();

    String empresasJsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

    Incidente[] incidenteConverted = objectMapper.readValue(empresasJsonResponse, Incidente[].class);

    assertEquals("03021302000134", incidenteConverted[0].getCnpjEmpresa());
    assertEquals("Vazamento de óleo no setor 4", incidenteConverted[0].getComentario());
    assertEquals("03021302000134", incidenteConverted[1].getCnpjEmpresa());
    assertEquals("Container rachado no pier 76", incidenteConverted[1].getComentario());

  }

  @Test
  void shouldReturnEmpresaFavoritaAndReturnStatus200Ok() throws Exception {
    String token = autheticate("zecantor@texaco.com", "senha");
    MvcResult result = mockMvc.perform(get("/empresas/favorita").header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(status().isOk()).andReturn();

    String empresaFavoritaJsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

    EmpresaFavoritaDTO empresaFavoritaConverted = objectMapper.readValue(empresaFavoritaJsonResponse,
        EmpresaFavoritaDTO.class);

    assertEquals("69855137000124", empresaFavoritaConverted.getCnpj());
    assertEquals("Petrobras", empresaFavoritaConverted.getFantasia());
    assertEquals(NivelPerigo.DANGER, empresaFavoritaConverted.getNivelPerigo());
    assertEquals("Foco de incendio!!!", empresaFavoritaConverted.getComentario());

  }

  @Test
  void ShouldRegisterAnIncicentAndReturnStatus201Created() throws Exception {
    String token = autheticate("pattyr@tal.br", "senha");
    String incidenteJson = "{\"nivelPerigo\":\"DANGER\",\"comentario\":\"Explosão de tubulação no setor 5\",\"data\":\"2020/12/26 19:02:37\",\"status\":\"ABERTO\"}";

    MvcResult result = mockMvc.perform(post("/empresas/05014725000152").header(HttpHeaders.AUTHORIZATION, token)
        .contentType("application/json").content(incidenteJson)).andExpect(status().isCreated()).andReturn();

    String incidenteJsonResult = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

    Incidente incidenteRegistrado = objectMapper.readValue(incidenteJsonResult, Incidente.class);

    assertEquals(9L, incidenteRegistrado.getId());
    assertEquals("05014725000152", incidenteRegistrado.getCnpjEmpresa());
    assertEquals(NivelPerigo.DANGER, incidenteRegistrado.getNivelPerigo());
    assertEquals("Explosão de tubulação no setor 5", incidenteRegistrado.getComentario());
    assertEquals(LocalDateTime.of(2020, 12, 26, 19, 2, 37), incidenteRegistrado.getData());
    assertEquals(Status.ABERTO, incidenteRegistrado.getStatus());
  }

  @Test
  void ShouldUpdateAnIncicentAndReturnStatus200Ok() throws Exception {
    String token = autheticate("pattyr@tal.br", "senha");
    String incidenteJson = "{\"nivelPerigo\":\"DANGER\",\"comentario\":\"Explosão de tubulação no setor 5\",\"status\":\"RESOLVIDO\",\"dataResolucao\":\"2020/12/27 20:02:37\"}";

    MvcResult result = mockMvc.perform(put("/empresas/1").header(HttpHeaders.AUTHORIZATION, token)
        .contentType("application/json").content(incidenteJson)).andExpect(status().isOk()).andReturn();

    String incidenteJsonResult = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

    Incidente incidenteRegistrado = objectMapper.readValue(incidenteJsonResult, Incidente.class);

    assertEquals(1L, incidenteRegistrado.getId());
    assertEquals("05014725000152", incidenteRegistrado.getCnpjEmpresa());
    assertEquals(NivelPerigo.DANGER, incidenteRegistrado.getNivelPerigo());
    assertEquals("Explosão de tubulação no setor 5", incidenteRegistrado.getComentario());
    assertEquals(LocalDateTime.of(2020, 12, 27, 20, 2, 37), incidenteRegistrado.getDataResolucao());
    assertEquals(Status.RESOLVIDO, incidenteRegistrado.getStatus());
  }
}
