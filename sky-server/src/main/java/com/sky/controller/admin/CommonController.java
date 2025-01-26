package com.sky.controller.admin;


import com.sky.result.Result;
import io.lettuce.core.output.ListOfGenericMapsOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequestMapping("/admin/common")
public class CommonController {
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file.getOriginalFilename());
        return Result.success("https://web11245.oss-cn-beijing.aliyuncs.com/2025/01/ffb4d178-7614-4c87-be7b-2de4347c4c03.jpg");
    }
}
