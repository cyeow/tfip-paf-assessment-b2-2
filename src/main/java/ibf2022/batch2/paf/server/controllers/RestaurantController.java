package ibf2022.batch2.paf.server.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ibf2022.batch2.paf.server.models.Comment;
import ibf2022.batch2.paf.server.models.Restaurant;
import ibf2022.batch2.paf.server.services.RestaurantService;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

@RestController
@RequestMapping(path = "/api")
public class RestaurantController {

	@Autowired
	private RestaurantService svc;

	// TODO: Task 2 - request handler
	@GetMapping(path = "/cuisines", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getCuisineList() {
		List<String> cuisines = svc.getCuisines();

		return ResponseEntity.status(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON)
				.body(listToJsonArrayString(cuisines));
	}

	// TODO: Task 3 - request handler
	@GetMapping(path = "/restaurants/{cuisine}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getRestaurantsByCuisine(@PathVariable String cuisine) {
		// replace / in cuisine
		cuisine = cuisine.replace('_', '/');

		List<Restaurant> restaurants = svc.getRestaurantsByCuisine(cuisine);

		return ResponseEntity.status(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON)
				.body(generateRestaurantsOverview(restaurants));
	}

	// TODO: Task 4 - request handler
	@GetMapping(path = "/restaurant/{restaurantId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getRestaurantById(@PathVariable String restaurantId) {
		Optional<Restaurant> optR = svc.getRestaurantById(restaurantId);

		if (optR.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.contentType(MediaType.APPLICATION_JSON)
					.body(generateMsgJsonString("error", "Missing " + restaurantId));
		}

		return ResponseEntity.status(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON)
				.body(restaurantToJsonString(optR.get()));
	}

	// TODO: Task 5 - request handler
	@PostMapping(path = "/restaurant/comment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> postComment(Comment c) {
		if (!isValidComment(c)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.contentType(MediaType.APPLICATION_JSON)
					.body(generateMsgJsonString("error", "Invalid comment content."));
		}

		svc.postRestaurantComment(c);

		return ResponseEntity.status(HttpStatus.CREATED)
				.contentType(MediaType.APPLICATION_JSON)
				.body("{ }");

	}

	// helper methods
	private String restaurantToJsonString(Restaurant r) {
		JsonArrayBuilder ab = Json.createArrayBuilder();
		if (r.getComments() != null && r.getComments().size() > 0) {
			r.getComments().forEach(c -> {
				JsonObject o = Json.createObjectBuilder()
						.add("restaurantId", c.getRestaurantId())
						.add("name", c.getName())
						.add("date", c.getDate())
						.add("comment", c.getComment())
						.add("rating", c.getRating())
						.build();
				ab.add(o);
			});
		}

		return Json.createObjectBuilder()
				.add("restaurant_id", r.getRestaurantId())
				.add("name", r.getName())
				.add("cuisine", r.getCuisine())
				.add("address", r.getAddress())
				.add("comments", ab)
				.build()
				.toString();
	}

	private String listToJsonArrayString(List<String> list) {
		JsonArrayBuilder ab = Json.createArrayBuilder();
		list.forEach(item -> ab.add(item));

		return ab.build().toString();
	}

	private String generateRestaurantsOverview(List<Restaurant> list) {
		JsonArrayBuilder ab = Json.createArrayBuilder();
		list.forEach(item -> ab.add(
				Json.createObjectBuilder()
						.add("restaurantId", item.getRestaurantId())
						.add("name", item.getName())
						.build()));

		return ab.build().toString();
	}

	private String generateMsgJsonString(String fieldName, String message) {
		return Json.createObjectBuilder()
				.add(fieldName, message)
				.build()
				.toString();

	}

	private boolean isValidComment(Comment c) {
		if (!svc.isValidRestaurantId(c.getRestaurantId())) {
			return false;
		}
		if (c.getName().length() <= 3) {
			return false;
		}
		if (c.getRating() < 1 || c.getRating() > 5) {
			return false;
		}
		return true;
	}
}
