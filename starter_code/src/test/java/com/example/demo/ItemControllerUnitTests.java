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
import com.example.demo.model.requests.ModifyCartRequest;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ItemControllerUnitTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRepository itemRepository;

    private User testUser;
    private Item testItem1;
    private Item testItem2;
    private List<Item> testItemList;

    @Before
    public void setup() {
        testUser = getTestUser();
        testItem1 = getTestItem1();
        testItem2 = getTestItem2();
        testItemList = getTestItemList();
    }

    @Test
    public void whenGetItems_thenReturnItemList() throws Exception {
        // given
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());

        given(itemRepository.findAll()).willReturn(testItemList);

        // when
        MvcResult mvcResult = mvc.perform(get("/api/item")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        List<Item> returnedItemList = mvcResultToItemList(mvcResult);
        Item returnedItem1 = (Item)returnedItemList.get(0);
        Item returnedItem2 = (Item)returnedItemList.get(1);

        // then
        Assertions.assertEquals(testItemList.size(), returnedItemList.size());
        Assertions.assertEquals(testItem1.getPrice(), returnedItem1.getPrice());
        Assertions.assertEquals(testItem1.getName(), returnedItem1.getName());
        Assertions.assertEquals(testItem1.getDescription(), returnedItem1.getDescription());
        Assertions.assertEquals(testItem2.getPrice(), returnedItem2.getPrice());
        Assertions.assertEquals(testItem2.getName(), returnedItem2.getName());
        Assertions.assertEquals(testItem2.getDescription(), returnedItem2.getDescription());
   }

    @Test
    public void whenGetSingleItemById_thenReturnItem() throws Exception {
        // given
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());

        given(itemRepository.findById(testItem1.getId())).willReturn(Optional.of(testItem1));

        // when
        MvcResult mvcResult = mvc.perform(get("/api/item/"+testItem1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Item returnedItem = mvcResultToItem(mvcResult);

        // then
        Assertions.assertEquals(testItem1.getPrice(), returnedItem.getPrice());
        Assertions.assertEquals(testItem1.getName(), returnedItem.getName());
        Assertions.assertEquals(testItem1.getDescription(), returnedItem.getDescription());
    }

    @Test
    public void whenGetSingleItemByName_thenReturnItemList() throws Exception {
        // given
        String jwtToken = JWTAuthenticationFilter.createToken(testUser.getUsername());
        List<Item> singleItemList = Arrays.asList(testItem1);

        given(itemRepository.findByName(testItem1.getName())).willReturn(singleItemList);

        // when
        MvcResult mvcResult = mvc.perform(get("/api/item/name/"+testItem1.getName())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        List<Item> returnedItemList = mvcResultToItemList(mvcResult);

        // then
        Assertions.assertEquals(1, returnedItemList.size());
        Assertions.assertEquals(testItem1.getPrice(), returnedItemList.get(0).getPrice());
        Assertions.assertEquals(testItem1.getName(), returnedItemList.get(0).getName());
        Assertions.assertEquals(testItem1.getDescription(), returnedItemList.get(0).getDescription());
    }

    @Test
    public void givenNoJwt_whenListAllItems_thenReturnForbidden() throws Exception {
        // when / then
        mvc.perform(get("/api/item")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private User getTestUser() {
        User testUser = new User();
        testUser.setId(345L);
        testUser.setUsername("testuser");
        return testUser;
    }

    private Item getTestItem1() {
        Item testItem = new Item();
        testItem.setId(123L);
        testItem.setName("Test Item 1");
        testItem.setDescription("Lengthy description of Test Item 1");
        testItem.setPrice(BigDecimal.valueOf(5.99));
        return testItem;
    }

    private Item getTestItem2() {
        Item testItem = new Item();
        testItem.setId(124L);
        testItem.setName("Test Item 2");
        testItem.setDescription("Even lengthier description of Test Item 2");
        testItem.setPrice(BigDecimal.valueOf(3.54));
        return testItem;
    }

    private List<Item> getTestItemList() {
        List<Item> testItemList = new ArrayList<>();
        testItemList.add(getTestItem1());
        testItemList.add(getTestItem2());
        return testItemList;
    }

    private Item mvcResultToItem(MvcResult result) throws Exception {
        String contentAsString = result.getResponse().getContentAsString();
        return objectMapper.readValue(contentAsString, Item.class);
    }

   private List<Item> mvcResultToItemList(MvcResult result) throws Exception {
       String contentAsString = result.getResponse().getContentAsString();
       // return objectMapper.readValue(contentAsString, List.class);
       return objectMapper.convertValue(objectMapper.readValue(contentAsString, List.class), new TypeReference<List<Item>>() { });
   }
}
