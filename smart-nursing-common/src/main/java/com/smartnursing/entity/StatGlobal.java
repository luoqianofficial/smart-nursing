package com.smartnursing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("stat_global")
public class StatGlobal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Long totalWorkOrder;
    private Double totalCost;
    private Long resourceVisit;
    private LocalDateTime createTime;
}
