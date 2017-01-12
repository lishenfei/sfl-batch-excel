package com.lsf.batch.excel.domain;

import com.lsf.batch.excel.annotation.ExcelHeader;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity(name = "product")
@Data
@NoArgsConstructor
//@LinesToSkip(1)
public class Product {

    @Id
    @Column
    private String id;

    @ExcelHeader("商品")
    @Column(name = "title")
    @Size(max = 100, min = 1)
    private String name;

    @ExcelHeader("价格")
    @Column
    @Size(max = 20, min = 1)
    private String price;

}
