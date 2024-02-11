package reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reggie.dto.DishDto;
import reggie.entity.Dish;
import reggie.entity.DishFlavor;
import reggie.mapper.DishMapper;
import reggie.service.DishFlavorService;
import reggie.service.DishService;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j

public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    //由于实现了DishService，所以这个对象自动就可以使用这个方法，不需要来自动装配
    @Autowired
    private DishFlavorService dishFlavorService;

    /***
     * 新增菜品，同时保存口味信息
     * @param dishDto
     */
    //多张表需要加入事务控制
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //继承
        //save 的时候通过雪花算法生成id，然后插入到dishDto中, 所以可以直接那吃来
        this.save(dishDto);
        Long dishId = dishDto.getId();
        //口味:
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) ->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);


    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品信息'
        Dish dish = this.getById(id);
        //对象拷贝
        DishDto dishDto = new DishDto();
        //把属性拷贝拷贝进去
        BeanUtils.copyProperties(dish,dishDto);
        //查询口味表
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getId, id);
        List<DishFlavor> flavors = dishFlavorService.list(lambdaQueryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;



    }

    @Transactional
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //清理口味信息：
        //修改
        this.updateById(dishDto);
        //删除
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper= new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getId, dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);
        //插入：
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);



    }
}
