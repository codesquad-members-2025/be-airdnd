package rice_monkey.Image.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rice_monkey.Image.dto.ImageResponse;
import rice_monkey.Image.dto.ImageUploadResponse;
import rice_monkey.Image.service.ImageService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    public ImageUploadResponse uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return imageService.uploadImage(file);
    }

    @GetMapping("/{id}")
    public ImageResponse getImageById(@PathVariable long id) {
        return imageService.getImage(id);
    }
}
