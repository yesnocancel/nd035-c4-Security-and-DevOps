package com.example.demo;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import com.example.demo.security.JWTAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CartControllerUnitTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private ItemRepository itemRepository;

    private User testUser;
    private Item testItem;
    private Cart testCart;

    @Before
    public void setup() {
        testUser = getTestUser();
        testItem = getTestItem();
        testCart = testUser.getCart();
    }

    @Test
    public void givenAllReqs_whenAddToCart_thenReturnCorrectCart() throws Exception {
        // given
        int quantity = testCart.getItems().size();
        ModifyCartRequest testModifyCartRequest = getTestModifyCartRequest(quantity);

        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());
        String requestString = objectMapper.writeValueAsString(testModifyCartRequest);

        given(userRepository.findByUsername(testUser.getUsername())).willReturn(testUser);
        given(itemRepository.findById(testModifyCartRequest.getItemId())).willReturn(Optional.of(testItem));

        // when
        MvcResult mvcResult = mvc.perform(post("/api/cart/addToCart")
                .header("Authorization", "Bearer " + jwtToken)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Cart returnedCart = mvcResultToCart(mvcResult);

        // then
        Assertions.assertEquals(testCart.getUser().getUsername(), returnedCart.getUser().getUsername());
        Assertions.assertEquals(testCart.getTotal(), returnedCart.getTotal());
        Assertions.assertEquals(testCart.getItems(), returnedCart.getItems());
   }

    @Test
    public void givenUnknownUser_whenAddToCart_thenReturnNotFound() throws Exception {
        // given
        ModifyCartRequest testModifyCartRequest = getTestModifyCartRequest(1);
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());
        String requestString = objectMapper.writeValueAsString(testModifyCartRequest);

        given(userRepository.findByUsername(testUser.getUsername())).willReturn(null);

        // when / then
        mvc.perform(post("/api/cart/addToCart")
                .header("Authorization", "Bearer " + jwtToken)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenUnknownItem_whenAddToCart_thenReturnNotFound() throws Exception {
        // given
        ModifyCartRequest testModifyCartRequest = getTestModifyCartRequest(1);
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());
        String requestString = objectMapper.writeValueAsString(testModifyCartRequest);

        given(userRepository.findByUsername(testUser.getUsername())).willReturn(testUser);
        given(itemRepository.findById(testModifyCartRequest.getItemId())).willReturn(Optional.empty());

        // when / then
        mvc.perform(post("/api/cart/addToCart")
                .header("Authorization", "Bearer " + jwtToken)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenAllReqs_whenRemoveFromCart_thenReturnCorrectCart() throws Exception {
        // given
        int quantity = testCart.getItems().size();
        ModifyCartRequest testModifyCartRequest = getTestModifyCartRequest(quantity);

        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());
        String requestString = objectMapper.writeValueAsString(testModifyCartRequest);

        given(userRepository.findByUsername(testUser.getUsername())).willReturn(testUser);
        given(itemRepository.findById(testModifyCartRequest.getItemId())).willReturn(Optional.of(testItem));

        // when
        MvcResult mvcResult = mvc.perform(post("/api/cart/removeFromCart")
                .header("Authorization", "Bearer " + jwtToken)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Cart returnedCart = mvcResultToCart(mvcResult);

        // then
        Assertions.assertEquals(testCart.getUser().getUsername(), returnedCart.getUser().getUsername());
        Assertions.assertEquals(0, returnedCart.getTotal().compareTo(BigDecimal.ZERO));
        Assertions.assertEquals(0, returnedCart.getItems().size());
    }

    @Test
    public void givenUnknownUser_whenRemoveFromCart_thenReturnNotFound() throws Exception {
        // given
        ModifyCartRequest testModifyCartRequest = getTestModifyCartRequest(1);
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());
        String requestString = objectMapper.writeValueAsString(testModifyCartRequest);

        given(userRepository.findByUsername(testUser.getUsername())).willReturn(null);

        // when / then
        mvc.perform(post("/api/cart/removeFromCart")
                .header("Authorization", "Bearer " + jwtToken)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenUnknownItem_whenRemoveFromCart_thenReturnNotFound() throws Exception {
        // given
        ModifyCartRequest testModifyCartRequest = getTestModifyCartRequest(1);
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());
        String requestString = objectMapper.writeValueAsString(testModifyCartRequest);

        given(userRepository.findByUsername(testUser.getUsername())).willReturn(testUser);
        given(itemRepository.findById(testModifyCartRequest.getItemId())).willReturn(Optional.empty());

        // when / then
        mvc.perform(post("/api/cart/removeFromCart")
                .header("Authorization", "Bearer " + jwtToken)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoJwt_whenAddToCart_thenReturnForbidden() throws Exception {
        // when / then
        mvc.perform(post("/api/cart/addToCart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoJwt_whenRemoveFromCart_thenReturnForbidden() throws Exception {
        // when / then
        mvc.perform(post("/api/cart/removeFromCart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private Item getTestItem() {
        Item testItem = new Item();
        testItem.setId(123L);
        testItem.setName("Test Item");
        testItem.setDescription("Lengthy description of Test Item");
        testItem.setPrice(BigDecimal.valueOf(5.99));
        return testItem;
    }

   private User getTestUser() {
        User testUser = new User();
        testUser.setId(345L);
        testUser.setUsername("testuser");
        testUser.setCart(getTestCart(testUser, 4));
        return testUser;
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

    private ModifyCartRequest getTestModifyCartRequest(int quantityOfTestItem) {
        ModifyCartRequest testModifyCartRequest = new ModifyCartRequest();
        Item testItem = getTestItem();
        User testUser = getTestUser();
        testModifyCartRequest.setItemId(testItem.getId());
        testModifyCartRequest.setQuantity(quantityOfTestItem);
        testModifyCartRequest.setUsername(testUser.getUsername());
        return testModifyCartRequest;
    }

   private Cart mvcResultToCart(MvcResult result) throws Exception {
       String contentAsString = result.getResponse().getContentAsString();
       return objectMapper.readValue(contentAsString, Cart.class);
   }
}
