package com.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.security.JWTAuthenticationFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class OrderControllerUnitTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrderRepository orderRepository;

    private User testUser;
    private Item testItem;
    private UserOrder testUserOrder;
    private List<UserOrder> testUserOrderList;

    @Before
    public void setup() {
        testUser = getTestUser();
        testItem = getTestItem();
        testUserOrder = getTestUserOrder(testUser, 4);
        testUserOrderList = getTestUserOrderList(testUser);
    }

    @Test
    public void whenGetHistory_thenReturnUserOrderList() throws Exception {
        // given
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());

        given(userRepository.findByUsername(testUser.getUsername())).willReturn(testUser);
        given(orderRepository.findByUser(any(User.class))).willReturn(testUserOrderList);

        // when
        MvcResult mvcResult = mvc.perform(get("/api/order/history/"+testUser.getUsername())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        List<UserOrder> returnedUserOrderList = mvcResultToUserOrderList(mvcResult);

        // then
        Assertions.assertEquals(testUserOrderList.size(), returnedUserOrderList.size());
        Assertions.assertEquals(testUserOrderList.get(0).getTotal(), returnedUserOrderList.get(0).getTotal());
        Assertions.assertEquals(testUserOrderList.get(0).getItems().size(), returnedUserOrderList.get(0).getItems().size());
        Assertions.assertEquals(testUserOrderList.get(1).getTotal(), returnedUserOrderList.get(1).getTotal());
        Assertions.assertEquals(testUserOrderList.get(1).getItems().size(), returnedUserOrderList.get(1).getItems().size());
    }

    @Test
    public void whenSubmitOrder_thenReturnUserOrder() throws Exception {
        // given
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());
        Cart testCart = testUser.getCart();

        given(userRepository.findByUsername(testUser.getUsername())).willReturn(testUser);

        // when
        MvcResult mvcResult = mvc.perform(post("/api/order/submit/"+testUser.getUsername())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserOrder returnedUserOrder = mvcResultToUserOrder(mvcResult);

        // then
        Assertions.assertEquals(testCart.getTotal(), returnedUserOrder.getTotal());
        Assertions.assertEquals(testCart.getItems().size(), returnedUserOrder.getItems().size());
        Assertions.assertEquals(testCart.getUser().getUsername(), returnedUserOrder.getUser().getUsername());
        Assertions.assertEquals(testCart.getUser().getId(), returnedUserOrder.getUser().getId());
    }

    @Test
    public void givenUnknownUser_whenSubmitOrder_thenReturnNotFound() throws Exception {
        // given
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());

        given(userRepository.findByUsername(testUser.getUsername())).willReturn(null);

        // when
        mvc.perform(post("/api/order/submit/"+testUser.getUsername())
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenUnknownUser_whenGetHistory_thenReturnNotFound() throws Exception {
        // given
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());

        given(userRepository.findByUsername(testUser.getUsername())).willReturn(null);

        // when
        mvc.perform(get("/api/order/history/"+testUser.getUsername())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoJwt_whenSubmit_thenReturnForbidden() throws Exception {
        // when / then
        mvc.perform(post("/api/order/submit/username")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoJwt_whenGetHistory_thenReturnForbidden() throws Exception {
        // when / then
        mvc.perform(get("/api/order/history/username")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private User getTestUser() {
        User testUser = new User();
        testUser.setId(345L);
        testUser.setUsername("testuser");
        testUser.setCart(getTestCart(testUser, 4));
        return testUser;
    }

    private Item getTestItem() {
        Item testItem = new Item();
        testItem.setId(123L);
        testItem.setName("Test Item");
        testItem.setDescription("Lengthy description of Test Item");
        testItem.setPrice(BigDecimal.valueOf(5.99));
        return testItem;
    }

    private UserOrder getTestUserOrder(User user, int quantityOfTestItem) {
        UserOrder testUserOrder = new UserOrder();
        Item testItem = getTestItem();
        testUserOrder.setId(678L);
        testUserOrder.setUser(user);
        List<Item> orderItems = new ArrayList<>();
        for (int i = 0; i < quantityOfTestItem; i++)
            orderItems.add(testItem);
        testUserOrder.setItems(orderItems);
        testUserOrder.setTotal(testItem.getPrice().multiply(new BigDecimal(quantityOfTestItem)));
        return testUserOrder;
    }

    private Cart getTestCart(User user, int quantityOfTestItem) {
        Cart testCart = new Cart();
        Item testItem = getTestItem();
        testCart.setId(678L);
        testCart.setUser(user);
        List<Item> cartItems = new ArrayList<>();
        for (int i = 0; i < quantityOfTestItem; i++)
            cartItems.add(testItem);
        testCart.setItems(cartItems);
        testCart.setTotal(testItem.getPrice().multiply(new BigDecimal(quantityOfTestItem)));
        return testCart;
    }

    private List<UserOrder> getTestUserOrderList(User user) {
        List<UserOrder> testUserOrderList = new ArrayList<>();
        testUserOrderList.add(getTestUserOrder(user, 3));
        testUserOrderList.add(getTestUserOrder(user, 4));
        return testUserOrderList;
    }

    private UserOrder mvcResultToUserOrder(MvcResult result) throws Exception {
        String contentAsString = result.getResponse().getContentAsString();
        return objectMapper.readValue(contentAsString, UserOrder.class);
    }


   private List<UserOrder> mvcResultToUserOrderList(MvcResult result) throws Exception {
       String contentAsString = result.getResponse().getContentAsString();
       return objectMapper.convertValue(objectMapper.readValue(contentAsString, List.class), new TypeReference<List<UserOrder>>() { });
   }
}
