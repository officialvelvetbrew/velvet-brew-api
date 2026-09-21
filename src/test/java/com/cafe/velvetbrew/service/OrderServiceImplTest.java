package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.dto.CreateOrderRequest;
import com.cafe.velvetbrew.dto.CustomerRequest;
import com.cafe.velvetbrew.dto.OrderItemRequest;
import com.cafe.velvetbrew.dto.OrderResponse;
import com.cafe.velvetbrew.entity.Customer;
import com.cafe.velvetbrew.entity.MenuItem;
import com.cafe.velvetbrew.entity.Order;
import com.cafe.velvetbrew.repository.MenuItemRepository;
import com.cafe.velvetbrew.repository.OrderRepository;
import com.cafe.velvetbrew.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private OfferApplicationService offerApplicationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeInventoryService recipeInventoryService;

    @Test
    void createOrderChargesOfferPriceNotListPrice() {
        MenuPricingService menuPricingService = new MenuPricingService(menuItemRepository);
        OrderServiceImpl orderService = new OrderServiceImpl(
                orderRepository, customerService, menuPricingService, offerApplicationService, userRepository,
                recipeInventoryService);

        MenuItem cheeseSandwich = MenuItem.builder()
                .id(10L)
                .name("Cheese Sandwich")
                .price(new BigDecimal("109.00"))
                .offerPrice(new BigDecimal("1.00"))
                .available(true)
                .build();

        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(cheeseSandwich));

        Customer customer = Customer.builder().id(1L).fullName("Test User").mobile("9999999999").build();
        when(customerService.findOrCreate(any())).thenReturn(customer);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest();
        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setFullName("Test User");
        customerRequest.setMobile("9999999999");
        request.setCustomer(customerRequest);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setMenuId(10L);
        itemRequest.setQuantity(1);
        request.setItems(List.of(itemRequest));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getSubtotal()).isEqualByComparingTo("1.00");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("1.00");
        assertThat(response.getItems().get(0).getUnitPrice()).isEqualByComparingTo("1.00");
        assertThat(response.getItems().get(0).getTotalPrice()).isEqualByComparingTo("1.00");
    }
}
