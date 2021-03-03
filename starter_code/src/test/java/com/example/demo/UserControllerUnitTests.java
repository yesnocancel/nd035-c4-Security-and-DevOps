package com.example.demo;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.security.JWTAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerUnitTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CartRepository cartRepository;

    @Test
    public void givenUser_whenGetUsername_thenReturnCorrectUser() throws Exception {
        // given
        User testUser = getTestUser();
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());

        given(userRepository.findByUsername(testUser.getUsername())).willReturn(testUser);

        // when
        MvcResult mvcResult = mvc.perform(get("/api/user/" + testUser.getUsername())
                .header("Authorization", "Bearer " + jwtToken)
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
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());

        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));

        // when
        MvcResult mvcResult = mvc.perform(get("/api/user/id/"+testUser.getId())
                .header("Authorization", "Bearer " + jwtToken)
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

    @Test
    public void givenPasswordTooShort_whenCreateUser_thenReturnBadRequest() throws Exception {
        // given
        CreateUserRequest testCreateUserRequest = getTestCreateUserRequest();
        testCreateUserRequest.setPassword("short");
        testCreateUserRequest.setConfirmPassword("short");

        String requestString = objectMapper.writeValueAsString(testCreateUserRequest);

        // when / then
        mvc.perform(post("/api/user/create")
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenPasswordConfirmNotEqual_whenCreateUser_thenReturnBadRequest() throws Exception {
        // given
        CreateUserRequest testCreateUserRequest = getTestCreateUserRequest();
        testCreateUserRequest.setConfirmPassword("typoinconfirm");

        String requestString = objectMapper.writeValueAsString(testCreateUserRequest);

        // when / then
        mvc.perform(post("/api/user/create")
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoJwt_whenGetUsername_thenReturnUnauthorized() throws Exception {
        // when / then
        mvc.perform(get("/api/user/username")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenNoJwt_whenGetUserId_thenReturnUnauthorized() throws Exception {
        // when / then
        mvc.perform(get("/api/user/id/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
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
        testCreateUserRequest.setPassword("testpassword");
        testCreateUserRequest.setConfirmPassword("testpassword");
        return testCreateUserRequest;
    }

   private User mvcResultToUser(MvcResult result) throws Exception {
       String contentAsString = result.getResponse().getContentAsString();
       return objectMapper.readValue(contentAsString, User.class);
   }
}
