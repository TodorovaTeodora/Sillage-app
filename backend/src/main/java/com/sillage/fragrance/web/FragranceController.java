package com.sillage.fragrance.web;

import com.sillage.fragrance.model.Fragrance;
import com.sillage.fragrance.model.FragranceFacets;
import com.sillage.fragrance.service.FragranceService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/fragrances")
public class FragranceController {

	private final FragranceService service;

	public FragranceController(FragranceService service) {
		this.service = service;
	}

	@GetMapping
	public List<Fragrance> search(
			@RequestParam(required = false) String query,
			@RequestParam(required = false) List<String> manufacturer,
			@RequestParam(required = false) String note,
			@RequestParam(required = false) @Min(1700) Integer fromYear,
			@RequestParam(required = false) @Min(1700) @Max(Year.MAX_VALUE) Integer toYear
	) {
		return service.search(query, manufacturer, note, fromYear, toYear);
	}

	@GetMapping("/{id}")
	public Fragrance getById(@PathVariable String id) {
		return service.findById(id);
	}

	@GetMapping("/facets")
	public FragranceFacets facets() {
		return service.facets();
	}
}
