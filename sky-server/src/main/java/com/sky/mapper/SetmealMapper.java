package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    List<SetmealVO> page(Setmeal setmeal);

    @AutoFill(value = OperationType.INSERT)
    @Options(useGeneratedKeys = true,keyProperty = "id")
    void insertSetmeal(Setmeal setmeal);

    @Select("select s.*,c.name as category_name from setmeal s left outer join category c on s.category_id = c.id" +
            "  where s.id =#{id}")
    SetmealVO getById(Long id);

    void deleteByIds(List<Long> ids);

    @AutoFill(value = OperationType.UPDATE)
    void startOrStop(Setmeal setmeal);

    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);
}
