package org.igov.service.controller.central;

import org.igov.model.server.Server;
import org.igov.util.JSON.JsonRestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("default")
@ContextConfiguration(classes = IntegrationTestsApplicationConfiguration.class)
public class SubjectServerControllerScenario {
    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=UTF-8";
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void shouldSuccessfullyGetServer() throws Exception {

        Long testServerId = 5L;//0L;

        String jsonData = mockMvc.perform(get("/subject/getServer")
                .param("nID", testServerId.toString())).
                andExpect(status().isOk()).
                andExpect(content().contentType(APPLICATION_JSON_CHARSET_UTF_8)).
                andExpect(jsonPath("$", not(empty()))).
                andReturn().getResponse().getContentAsString();
        Server server = JsonRestUtils.readObject(jsonData, Server.class);
        Assert.assertNotNull(server);
        Assert.assertNotNull(server.getsID());
        Assert.assertEquals(testServerId, server.getId());
    }

    @Ignore
    @Test
    public void shouldFailToGetServer() throws Exception {

        Long wrongServerId = -1L;

        mockMvc.perform(get("/subject/getServer")
                .param("nID", wrongServerId.toString())).
                andExpect(status().is5xxServerError());
    }

    @Ignore
    @Test
    public void shouldReturnJsonWithoutLoginDuplications() throws Exception {
        String withDetails = mockMvc.perform(get("/subject/getSubjectsBy")
                .param("saAccount", "[\"Barmaley\",\"GrekD\"]")
                .param("nID_SubjectAccountType", "1"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(withDetails);

        String outputWithoutDetails = mockMvc.perform(get("/subject/getSubjectsBy")
                .param("saAccount", "[\"Barmaley\",\"GrekD\"]")
                .param("nID_SubjectAccountType", "1")
                .param("bSkipDetails", "true"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(outputWithoutDetails);
    }
    
    @Ignore
    @Test
    public void shouldGetActionKVED() throws Exception {
        String withDetails = mockMvc.perform(get("/subject/getActionKVED2")
                .param("sID", "03"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(withDetails);

        withDetails = mockMvc.perform(get("/subject/getActionKVED2")
                .param("sID", "03")
                .param("sNote", "Морське"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(withDetails);

        withDetails = mockMvc.perform(get("/subject/getActionKVED2")
                .param("sNote", "Вирощування"))
                .andReturn().getResponse()                
                .getContentAsString();
        System.out.println(withDetails);

        withDetails = mockMvc.perform(get("/subject/getActionKVED")
                .param("sFind", "03"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(withDetails);

    
    }
    
}
