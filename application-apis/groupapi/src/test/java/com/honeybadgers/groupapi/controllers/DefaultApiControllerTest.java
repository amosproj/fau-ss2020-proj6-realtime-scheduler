package com.honeybadgers.groupapi.controllers;

import com.honeybadgers.groupapi.exceptions.CreationException;
import com.honeybadgers.groupapi.exceptions.JpaException;
import com.honeybadgers.groupapi.models.GroupModel;
import com.honeybadgers.groupapi.service.IGroupService;
import com.honeybadgers.groupapi.utils.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(DefaultApiController.class)
public class DefaultApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    IGroupService groupService;


    @Test
    public void testGroupCreate() throws Exception {
        GroupModel testModel = new GroupModel();
        testModel.setId("TestGroup");

        mvc.perform(post( "/api/group/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.convertObjectToJsonBytes(testModel)))
                .andExpect(status().isOk());

        verify(groupService, only())
                .createGroup(any(GroupModel.class));
    }

    @Test
    public void testGroupCreate_JpaExceptionWasThrown() throws Exception {
        GroupModel testModel = new GroupModel();
        testModel.setId("TestGroup");

        JpaException ex = new JpaException("Primary or unique constraint failed!");
        when(groupService.createGroup(any(GroupModel.class))).thenThrow(ex);

        mvc.perform(post( "/api/group/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.convertObjectToJsonBytes(testModel)))
                .andExpect(status().isBadRequest());

        verify(groupService, only())
                .createGroup(any(GroupModel.class));
    }

    @Test
    public void testGroupCreate_CreationExceptionWasThrown() throws Exception {
        GroupModel testModel = new GroupModel();
        testModel.setId("TestGroup");
        testModel.setParentId("TestParentId");

        CreationException ex = new CreationException("Parent group has tasks as children -> aborting!");
        when(groupService.createGroup(any(GroupModel.class))).thenThrow(ex);

        mvc.perform(post( "/api/group/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.convertObjectToJsonBytes(testModel)))
                .andExpect(status().isBadRequest());

        verify(groupService, only())
                .createGroup(any(GroupModel.class));
    }

    @Test
    public void testGroupCreate_invalidModel() throws Exception {

        mvc.perform(post( "/api/group/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{" +
                                "\"id\":\"" + UUID.randomUUID().toString() + "\"," +
                                "\"group_id\":\"TestGroup\"," +
                                "\"mode\":\"Solo\"" +
                        "}"
                ))
                .andExpect(status().isBadRequest());
    }
}
