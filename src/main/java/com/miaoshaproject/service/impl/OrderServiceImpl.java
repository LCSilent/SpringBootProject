package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDaoMapper;
import com.miaoshaproject.dao.SequenceDaoMapper;
import com.miaoshaproject.dataobject.OrderDao;
import com.miaoshaproject.dataobject.SequenceDao;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDaoMapper orderDaoMapper;

    @Autowired
    private SequenceDaoMapper sequenceDaoMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {

        //校验用户的下单状态，下单的商品是否存在，用户是否合法，购买的数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if (itemModel == null)
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "商品信息不存在");

        UserModel userModel = userService.getUserById(userId);
        if (userModel == null)
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "该用户不存在");

        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "数量信息不存在");
        }
        //校验活动信息
        System.out.println(promoId);
//        System.out.println(itemModel.getPromoModel());
//        System.out.println(itemModel.getPromoModel());
        if (promoId != null) {
            //校验对应活动是否存在适用商品
            if (promoId.intValue() != itemModel.getPromoModel().getId())
                throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
            else if (itemModel.getPromoModel().getStatus().intValue() != 2)
                throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "活动还未开始");
        }

        System.out.println(promoId);
        //落单减库存
        boolean result = itemService.decreaseStock(itemId, amount);

        if (!result)
            throw new BusinessException(EmBussinessError.STOCK_NOT_ENOUGH);


        //订单入库
        System.out.println(promoId);
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if (promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        System.out.println(promoId);
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        orderModel.setId(generaterOrderNo());


        //生成交易流水号，因为不是自增的主键

        OrderDao orderDao = this.convertFromOrderModel(orderModel);

        System.out.println(orderDao.getPromoId());
        orderDaoMapper.insertSelective(orderDao);


        //加上商品销量
        itemService.increaseSales(itemId, amount);

        //返回前端


        return orderModel;
    }

    //按一定规则生成交易订单号

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generaterOrderNo() {

        /*
        订单号有16位

        1、前八位为时间信息

        2.中间位为自增序列


         */

        StringBuilder sb = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();
        String nowDATE = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        sb.append(nowDATE);

        int sequence = 0;
        SequenceDao sequenceDao = sequenceDaoMapper.getSequenceByName("order_info");
        sequence = sequenceDao.getCurrentValue();
        sequenceDao.setCurrentValue(sequenceDao.getCurrentValue() + sequenceDao.getStep());
        sequenceDaoMapper.updateByPrimaryKeySelective(sequenceDao);

        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6 - sequenceStr.length(); i++) {
            sb.append(0);
        }
        sb.append(sequenceStr);
        sb.append("00");

        return sb.toString();
    }

    //将ordermodel转换成dataobject模式

    private OrderDao convertFromOrderModel(OrderModel orderModel) {

        if (orderModel == null) return null;
        OrderDao orderDao = new OrderDao();
        BeanUtils.copyProperties(orderModel, orderDao);
        orderDao.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDao.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDao;
    }
}
