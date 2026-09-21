package com.cafe.velvetbrew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cafe.velvetbrew.common.enums.OrderStatus;
import com.cafe.velvetbrew.common.enums.PaymentStatus;
import com.cafe.velvetbrew.dto.CreateOrderRequest;
import com.cafe.velvetbrew.dto.CustomerRequest;
import com.cafe.velvetbrew.dto.OrderItemRequest;
import com.cafe.velvetbrew.entity.Customer;
import com.cafe.velvetbrew.entity.MenuItem;
import com.cafe.velvetbrew.entity.Order;
import com.cafe.velvetbrew.entity.OrderItem;
import com.cafe.velvetbrew.repository.MenuItemRepository;
import com.cafe.velvetbrew.repository.OrderRepository;
import com.cafe.velvetbrew.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderInventoryLifecycleTest {
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

    private OrderServiceImpl orderService;

    private MenuItem espresso;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, customerService,
                new MenuPricingService(menuItemRepository), offerApplicationService, userRepository,
                recipeInventoryService);

        espresso = MenuItem.builder().id(10L).name("Espresso").price(new BigDecimal("59.00")).available(true).build();
    }

    private Order pendingOrder(boolean deducted) {
        Order order = Order.builder()
                .id(1L)
                .orderNumber("VB-TEST-1")
                .customer(Customer.builder().id(1L).fullName("T").build())
                .subtotal(new BigDecimal("59.00"))
                .totalAmount(new BigDecimal("59.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .orderStatus(OrderStatus.PENDING)
                .inventoryDeducted(deducted)
                .build();
        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder().order(order).menuItem(espresso).quantity(1)
                .unitPrice(new BigDecimal("59.00")).totalPrice(new BigDecimal("59.00")).build());
        order.setOrderItems(items);
        when(orderRepository.findByOrderNumber("VB-TEST-1")).thenReturn(Optional.of(order));
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        return order;
    }

    @Test
    void scenarioA_placingAnOrderLeavesItPendingAndDoesNotTouchStock() {
        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(espresso));
        when(customerService.findOrCreate(any())).thenReturn(Customer.builder().id(1L).fullName("T").build());
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest();
        CustomerRequest customer = new CustomerRequest();
        customer.setFullName("T");
        request.setCustomer(customer);
        OrderItemRequest item = new OrderItemRequest();
        item.setMenuId(10L);
        item.setQuantity(2);
        request.setItems(List.of(item));

        var response = orderService.createOrder(request);

        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        verify(recipeInventoryService, never()).deductForOrder(any());
        verify(recipeInventoryService, never()).restockForOrder(any());
    }

    @Test
    void scenarioA_movingToIntermediateStatusesDoesNotTouchStock() {
        pendingOrder(false);

        for (OrderStatus status : List.of(OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY)) {
            orderService.updateOrderStatus("VB-TEST-1", status);
        }

        verify(recipeInventoryService, never()).deductForOrder(any());
        verify(recipeInventoryService, never()).restockForOrder(any());
    }

    @Test
    void scenarioB_completingAnOrderDeductsStockOnce() {
        Order order = pendingOrder(false);

        var response = orderService.updateOrderStatus("VB-TEST-1", OrderStatus.COMPLETED);

        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getInventoryDeducted()).isTrue();
        verify(recipeInventoryService, times(1)).deductForOrder(order);
    }

    @Test
    void scenarioC_cancellingAPendingOrderLeavesStockUnchanged() {
        pendingOrder(false);

        orderService.updateOrderStatus("VB-TEST-1", OrderStatus.CANCELLED);

        verify(recipeInventoryService, never()).deductForOrder(any());
        verify(recipeInventoryService, never()).restockForOrder(any());
    }

    @Test
    void scenarioC_rejectingAPendingOrderLeavesStockUnchanged() {
        pendingOrder(false);

        orderService.updateOrderStatus("VB-TEST-1", OrderStatus.REJECTED);

        verify(recipeInventoryService, never()).deductForOrder(any());
        verify(recipeInventoryService, never()).restockForOrder(any());
    }

    @Test
    void scenarioD_completingAnAlreadyCompletedOrderDoesNotDeductAgain() {
        Order order = pendingOrder(false);

        orderService.updateOrderStatus("VB-TEST-1", OrderStatus.COMPLETED);
        orderService.updateOrderStatus("VB-TEST-1", OrderStatus.COMPLETED);
        orderService.updateOrderStatus("VB-TEST-1", OrderStatus.COMPLETED);

        verify(recipeInventoryService, times(1)).deductForOrder(order);
    }

    @Test
    void cancellingACompletedOrderReturnsTheStockExactlyOnce() {
        Order order = pendingOrder(true);
        order.setOrderStatus(OrderStatus.COMPLETED);

        orderService.updateOrderStatus("VB-TEST-1", OrderStatus.CANCELLED);
        orderService.updateOrderStatus("VB-TEST-1", OrderStatus.CANCELLED);

        assertThat(order.getInventoryDeducted()).isFalse();
        verify(recipeInventoryService, times(1)).restockForOrder(order);
    }

    @Test
    void insufficientStockOnCompletionPropagatesSoTheStatusChangeRollsBack() {
        pendingOrder(false);
        org.mockito.Mockito.doThrow(new IllegalStateException("Insufficient stock"))
                .when(recipeInventoryService).deductForOrder(any());

        assertThatThrownBy(() -> orderService.updateOrderStatus("VB-TEST-1", OrderStatus.COMPLETED))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void placingAnOrderThatCannotBeMadeIsRejectedAndNothingIsSaved() {
        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(espresso));
        when(customerService.findOrCreate(any())).thenReturn(Customer.builder().id(1L).fullName("T").build());
        org.mockito.Mockito.doThrow(new IllegalStateException("Not enough stock to make Espresso"))
                .when(recipeInventoryService).assertStockAvailable(any());

        CreateOrderRequest request = new CreateOrderRequest();
        CustomerRequest customer = new CustomerRequest();
        customer.setFullName("T");
        request.setCustomer(customer);
        OrderItemRequest item = new OrderItemRequest();
        item.setMenuId(10L);
        item.setQuantity(500);
        request.setItems(List.of(item));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock");

        verify(orderRepository, never()).save(any());
    }
}
