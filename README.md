
## Excel Import Job

若需要导入Excel文件至数据库，开发流程如下：

1 在 com.lsf.batch.excel.domain 包下建立数据模型类

    @ExcelHeader -- 定义 Excel sheet 标题名
    @LinesToSkip -- 定义 Excel sheet 第几行为标题

2 运行项目，http://localhost:8007/api/file/excels 导入 Excel 文件

