package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    /* 의존 관계 추가 */
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // 엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        // 배송 정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        delivery.setDeliveryStatus(DeliveryStatus.READY);

        // 주문 상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
//        OrderItem orderItem1 = new OrderItem(); // NoArgsConstructor 로 인해 protected 생성자이므로 사용하지 못함

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저장
        orderRepository.save(order); // 하나만 저장해 줘도 orderitem이랑 delivery가 persist 된다
        /*
         * cascade 사용 ??
         * 참조하는 것이 private owner? 인 경우에서 사용
         * 다른 값이 참조할 수 없는 경우 사용
         */
        return order.getId();
    }

    /**
     * 주문 취소
     *
     * @param orderId order id
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        // 주문 취소
        order.cancel();
    }

    // 검색
//    public List<Order> findOrder(OrderSearch orderSearch){
//        return orderRepository.findAll(orderSearch);
//    }

}
