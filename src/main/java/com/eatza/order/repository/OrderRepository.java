package com.eatza.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eatza.order.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query(nativeQuery = true, value = "select * from orders where id = ?1 and customer_id = ?2 and status not in ('CANCELLED', 'COMPLETED')")
	Optional<Order> findActiveOrderById(Long orderId, Long customerId);

	@Query(nativeQuery = true, value = "select * from orders where id = ?1 and status not in ('CANCELLED', 'COMPLETED')")
	Optional<Order> findActiveOrder(Long orderId);
	
}
