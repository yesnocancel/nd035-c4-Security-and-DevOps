package com.example.demo;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerUnitTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private ItemRepository itemRepository;
    
    @Test
    public void givenUser_whenGetUsername_thenReturnCorrectUser() throws Exception {
        // given
        User testUser = getTestUser();
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(testUser);

        // when
        MvcResult mvcResult = mvc.perform(get("/api/user/testuser")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        User returnedUser = mvcResultToUser(mvcResult);

        // then
        Assertions.assertEquals(testUser.getId(), returnedUser.getId());
        Assertions.assertEquals(testUser.getUsername(), returnedUser.getUsername());
   }

    @Test
    public void givenUser_whenGetUserId_thenReturnCorrectUser() throws Exception {
        // given
        User testUser = getTestUser();
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));

        // when
        MvcResult mvcResult = mvc.perform(get("/api/user/id/"+testUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        User returnedUser = mvcResultToUser(mvcResult);

        // then
        Assertions.assertEquals(testUser.getId(), returnedUser.getId());
        Assertions.assertEquals(testUser.getUsername(), returnedUser.getUsername());
    }

    @Test
    public void givenCreateUserRequest_whenCreateUser_thenReturnCorrectUser() throws Exception {
        // given
        User testUser = getTestUser();
        CreateUserRequest testCreateUserRequest = getTestCreateUserRequest();
        String requestString = objectMapper.writeValueAsString(testCreateUserRequest);

        // when
        MvcResult mvcResult = mvc.perform(post("/api/user/create")
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        User returnedUser = mvcResultToUser(mvcResult);

        // then
        Assertions.assertEquals(testUser.getUsername(), returnedUser.getUsername());
    }

   private User getTestUser() {
        User testUser = new User();
        testUser.setId(345L);
        testUser.setUsername("testuser");
        testUser.setCart(new Cart());
        return testUser;
   }

    private CreateUserRequest getTestCreateUserRequest() {
        CreateUserRequest testCreateUserRequest = new CreateUserRequest();
        testCreateUserRequest.setUsername("testuser");
        return testCreateUserRequest;
    }

   private User mvcResultToUser(MvcResult result) throws Exception {
       String contentAsString = result.getResponse().getContentAsString();
       return objectMapper.readValue(contentAsString, User.class);
   }
}
