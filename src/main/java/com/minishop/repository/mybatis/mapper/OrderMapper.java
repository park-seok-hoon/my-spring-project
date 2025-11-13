package com.minishop.repository.mybatis.mapper;

import com.minishop.domain.Orders;
import com.minishop.domain.OrderItems;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    void insertOrder(Orders order); // 주문 저장

    void insertOrderItem(OrderItems orderItems); // 주문상품 저장

    Orders findById(Long id); // 주문 단건 조회

    List<OrderItems> findOrderItemsByOrderId(Long orderId); // 주문의 상품 목록 조회

    List<Orders> findAll(); // 전체 주문 조회

    void updateOrderStatus(@Param("orderId") Long orderId, @Param("status") String status); //주문 상태 변경

    void updateOrder(Orders order); //주문 수정
}
