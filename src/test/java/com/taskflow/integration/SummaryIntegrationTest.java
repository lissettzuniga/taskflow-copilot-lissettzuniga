package com.taskflow.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SummaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String tokenDe(String username, String password) throws Exception {
        String body = """
                { "username": "%s", "password": "%s" }
                """.formatted(username, password);
        String json = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.token");
    }

    @Test
    @DisplayName("GET /projects/{id}/summary sin token → 401")
    void getSummary_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/projects/2/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /projects/1/summary con token devuelve los contadores semilla")
    void getSummary_project1_conToken_devuelveEsperado() throws Exception {
        String token = tokenDe("ana", "ana123");
        mockMvc.perform(get("/projects/1/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.projectName").isNotEmpty())
                .andExpect(jsonPath("$.totalTasks").value(5))
                .andExpect(jsonPath("$.byStatus.TODO").value(3))
                .andExpect(jsonPath("$.byStatus.IN_PROGRESS").value(1))
                .andExpect(jsonPath("$.byStatus.DONE").value(1))
                .andExpect(jsonPath("$.overdue").value(0));
    }

    @Test
    @DisplayName("GET /projects/2/summary con token devuelve los contadores semilla")
    void getSummary_project2_conToken_devuelveEsperado() throws Exception {
        String token = tokenDe("ana", "ana123");
        mockMvc.perform(get("/projects/2/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(2))
                .andExpect(jsonPath("$.projectName").value("App Móvil"))
                .andExpect(jsonPath("$.totalTasks").value(4))
                .andExpect(jsonPath("$.byStatus.TODO").value(1))
                .andExpect(jsonPath("$.byStatus.IN_PROGRESS").value(2))
                .andExpect(jsonPath("$.byStatus.DONE").value(1))
                .andExpect(jsonPath("$.overdue").value(1));
    }

    @Test
    @DisplayName("GET /projects/3/summary con token devuelve ceros")
    void getSummary_project3_conToken_devuelveCeros() throws Exception {
        String token = tokenDe("ana", "ana123");
        mockMvc.perform(get("/projects/3/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(0))
                .andExpect(jsonPath("$.byStatus.TODO").value(0))
                .andExpect(jsonPath("$.byStatus.IN_PROGRESS").value(0))
                .andExpect(jsonPath("$.byStatus.DONE").value(0))
                .andExpect(jsonPath("$.overdue").value(0));
    }

    @Test
    @DisplayName("GET /projects/99/summary con token devuelve 404")
    void getSummary_projectInexistente_devuelve404() throws Exception {
        String token = tokenDe("ana", "ana123");
        mockMvc.perform(get("/projects/99/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
