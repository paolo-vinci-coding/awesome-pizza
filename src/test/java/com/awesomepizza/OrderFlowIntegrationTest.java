package com.awesomepizza;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullScenario() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        String first = placeOrder(1, 2);
        String second = placeOrder(3, 1);

        mockMvc.perform(get("/api/pizzaiolo/queue"))
                .andExpect(jsonPath("$[0].code").value(first))
                .andExpect(jsonPath("$[1].code").value(second));

        mockMvc.perform(post("/api/pizzaiolo/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(first));

        mockMvc.perform(post("/api/pizzaiolo/next"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        "An order is already in preparation (" + first + "): complete it before taking another one"));

        mockMvc.perform(get("/api/orders/" + first))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/pizzaiolo/orders/" + first + "/ready"))
                .andExpect(jsonPath("$.status").value("READY"));
        mockMvc.perform(get("/api/orders/" + first))
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.items[0].pizzaName").value("Margherita"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        mockMvc.perform(post("/api/pizzaiolo/next"))
                .andExpect(jsonPath("$.code").value(second));
        mockMvc.perform(get("/api/pizzaiolo/current"))
                .andExpect(jsonPath("$.code").value(second));
    }

    @Test
    void emptyQueue() throws Exception {
        mockMvc.perform(post("/api/pizzaiolo/next"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/pizzaiolo/current"))
                .andExpect(status().isNoContent());
    }

    private String placeOrder(long pizzaId, int quantity) throws Exception {
        String body = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"pizzaId": %d, "quantity": %d}]}
                                """.formatted(pizzaId, quantity)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.code");
    }
}
