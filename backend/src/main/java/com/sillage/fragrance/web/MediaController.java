package com.sillage.fragrance.web;

import com.sillage.fragrance.media.CommonsMediaService;
import com.sillage.fragrance.media.FragranceMedia;
import com.sillage.fragrance.media.FragranceMediaRequest;
import com.sillage.fragrance.media.FragranceImageBatchRequest;
import com.sillage.fragrance.media.FragranceImageBatch;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media")
public class MediaController {

	private final CommonsMediaService mediaService;

	public MediaController(CommonsMediaService mediaService) {
		this.mediaService = mediaService;
	}

	@PostMapping
	public FragranceMedia findMedia(@Valid @RequestBody FragranceMediaRequest request) {
		return mediaService.findMedia(request);
	}

	@PostMapping("/fragrances")
	public FragranceImageBatch findFragranceImages(
			@Valid @RequestBody FragranceImageBatchRequest request
	) {
		return mediaService.findFragranceImages(request);
	}
}
