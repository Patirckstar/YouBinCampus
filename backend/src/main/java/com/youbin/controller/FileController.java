package com.youbin.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.youbin.common.BizException;
import com.youbin.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class FileController {

    private static final List<String> ALLOWED_EXT = List.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/image")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件为空");
        }
        String ext = FileUtil.extName(file.getOriginalFilename());
        if (ext == null || !ALLOWED_EXT.contains(ext.toLowerCase())) {
            throw new BizException("仅支持图片格式");
        }
        String filename = IdUtil.fastSimpleUUID() + "." + ext.toLowerCase();
        File dest = new File(uploadPath, filename).getAbsoluteFile();
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
        } catch (Exception e) {
            throw new BizException("图片保存失败");
        }
        return Result.ok("/upload/" + filename);
    }
}