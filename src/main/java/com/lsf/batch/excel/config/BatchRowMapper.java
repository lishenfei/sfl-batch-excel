package com.lsf.batch.excel.config;

import com.lsf.batch.excel.annotation.ExcelHeader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.Sheet;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lishenfei on 2017-01-10.
 */
@Slf4j
public class BatchRowMapper implements RowMapper<Object> {

    private Class targetClazz;

    public BatchRowMapper(Class targetClazz) {
        this.targetClazz = targetClazz;
    }

    @Override
    public Object mapRow(Sheet sheet, String[] columns, int rowCount) throws Exception {
        Map<String, String> rowMap = new HashMap<>();
        int index = 0;
        for (String head : sheet.getHeader()) {
            if (index >= columns.length) {
                break;
            }
            if (StringUtils.isNotEmpty(head)) {
                rowMap.put(head, columns[index]);
            }
            index++;
        }
        return transformTargetObject(rowMap);
    }

    public Object transformTargetObject(Map<String, String> rowMap) throws IllegalAccessException, InstantiationException {
        Field[] fields = targetClazz.getDeclaredFields();
        Object targetObject = targetClazz.newInstance();
        ExcelHeader header;

        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelHeader.class)) {
                field.setAccessible(true);
                try {
                    header = field.getAnnotation(ExcelHeader.class);
                    field.set(targetObject, rowMap.get(header.value()));
                } catch (Exception e) {
                    log.error("Field set value by header exception. ", e);
                }
            } else if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                try {
                    field.set(targetObject, String.valueOf(org.n3r.idworker.Id.next()));
                } catch (Exception e) {
                    log.error("Field set Id exception. ", e);
                }
            }
        }
        return targetObject;
    }

}
