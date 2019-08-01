package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.PromoDaoMapper;
import com.miaoshaproject.dataobject.PromoDao;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.websocket.server.ServerEndpoint;
import java.math.BigDecimal;


@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDaoMapper promoDaoMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {

        //根据商品的itemId查询对应的秒杀活动对应的Dao
        PromoDao promoDao = promoDaoMapper.selectByItemId(itemId);

        //将Dao转换成Model类型
        PromoModel promoModel = this.convertfromModel(promoDao);

        if (promoModel == null) return null;

        //判断秒杀活动
        if (promoModel.getStartDate().isAfterNow()) {
            promoModel.setStatus(1); //秒杀活动还未开始
        } else if (promoModel.getEndDate().isBeforeNow()) {
            promoModel.setStatus(3); //秒杀活动已经结束
        } else {
            promoModel.setStatus(2); //秒杀活动正在进行中
        }


        return promoModel;
    }

    private PromoModel convertfromModel(PromoDao promoDao) {
        if (promoDao == null) return null;
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDao, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDao.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDao.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDao.getEndDate()));
        return promoModel;
    }
}
