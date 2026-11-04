package com.springpm.springpm.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Database and library messages must never reach the client.
 */
class GlobalExceptionHandlerTest {

    @RestController
    static class Failing {
        @GetMapping("/sql")
        String sql() {
            throw new DataIntegrityViolationException("could not execute batch",
                    new SQLException("ERROR: null value in column \"role_id\" of relation \"workspace_members\"; insert into workspace_members (...)"));
        }

        @GetMapping("/bug")
        String bug() {
            throw new IllegalStateException("User ID not set in TenantContext at com.springpm.secret.Internal");
        }

        @GetMapping("/auth")
        String auth() {
            throw new UnauthorizedException("Invalid email or password");
        }

        @GetMapping("/things/{id}")
        String thing(@PathVariable UUID id) {
            return id.toString();
        }
    }

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new Failing())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void constraintViolationsHideTheSql() throws Exception {
        MvcResult result = mvc.perform(get("/sql"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("DATA_CONFLICT"))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("insert into", "workspace_members", "role_id", "batch");
        assertThat(body).contains("Reference: ");
    }

    @Test
    void unexpectedErrorsHideTheirMessage() throws Exception {
        MvcResult result = mvc.perform(get("/bug"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andReturn();
        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("TenantContext", "com.springpm", "secret")
                .contains("Something went wrong on our side. Reference: ");
    }

    @Test
    void ourOwnMessagesStillComeThrough() throws Exception {
        mvc.perform(get("/auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void aBadIdIsABadRequestNotAServerError() throws Exception {
        mvc.perform(get("/things/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for id."));
    }
}
