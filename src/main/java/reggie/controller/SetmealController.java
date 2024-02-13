package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import reggie.common.R;
import reggie.dto.SetmealDto;
import reggie.entity.Category;
import reggie.entity.Setmeal;
import reggie.entity.SetmealDish;
import reggie.service.CategoryService;
import reggie.service.SetmealDishService;
import reggie.service.SetmealService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true) //删除所有的缓存数据
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("新增成功");

    }
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //1 创建分页构造器
        Page<Setmeal> pageInfo = new Page<Setmeal>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        // 查询条件
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name != null, Setmeal::getName, name);
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, lambdaQueryWrapper);
        //拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();


        List<SetmealDto> list = records.stream().map((item) -> {
            //分类ID
            Long categoryId = item.getCategoryId();
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            //名字
            Category category = categoryService.getById(categoryId);
            if(category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;



        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);

    }

    /***
     * 删除套餐
     * @param ids
     * @return
     */
    //多张表的操作可以封装到service中完成
    //对于多个参数的可以直接用list来接受
    @CacheEvict(value = "setmealCache", allEntries = true) //删除所有的缓存数据
    @DeleteMapping
    public R<String> delete(@RequestParam  List<Long> ids) {
        setmealService.removeWithDish(ids);

        return R.success("套餐删除成功");
    }
    //返回套餐信息

    /***
     * 根据条件来查找信息
     * @param setmeal
     * @return
     */
    //如果是键值对的话可以直接来接收，自动封装为对象
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId +'_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(setmeal.getCategoryId()!= null, Setmeal::getCategoryId, setmeal.getCategoryId());
        lambdaQueryWrapper.eq(setmeal.getStatus() != 0, Setmeal::getStatus, setmeal.getStatus());
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(lambdaQueryWrapper);

        return R.success(list);


    }


}
