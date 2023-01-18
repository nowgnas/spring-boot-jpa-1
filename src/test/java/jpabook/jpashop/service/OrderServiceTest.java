package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Fail.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {
    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        // given
        Member member = getMember("회원1", "서울", "강가", "123-123");

        Item book = getItem("시골", 10000, 10);

        // when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        Assert.assertEquals("상품 주문 시 상태는 order", OrderStatus.ORDER, getOrder.getStatus());
        Assert.assertEquals("주문한 상품 종류 수가 정확해야 한다 ", 1, getOrder.getOrderItems().size());
        Assert.assertEquals("주문 가격은 가격 * 수량이다", 10000 * orderCount, getOrder.getTotalPrice());
        Assert.assertEquals("주문 수량만큼 재고가 줄어야 한다 ", 8, book.getStockQuantity());

    }

    private Item getItem(String bookName, int price, int stockQuantity) {
        Item book = new Book();
        book.setName(bookName);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member getMember(String name, String city, String street, String zipcode) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address(city, street, zipcode));
        em.persist(member);
        return member;
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws Exception {
        // given
        Member member = getMember("name", "city", "street", "1");
        Item item = getItem("jpa", 10000, 10);

        int orderCount = 11;
        // when
        orderService.order(member.getId(), item.getId(), orderCount);

        // then
        fail("재고 수량 부족 예외 ");
    }

    @Test
    public void 주문취소() throws Exception {
        // given
        Member member = getMember("name", "city", "street", "1");
        Item item = getItem("jpa", 10000, 10);

        int orderCount = 2;

        Long orderID = orderService.order(member.getId(), item.getId(), orderCount);
        // when

        orderService.cancelOrder(orderID);

        // then
        Order getOrder = orderRepository.findOne(orderID);
        Assert.assertEquals("주문 취소시 상태는 cancel이다 ", OrderStatus.CANCEL, getOrder.getStatus());
        Assert.assertEquals("주문이 취소된 상품은 그만큼 재고가 증가해야 함 ", 10, item.getStockQuantity());

    }


}