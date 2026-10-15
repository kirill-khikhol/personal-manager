package com.personalassistant.user.infrastructure.api;

import com.personalassistant.TestcontainersConfiguration;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAndRetrieveUserProfile() throws Exception {
        String request = """
                {
                    "timeZone": "Asia/Jerusalem",
                    "preferredLanguage": "ru"
                }
                """;

        String response = mockMvc.perform(post("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.timeZone").value("Asia/Jerusalem"))
                .andExpect(jsonPath("$.preferredLanguage").value("ru"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdProfile = objectMapper.readTree(response);
        String id = createdProfile.get("id").asText();

        mockMvc.perform(get("/api/users/profile")
                        .param("id", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.timeZone").value("Asia/Jerusalem"))
                .andExpect(jsonPath("$.preferredLanguage").value("ru"));
    }

    @Test
    void shouldRejectBlankRequiredField() throws Exception {
        String request = """
            {
                "timeZone": "",
                "preferredLanguage": "ru"
            }
            """;

        mockMvc.perform(post("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/users/profile"))
                .andExpect(jsonPath("$.details.timeZone").exists());
    }

    @Test
    void shouldRejectInvalidTimeZone() throws Exception {
        String request = """
            {
                "timeZone": "Invalid/Zone",
                "preferredLanguage": "ru"
            }
            """;

        mockMvc.perform(post("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_TIME_ZONE"));
    }

    @Test
    void shouldRejectMalformedUuid() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .param("id", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.details.id").value("Invalid value"));
    }

    @Test
    void shouldReturnNotFoundWhenProfileDoesNotExist()
            throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .param(
                                "id",
                                "00000000-0000-0000-0000-000000000001"
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }


    @Test
    void shouldUpdateExistingUserProfile() throws Exception {
        String createRequest = """
            {
                "timeZone": "Asia/Jerusalem",
                "preferredLanguage": "ru"
            }
            """;

        String createdResponse = mockMvc.perform(
                        post("/api/users/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequest)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(createdResponse)
                .get("id")
                .asText();

        String updateRequest = """
            {
                "timeZone": "Europe/London",
                "preferredLanguage": "en"
            }
            """;

        mockMvc.perform(
                        put("/api/users/profile")
                                .param("id", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.timeZone").value("Europe/London"))
                .andExpect(jsonPath("$.preferredLanguage").value("en"));

        mockMvc.perform(
                        get("/api/users/profile")
                                .param("id", id)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.timeZone").value("Europe/London"))
                .andExpect(jsonPath("$.preferredLanguage").value("en"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingProfile() throws Exception {
        String request = """
            {
                "timeZone": "Europe/London",
                "preferredLanguage": "en"
            }
            """;

        mockMvc.perform(
                        put("/api/users/profile")
                                .param(
                                        "id",
                                        "00000000-0000-0000-0000-000000000001"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

}