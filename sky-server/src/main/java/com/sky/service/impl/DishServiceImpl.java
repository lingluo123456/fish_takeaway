package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmeaelDishMapper;

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        List<DishVO> dishList = dishMapper.page(dishPageQueryDTO);
        Page<DishVO> pageInfo = (Page<DishVO>) dishList;
        return new PageResult(pageInfo.getTotal(),pageInfo.getResult());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.saveDish(dish);
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.saveFlavor(flavors);
        }



    }
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        DishVO dishVO =dishMapper.getById(id);
        if (dishVO == null)return null;
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(flavors);
        return dishVO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()){
            dishFlavorMapper.deleteByDishId(dish.getId());
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dish.getId());
            }
            dishFlavorMapper.saveFlavor(flavors);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids){
            DishVO dishVO =dishMapper.getById(id);
            if (dishVO.getStatus() == StatusConstant.ENABLE){
                throw new RuntimeException("起售中的菜品不能删除");
            }
            if (setmeaelDishMapper.getSetmealIdsByDishIds(ids) != null){
                throw new RuntimeException("菜品正在被套餐关联，不能删除");
            }
            dishMapper.deleteById(id);
            dishFlavorMapper.deleteByDishId(id);
        }
    }


    @Override
    public void startOrStop(Integer status, Long id) {
        DishVO dishVO =dishMapper.getById(id);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishVO,dish);
        dish.setStatus(status);
        dishMapper.startOrStop(dish);
    }
}
