package com.smartnursing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("stat_dept")
public class StatDept {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deptId;
    private LocalDate statDate;
    private Long workOrderCount;
    private Double costTotal;
}
