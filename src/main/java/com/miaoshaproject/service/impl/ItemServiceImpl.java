package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDaoMapper;
import com.miaoshaproject.dao.ItemStockDaoMapper;
import com.miaoshaproject.dataobject.ItemDao;
import com.miaoshaproject.dataobject.ItemStockDao;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.rmi.server.ExportException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {


    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDaoMapper itemDaoMapper;

    @Autowired
    private ItemStockDaoMapper itemStockDaoMapper;

    @Autowired
    private PromoService promoService;

    private ItemDao convertItemDaoFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDao itemDao = new ItemDao();
        BeanUtils.copyProperties(itemModel, itemDao);
        itemDao.setPrice(itemModel.getPrice().doubleValue());
        return itemDao;

    }

    private ItemStockDao convertItemStockDaoFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDao itemStockDao = new ItemStockDao();
//        BeanUtils.copyProperties(itemModel,itemStockDao);
        itemStockDao.setItemId(itemModel.getId());
        itemStockDao.setStock(itemModel.getStock());
        return itemStockDao;


    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {

        int affectedRow = itemStockDaoMapper.decreaseStock(itemId, amount);

        if (affectedRow > 0) return true;
        else return false;
//        return false;
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDaoMapper.increaseSales(itemId, amount);
    }

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result = validator.validate(itemModel);
//        System.out.println(result.isHasErrors());
        if (result.isHasErrors()) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        //装换itemmodel dataobject
//        System.out.println(itemModel);
        ItemDao itemDao = this.convertItemDaoFromItemModel(itemModel);
//        System.out.println(itemDao.getImgUrl());


        //写入数据库
//        System.out.println(1);

        itemDaoMapper.insertSelective(itemDao);


//        System.out.println(1);
        itemModel.setId(itemDao.getId());
//        System.out.println(1);


        ItemStockDao itemStockDao = this.convertItemStockDaoFromItemModel(itemModel);

        itemStockDaoMapper.insertSelective(itemStockDao);

        //返回创建完成的model

        ItemModel itemModel1forReturn = this.getItemById(itemModel.getId());


        return itemModel1forReturn;
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDao> itemDaoList = itemDaoMapper.listItem();
        List<ItemModel> itemModelList = itemDaoList.stream().map(itemDao -> {
            ItemStockDao itemStockDao = itemStockDaoMapper.selectByItemId(itemDao.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDao, itemStockDao);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {

        //通过商品id查询得到商品Dao

        ItemDao itemDao = itemDaoMapper.selectByPrimaryKey(id);

        //判断商品Dao是否为空
        if (itemDao == null) {
            return null;
        }

        //通过获取的商品id获取itemStock表中的itemStockDao商品库存表
        ItemStockDao itemStockDao = itemStockDaoMapper.selectByItemId(itemDao.getId());


        //将dataobject装成model
        ItemModel itemModel = convertModelFromDataObject(itemDao, itemStockDao);

        //获取秒杀活动的商品信息  将秒杀活动的信息聚合到秒杀商品的信息页面


        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
//        System.out.println(promoModel.getStartDate());
//        System.out.println(promoModel.getPromoItemPrice());
//        System.out.println(promoModel.getStatus());
        if (promoModel != null && promoModel.getStatus().intValue() != 3) {
            itemModel.setPromoModel(promoModel);

        }


        return itemModel;

    }

    private ItemModel convertModelFromDataObject(ItemDao itemDao, ItemStockDao itemStockDao) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDao, itemModel);
        itemModel.setPrice(new BigDecimal(itemDao.getPrice()));
        itemModel.setStock(itemStockDao.getStock());
        return itemModel;
    }
}
