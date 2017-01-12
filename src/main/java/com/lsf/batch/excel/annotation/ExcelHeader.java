package com.lsf.batch.excel.annotation;

import java.lang.annotation.*;

/**
 * Created by lishenfei on 2017-01-10.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelHeader {
    String value();
}
