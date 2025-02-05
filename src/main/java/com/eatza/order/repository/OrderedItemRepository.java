package com.eatza.order.repository;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eatza.order.model.OrderedItem;

public interface OrderedItemRepository extends JpaRepository<OrderedItem, Long> {
	
	@Query(value = "SELECT sum(price) FROM Ordered_Items WHERE order_id = ?1", nativeQuery = true)
	Double findAmountByOrderId(Long orderId);
	
	@Query(value = "SELECT * FROM Ordered_Items WHERE order_id = ?1", nativeQuery = true)
	List<OrderedItem> findByOrderId(Long orderId);
	
	@Transactional
	void deleteByOrder_id(Long orderId);

}
