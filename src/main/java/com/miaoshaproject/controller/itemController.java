package com.miaoshaproject.controller;


import com.miaoshaproject.controller.viewObject.ItemVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommomReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.model.ItemModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/item")
@RequestMapping("/item")
@CrossOrigin(allowedHeaders = "*", allowCredentials = "true")
public class itemController extends BaseController {


    @Autowired
    private ItemService itemService;

    //创建商品
    @RequestMapping(value = "/create", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommomReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl,
                                       @RequestParam(name = "description") String description) throws BusinessException {
        //封装service请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);
        itemModel.setDescription(description);
//        System.out.println(itemModel);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);
//        System.out.println(itemModelForReturn.getTitle());

        ItemVO itemVO = convertVOFromModel(itemModelForReturn);
//        System.out.println(itemVO);

        return CommomReturnType.create(itemVO);


    }


    //商品详情页面浏览
    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    @ResponseBody
    public CommomReturnType getItem(@RequestParam(name = "id") Integer id) {
        ItemModel itemModel = itemService.getItemById(id);
        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommomReturnType.create(itemVO);
    }


    //商品列表页面浏览
    @RequestMapping(value = "/list", method = {RequestMethod.GET})
    @ResponseBody
    public CommomReturnType listItem() {
        List<ItemModel> itemModelList = itemService.listItem();
        //使用stream将list
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = this.convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return CommomReturnType.create(itemVOList);
    }


    private ItemVO convertVOFromModel(ItemModel itemModel) {
        if (itemModel == null) return null;
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        if (itemModel.getPromoModel() != null) {
            //有秒杀活动
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            System.out.println(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setStartTime(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());

        } else {
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }
}
