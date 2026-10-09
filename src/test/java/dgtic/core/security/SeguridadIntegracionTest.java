package dgtic.core.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Comprueba las dos cadenas de seguridad con la aplicacion completa (perfil "test",
// base nubom_test con los usuarios de data.sql).
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegracionTest {

    private static final String PROPIETARIO = "armando@correo.com";
    private static final String CONTRASENA = "Nubom2026!"; // contrasena de desarrollo de data.sql

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ------------------------------------------------------------ cadena web

    @Test
    void unaPaginaProtegidaRedirigeAlLogin() throws Exception {
        mockMvc.perform(get("/hogar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void elFormularioAutenticaConCredencialesCorrectas() throws Exception {
        mockMvc.perform(formLogin("/login").userParameter("correo").passwordParam("contrasena")
                        .user(PROPIETARIO).password(CONTRASENA))
                .andExpect(authenticated().withUsername(PROPIETARIO).withRoles("USUARIO"))
                .andExpect(redirectedUrl("/inicio"));
    }

    @Test
    void elFormularioRechazaUnaContrasenaIncorrecta() throws Exception {
        mockMvc.perform(formLogin("/login").userParameter("correo").passwordParam("contrasena")
                        .user(PROPIETARIO).password("incorrecta"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void unPostSinTokenCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/registro").param("nombre", "X"))
                .andExpect(status().isForbidden());
    }

    @Test
    void elMismoPostConTokenCsrfLlegaAlControlador() throws Exception {
        mockMvc.perform(post("/registro").with(csrf()).param("nombre", ""))
                .andExpect(status().isOk()); // vuelve al formulario con errores de validacion
    }

    // ------------------------------------------------------------ cadena API

    @Test
    void laApiRespondeNoAutorizadoSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.ruta").value("/api/v1/productos"));
    }

    @Test
    void laApiNoExigeCsrfPeroSiCredenciales() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + PROPIETARIO + "\",\"contrasena\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conUnTokenValidoSeConsultanLosProductosDelHogar() throws Exception {
        mockMvc.perform(get("/api/v1/productos").header("Authorization", "Bearer " + accessToken(PROPIETARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.pagina").value(0));
    }

    @Test
    void unUsuarioNoPuedeVerUnHogarAjeno() throws Exception {
        mockMvc.perform(get("/api/v1/hogares/3").header("Authorization", "Bearer " + accessToken(PROPIETARIO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void soloElAdministradorModificaCatalogos() throws Exception {
        String cuerpo = "{\"nombre\":\"INVITADO\",\"descripcion\":\"Solo consulta\"}";

        mockMvc.perform(post("/api/v1/roles").header("Authorization", "Bearer " + accessToken(PROPIETARIO))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/roles").header("Authorization", "Bearer " + accessToken("admin@correo.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
    }

    @Test
    void elRefreshTokenSoloSirveUnaVez() throws Exception {
        String refreshToken = tokens(PROPIETARIO).get("refreshToken").asText();
        String cuerpo = "{\"refreshToken\":\"" + refreshToken + "\"}";

        mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());
    }

    private String accessToken(String correo) throws Exception {
        return tokens(correo).get("accessToken").asText();
    }

    private JsonNode tokens(String correo) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"" + CONTRASENA + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }
}
