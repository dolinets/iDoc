package org.igov.service.business.subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.service.controller.IntegrationTestsApplicationConfiguration;
import org.igov.util.JSON.JsonRestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author Oleksandr Belichenko
 */
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestsApplicationConfiguration.class)
public class SubjectGroupTreeServiceTest {
    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=UTF-8";
    private static final String ORGAN = "Organ";
    private static final String HUMAN = "Human";
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    
    private RequestBuilder requestBuilder;
    
    @Autowired
    private SubjectGroupDao oSubjectGroupDao;       
    
    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    @Ignore
    @Test
    public void checkAllSubjects() throws Exception {
        String sURL_Template = "/subject/group/checkAllSubjects?";
                
        Map<String, List<SubjectGroup>> mResult = new HashMap<>();
        List<SubjectGroup> aHierarhyOrgan = new ArrayList<>();
        List<SubjectGroup> aHierarhyHuman = new ArrayList<>();
        List<SubjectGroup> aHumanWithoutCompany = new ArrayList<>();
        List<SubjectGroup> aGroupWithoutSubject = new ArrayList<>();
        
        Integer nID = 0;
        String sTypeOrgan = ORGAN;
        String sTypeHuman = HUMAN;
        List <SubjectGroup> oSubjectGroup = oSubjectGroupDao.findAll();
        Integer count = oSubjectGroup.size();      
                        
        String sURLOrgan = sURL_Template + "nID=" + nID + "&count=" + count + "&sType=" + sTypeOrgan;  
        System.out.printf("\nsURLOrgan: " + sURLOrgan);
        requestBuilder = get(sURLOrgan);        
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
        mResult = JsonRestUtils.readObject(fetchJSONData(requestBuilder), Map.class);
        
        aHierarhyHuman = mResult.get("HierarhyHuman_InError");
        aHierarhyOrgan = mResult.get("HierarhyOrgan_InError");
        aHumanWithoutCompany = mResult.get("Human_without_company");
        aGroupWithoutSubject = mResult.get("aGroupWithoutSubject");

        Assert.assertNotEquals(0, aHierarhyHuman.size());
        Assert.assertNotEquals(0, aHierarhyOrgan.size());
        Assert.assertNotEquals(0, aHumanWithoutCompany.size());
        Assert.assertNotEquals(0, aGroupWithoutSubject.size());
        
                                                      
        String sURLHuman = sURL_Template + "nID=" + nID + "&count=" + count + "&sType=" + sTypeHuman;      
        System.out.printf("\nsURLHuman: " + sURLHuman);
        
        requestBuilder = get(sURLHuman);        
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
        mResult = JsonRestUtils.readObject(fetchJSONData(requestBuilder), Map.class);
        
        aHierarhyHuman = mResult.get("HierarhyHuman_InError");
        aHierarhyOrgan = mResult.get("HierarhyOrgan_InError");
        aHumanWithoutCompany = mResult.get("Human_without_company");
        aGroupWithoutSubject = mResult.get("aGroupWithoutSubject");

        Assert.assertNotEquals(0, aHierarhyHuman.size());
        Assert.assertNotEquals(0, aHierarhyOrgan.size());
        Assert.assertNotEquals(0, aHumanWithoutCompany.size());
        Assert.assertNotEquals(0, aGroupWithoutSubject.size());
    }

    private String fetchJSONData(RequestBuilder requestBuilder) throws Exception {
        return mockMvc.perform(requestBuilder).
                andExpect(status().isOk()).
                andExpect(content().contentType(APPLICATION_JSON_CHARSET_UTF_8)).
                andReturn().getResponse().getContentAsString();
    }    
}