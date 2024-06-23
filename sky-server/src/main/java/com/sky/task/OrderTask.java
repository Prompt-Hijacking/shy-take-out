package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单,每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ?")
//    @Scheduled(cron = " 1/5 * * * * ? ")
    public void processTimeoutOrder() {
        log.info("定时处理超市订单...{}", LocalDateTime.now());
        //查询订单表，时候有处于待付款的订单;
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        if (ordersList != null && ordersList.size() != 0) {
            for (Orders order : ordersList) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }


    /**
     * 处理派送中订单,每天触发一次
     */
    @Scheduled(cron = " 0 0 4 * * ? ") //每月每天凌晨四点
//    @Scheduled(cron = " 0/5 * * * * ? ")
    public void processDeliveryOrder() {
        log.info("定时处理派送中订单...{}", LocalDateTime.now());
        //每天凌晨四点清算上一天的派送中订单(小于上一天24:00的订单都完成)
        LocalDateTime time = LocalDateTime.now().plusMinutes(-240);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && ordersList.size() != 0) {
            for (Orders order : ordersList) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }


}
