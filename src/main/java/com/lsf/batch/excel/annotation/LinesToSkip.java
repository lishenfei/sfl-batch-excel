package com.lsf.batch.excel.annotation;

import java.lang.annotation.*;

/**
 * Created by lishenfei on 2017-01-10.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LinesToSkip {
    long value();
}
