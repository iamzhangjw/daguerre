package pers.zjw.daguerre.endpoint;

import pers.zjw.daguerre.oss.OssApi;
import pers.zjw.daguerre.pojo.vo.*;
import pers.zjw.daguerre.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

/**
 * storage controller
 *
 * @author zhangjw
 * @date 2022/05/02 0002 19:09
 */
@RestController
@RequestMapping("/oss")
public class StorageController {
    @Autowired
    private StorageService fileService;



    @GetMapping("/chunkSize")
    public int chunkSize() {
        return OssApi.DEFAULT_CHUNK_SIZE;
    }

    @PostMapping("/u")
    public FileUploadSchedule upload(
            @RequestParam("file") MultipartFile file, FileUpload upload) {
        return fileService.upload(file, upload);
    }

    @GetMapping("/d/{fileId}")
    public ResponseEntity<Resource> download(
            @PathVariable("fileId") String fileId,
            @RequestParam(required = false) Long offset,
            @RequestParam(required = false) Long length) {
        return fileService.download(fileId, offset, length);
    }

    @PostMapping("/url")
    public Collection<FileUrlVO> url(@Validated @RequestBody FileUrlQuery query) {
        return fileService.url(query);
    }

    @GetMapping("/url/{fileId}")
    public String url(
            @PathVariable("fileId") String fileId, @RequestParam(required = false) Integer expireMinutes) {
        return fileService.url(fileId, expireMinutes);
    }

    @GetMapping("/meta/{fileId}")
    public FileMeta metadata(@PathVariable("fileId") String fileId) {
        return fileService.meta(fileId);
    }
}
