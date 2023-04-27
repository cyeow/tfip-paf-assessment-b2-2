package ibf2022.batch2.paf.server.repositories;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import ibf2022.batch2.paf.server.models.Comment;
import ibf2022.batch2.paf.server.models.Restaurant;
import jakarta.json.Json;

@Repository
public class RestaurantRepository {

	@Autowired
	private MongoTemplate mongo;

	private static final String COLLECTION_RESTAURANTS = "restaurants";
	private static final String COLLECTION_COMMENTS = "comments";
	private static final String FIELD_CUISINE = "cuisine";
	private static final String FIELD_RESTAURANT_ID = "restaurant_id";
	private static final String FIELD_RESTAURANTID = "restaurantId";

	// TODO: Task 2
	// Do not change the method's signature
	// Write the MongoDB query for this method in the comments below
	// db.restaurants.distinct("cuisine");
	public List<String> getCuisines() {
		List<String> result = mongo.findDistinct(new Query(), FIELD_CUISINE, COLLECTION_RESTAURANTS, String.class);
		// the following also works
		// List<String> result =
		// mongo.query(String.class).inCollection(COLLECTION_RESTAURANTS).distinct(FIELD_CUISINE).as(String.class).all();
		return result;
	}

	// TODO: Task 3
	// Do not change the method's signature
	// Write the MongoDB query for this method in the comments below
	// db.restaurants.find({cuisine: 'Delicatessen'});
	public List<Restaurant> getRestaurantsByCuisine(String cuisine) {
		Query q = new Query();
		q.addCriteria(Criteria.where(FIELD_CUISINE).is(cuisine));
		List<Document> resultDocument = mongo.find(q, Document.class, COLLECTION_RESTAURANTS);

		List<Restaurant> result = new LinkedList<>();
		resultDocument.forEach(d -> result.add(convertDocumentToRestaurant(d)));
		return result;
	}

	// TODO: Task 4
	// Do not change the method's signature
	// Write the MongoDB query for this method in the comments below
	// db.restaurants.aggregate([
	// {
	// $match: {restaurant_id: "40827287"}
	// },
	// {
	// $lookup:
	// {
	// from: "comments",
	// localField: "restaurant_id",
	// foreignField: "restaurantId",
	// as: "comments",
	// },
	// },
	// ])
	public Optional<Restaurant> getRestaurantById(String id) {
		MatchOperation mOp = Aggregation.match(Criteria.where(FIELD_RESTAURANT_ID).is(id));
		LookupOperation lOp = Aggregation.lookup(COLLECTION_COMMENTS, FIELD_RESTAURANT_ID, FIELD_RESTAURANTID,
				COLLECTION_COMMENTS);

		List<Document> resultList = mongo
				.aggregate(Aggregation.newAggregation(mOp, lOp), COLLECTION_RESTAURANTS, Document.class)
				.getMappedResults();

		if (resultList.size() == 0) {
			return Optional.empty();
		}

		// convert to restaurant
		// should only have 1 result
		Document d = resultList.get(0);

		Restaurant r = convertDocumentToRestaurant(d);
		// comments
		List<Document> commentDocuments = d.getList("comments", Document.class);
		List<Comment> comments = new LinkedList<>();

		commentDocuments.forEach(comment -> {
			Comment c = new Comment();
			c.setRestaurantId(comment.getString("restaurantId"));
			c.setName(comment.getString("name"));
			c.setDate(comment.getLong("date"));
			c.setComment(comment.getString("comment"));
			c.setRating(comment.getInteger("rating"));
			comments.add(c);
		});
		r.setComments(comments);

		return Optional.of(r);
	}

	// TODO: Task 5
	// Do not change the method's signature
	// Write the MongoDB query for this method in the comments below
	// db.comments.insertOne( { restaurantId: "40709530", name: "fish", date: Long("1682602431365"), comment: "loved the sausages and beer", rating: 5 })
	public void insertRestaurantComment(Comment comment) {
		mongo.insert(convertCommentToDocument(comment), COLLECTION_COMMENTS);
	}

	// helper methods
	private Restaurant convertDocumentToRestaurant(Document d) {
		Restaurant r = new Restaurant();
		r.setRestaurantId(d.getString("restaurant_id"));
		r.setName(d.getString("name"));
		r.setCuisine(d.getString("cuisine"));

		List<String> addr = new LinkedList<>();
		addr.add(d.getEmbedded(List.of("address", "building"), String.class));
		addr.add(d.getEmbedded(List.of("address", "street"), String.class));
		addr.add(d.getEmbedded(List.of("address", "zipcode"), String.class));
		addr.add(d.getString("borough"));
		r.setAddress(String.join(", ", addr));
		return r;
	}

	private Document convertCommentToDocument(Comment c) {
		return Document.parse(
				Json.createObjectBuilder()
						.add("restaurantId", c.getRestaurantId())
						.add("name", c.getName())
						.add("date", c.getDate())
						.add("comment", c.getComment())
						.add("rating", c.getRating())
						.build().toString());
	}
}
