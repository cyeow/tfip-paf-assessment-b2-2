package ibf2022.batch2.paf.server.services;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ibf2022.batch2.paf.server.models.Comment;
import ibf2022.batch2.paf.server.models.Restaurant;
import ibf2022.batch2.paf.server.repositories.RestaurantRepository;

@Service
public class RestaurantService {

	@Autowired
	private RestaurantRepository repo;

	// TODO: Task 2
	// Do not change the method's signature
	public List<String> getCuisines() {
		List<String> unformattedResult = repo.getCuisines();
		List<String> result = new LinkedList<>();

		unformattedResult.forEach(r -> result.add(r.replace('/', '_')));
		result.sort(Comparator.naturalOrder());
		return result;
	}

	// TODO: Task 3
	// Do not change the method's signature
	public List<Restaurant> getRestaurantsByCuisine(String cuisine) {
		List<Restaurant> result = repo.getRestaurantsByCuisine(cuisine);
		result.sort((c1, c2) -> c1.getName().compareTo(c2.getName()));
		return result;
	}

	// TODO: Task 4
	// Do not change the method's signature
	public Optional<Restaurant> getRestaurantById(String id) {
		return repo.getRestaurantById(id);
	}

	// TODO: Task 5
	// Do not change the method's signature
	public void postRestaurantComment(Comment comment) {
		// set date
		comment.setDate((new Date()).getTime());

		repo.insertRestaurantComment(comment);
	}

	// helper methods
	public boolean isValidRestaurantId(String restaurantId) {
		if (getRestaurantById(restaurantId).isEmpty()) {
			return false;
		}
		return true;
	}
}
