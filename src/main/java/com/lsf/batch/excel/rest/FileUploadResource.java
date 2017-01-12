package com.lsf.batch.excel.rest;

import com.lsf.batch.excel.annotation.LinesToSkip;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.n3r.idworker.Id;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Collection;

import static com.lsf.batch.excel.config.BatchConfig.BASE_PACKAGE;

/**
 * Created by lishenfei on 2017-01-04.
 */
@RestController
@RequestMapping("/api/file")
@Slf4j
public class FileUploadResource {

    @Value("${spring.batch.tmpDirPath}")
    private String tmpDirPath;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job importJob;

    @PostMapping("/excels")
    public ResponseEntity<?> handelFile(@RequestParam("file") MultipartFile multipartFile,
                                        @RequestParam("targetType") String targetType) throws Exception {
        if (multipartFile == null) {
            return ResponseEntity.badRequest().body("MultipartFile is empty.");
        }
        String filePath = saveMultipartFile(multipartFile);
        String className = BASE_PACKAGE + "." + targetType;
        LinesToSkip annotation = Class.forName(className).getAnnotation(LinesToSkip.class);
        long linesToSkip = (annotation == null) ? 0 : annotation.value();

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("filePath", filePath)
                .addString("targetType", className)
                .addLong("linesToSkip", linesToSkip)
                .toJobParameters();
        return getResponseEntity(jobLauncher.run(importJob, jobParameters));
    }

    private String saveMultipartFile(MultipartFile multipartFile) throws Exception {
        String dirPath = tmpDirPath + Id.next();
        new File(dirPath).mkdirs();
        String filePath = dirPath + "/" + multipartFile.getOriginalFilename();
        multipartFile.transferTo(new File(filePath));
        return filePath;
    }

    private ResponseEntity<?> getResponseEntity(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            BatchResult result = BatchResult.builder().code(BatchResult.COMPLETED).message(BatchStatus.COMPLETED.name()).build();
            return ResponseEntity.ok().body(result);
        }
        Collection<StepExecution> exceptions = jobExecution.getStepExecutions();
        String message = jobExecution.getStatus().name();
        if (exceptions != null && exceptions.iterator().hasNext()) {
            String desc = exceptions.iterator().next().getExitStatus().getExitDescription();
            desc = desc.substring(0, desc.indexOf(": "));
            message += ": " + desc;
        }
        BatchResult result = BatchResult.builder().code(BatchResult.FAILED).message(message).build();
        return ResponseEntity.ok().body(result);
    }

    @Data
    @Builder
    public static class BatchResult {

        public static final int COMPLETED = 0;
        public static final int FAILED = 1;

        private int code;
        private String message;
    }
}
