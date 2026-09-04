package com.fotolou.app.cucumber.stepdefs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class UserStepDefs extends StepDefs {

    @Autowired
    private MockMvc mockMvc;

    @When("I list users")
    public void i_list_users() throws Throwable {
        actions = mockMvc.perform(get("/api/admin/users").accept(MediaType.APPLICATION_JSON));
    }

    @Then("the operation succeeds")
    public void the_operation_succeeds() throws Throwable {
        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
}
